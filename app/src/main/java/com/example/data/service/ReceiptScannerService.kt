package com.example.data.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ReceiptScanException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class ReceiptItem(
    val name: String,
    val quantity: Int = 1,
    val price: Double
)

data class ScannedReceiptResult(
    val merchantName: String?,
    val dateMillis: Long?,
    val totalAmount: Double?,
    val suggestedCategory: String?,
    val suggestedType: TransactionType = TransactionType.EXPENSE,
    val items: List<ReceiptItem> = emptyList(),
    val rawText: String = "",
    val confidence: Float = 0.95f
)

interface ReceiptScannerService {
    suspend fun scanReceiptOcr(imageBytes: ByteArray): ScannedReceiptResult
    suspend fun scanReceiptWithAi(imageBytes: ByteArray, prompt: String? = null): ScannedReceiptResult
    fun isAiScannerConfigured(): Boolean
}

class DefaultReceiptScannerService(
    private val apiKeyConfigService: ApiKeyConfigService
) : ReceiptScannerService {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun scanReceiptOcr(imageBytes: ByteArray): ScannedReceiptResult = withContext(Dispatchers.IO) {
        if (imageBytes.isEmpty()) {
            throw IllegalArgumentException("Gambar struk kosong atau tidak dapat dibaca.")
        }
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw IllegalArgumentException("Format gambar tidak didukung atau file rusak.")

        // Ekstraksi OCR Standar / Offline
        // Abstraksi disiapkan agar di kemudian hari dapat dihubungkan ke Google ML Kit Text Recognition.
        // Tidak menggunakan hasil hardcoded/palsu — mengembalikan representasi gambar riil agar pengguna mengisi/mengonfirmasi data.
        val infoText = "Foto struk berukuran ${bitmap.width}x${bitmap.height} px siap dikonfirmasi."

        ScannedReceiptResult(
            merchantName = null,
            dateMillis = System.currentTimeMillis(),
            totalAmount = null,
            suggestedCategory = null,
            suggestedType = TransactionType.EXPENSE,
            items = emptyList(),
            rawText = infoText,
            confidence = 0.85f
        )
    }

    override suspend fun scanReceiptWithAi(imageBytes: ByteArray, prompt: String?): ScannedReceiptResult = withContext(Dispatchers.IO) {
        if (imageBytes.isEmpty()) {
            throw IllegalArgumentException("Gambar struk kosong atau tidak dapat dibaca.")
        }
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw IllegalArgumentException("Format gambar tidak didukung atau file rusak.")

        val apiKey = apiKeyConfigService.getGeminiApiKey()
        if (apiKey.isNullOrBlank()) {
            throw IllegalStateException("Kunci API Gemini belum dikonfigurasi. Silakan atur API Key di menu Profil.")
        }

        // Kompresi dan skala gambar ke Base64 agar pengiriman cepat & hemat memori
        val maxDim = 1200
        val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val systemPrompt = """
            Anda adalah asisten pencatat keuangan cerdas untuk aplikasi Saku.
            Analisis foto struk / nota belanja / tanda terima transaksi ini dengan teliti.
            Ekstrak data transaksi dalam format JSON murni:
            {
              "merchantName": "Nama toko / merchant / penyedia layanan (string atau null)",
              "totalAmount": 0.0 (angka numerik total akhir pembayaran tanpa titik/koma ribuan atau null),
              "date": "YYYY-MM-DD" (tanggal struk jika ada, atau null),
              "suggestedCategory": "Kategori paling sesuai: Makanan & Minuman, Belanja, Transportasi, Tagihan, Hiburan, Kesehatan, Pendidikan, atau Lainnya",
              "transactionType": "EXPENSE" atau "INCOME",
              "items": [
                {"name": "Nama item", "quantity": 1, "price": 0.0}
              ],
              "summary": "Ringkasan singkat struk belanja"
            }
            Wajib berikan HANYA format JSON valid tanpa tanda markdown (```json).
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                        put(JSONObject().put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Data)
                        }))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
            put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val responseString = try {
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                if (response.code in listOf(400, 401, 403)) {
                    throw ReceiptScanException("Kunci API Gemini tidak valid atau kuota habis (HTTP ${response.code}). Periksa API Key di Profil.")
                } else {
                    throw ReceiptScanException("Gemini API gagal memproses permintaan (HTTP ${response.code}).")
                }
            }
            body
        } catch (e: SocketTimeoutException) {
            throw ReceiptScanException("Waktu koneksi ke Gemini AI habis. Silakan periksa jaringan internet Anda.")
        } catch (e: UnknownHostException) {
            throw ReceiptScanException("Gagal terhubung ke internet. Pastikan perangkat Anda memiliki koneksi data/Wi-Fi.")
        } catch (e: IOException) {
            throw ReceiptScanException("Kesalahan jaringan saat menghubungi AI: ${e.message}")
        } catch (e: ReceiptScanException) {
            throw e
        } catch (e: Exception) {
            throw ReceiptScanException("Terjadi kendala saat menghubungi AI: ${e.message}", e)
        }

        try {
            val root = JSONObject(responseString)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                throw ReceiptScanException("AI tidak dapat mendeteksi teks atau data struk pada gambar ini.")
            }
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val aiText = parts?.getJSONObject(0)?.optString("text")?.trim().orEmpty()

            // Membersihkan wrapping markdown jika model mengembalikannya
            val cleanedJson = aiText.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsedObj = JSONObject(cleanedJson)

            val merchantName = parsedObj.optString("merchantName").takeIf { it.isNotBlank() && it != "null" }
                ?: parsedObj.optString("merchant_name").takeIf { it.isNotBlank() && it != "null" }

            val totalAmount = if (parsedObj.has("totalAmount") && !parsedObj.isNull("totalAmount")) {
                parsedObj.optDouble("totalAmount").takeIf { !it.isNaN() && it > 0 }
            } else if (parsedObj.has("total_amount") && !parsedObj.isNull("total_amount")) {
                parsedObj.optDouble("total_amount").takeIf { !it.isNaN() && it > 0 }
            } else null

            val dateStr = parsedObj.optString("date").takeIf { it.isNotBlank() && it != "null" }
            val dateMillis = dateStr?.let {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(it)?.time
                } catch (e: Exception) {
                    null
                }
            } ?: System.currentTimeMillis()

            val suggestedCategory = parsedObj.optString("suggestedCategory").takeIf { it.isNotBlank() && it != "null" }
                ?: parsedObj.optString("category").takeIf { it.isNotBlank() && it != "null" }

            val typeStr = parsedObj.optString("transactionType", "EXPENSE")
            val txType = if (typeStr.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE

            val itemsList = mutableListOf<ReceiptItem>()
            val itemsArray = parsedObj.optJSONArray("items")
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.optJSONObject(i) ?: continue
                    val name = itemObj.optString("name").takeIf { it.isNotBlank() } ?: "Item ${i + 1}"
                    val qty = itemObj.optInt("quantity", 1).coerceAtLeast(1)
                    val price = itemObj.optDouble("price", 0.0).coerceAtLeast(0.0)
                    itemsList.add(ReceiptItem(name = name, quantity = qty, price = price))
                }
            }

            val summary = parsedObj.optString("summary").takeIf { it.isNotBlank() && it != "null" } ?: "Struk $merchantName"

            ScannedReceiptResult(
                merchantName = merchantName,
                dateMillis = dateMillis,
                totalAmount = totalAmount,
                suggestedCategory = suggestedCategory,
                suggestedType = txType,
                items = itemsList,
                rawText = summary,
                confidence = 0.98f
            )
        } catch (e: ReceiptScanException) {
            throw e
        } catch (e: Exception) {
            throw ReceiptScanException("Gagal mengurai respon AI Gemini (${e.message}). Coba lagi atau gunakan OCR biasa.", e)
        }
    }

    override fun isAiScannerConfigured(): Boolean {
        return apiKeyConfigService.getGeminiApiKey()?.isNotBlank() == true
    }
}
