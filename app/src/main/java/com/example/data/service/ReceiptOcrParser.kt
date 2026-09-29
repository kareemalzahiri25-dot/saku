package com.example.data.service

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * Parser untuk ekstrak informasi transaksi dari raw text hasil OCR.
 * Pure Kotlin, no Android dependencies — mudah di-unit-test.
 */
object ReceiptOcrParser {

    // Pola untuk nominal Indonesia (Rp, IDR, angka dengan titik/koma ribuan)
    private val CURRENCY_PATTERNS = listOf(
        Pattern.compile("(?i)(?:Rp|IDR|Rupiah)\\s*[:.]?\\s*([\\d.,]+)"),
        Pattern.compile("(?i)(?:Grand Total|Total\\s*[:.]|Jumlah\\s*[:.]|Amount\\s*[:.])\\s*(?:Rp|IDR)?\\s*([\\d.,]+)"),
        Pattern.compile("(?i)(?:Bayar|Dibayar|Paid)\\s*[:.]?\\s*(?:Rp|IDR)?\\s*([\\d.,]+)"),
        Pattern.compile("(?i)([\\d]{1,3}(?:[.,]\\d{3})+(?:[.,]\\d{2})?)\\s*(?:Rp|IDR)?"),
    )

    // Pola tanggal umum Indonesia - urutkan dari yang paling spesifik
    private val DATE_PATTERNS = listOf(
        Pattern.compile("(?i)(?<!\\d)(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})(?!\\d)"),       // yyyy-mm-dd (prioritaskan 4-digit year, not preceded/followed by digit)
        Pattern.compile("(?i)(?:Tgl|Tanggal|Date)\\s*[:.]?\\s*(\\d{1,2}[-/]\\d{1,2}[-/]\\d{4})"), // dengan kata kunci + 4-digit year
        Pattern.compile("(?i)(?<!\\d)(\\d{1,2}[-/]\\d{1,2}[-/]\\d{4})(?!\\d)"),       // dd/mm/yyyy atau dd-mm-yyyy (4-digit year)
        Pattern.compile("(?i)(?<!\\d)(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2})(?!\\d)"),       // dd/mm/yy atau dd-mm-yy (2-digit year)
        Pattern.compile("(?i)(\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4})"),      // dd MMMM yyyy
    )

    // Kata kunci yang kemungkinan besar BUKAN nama merchant
    private val NON_MERCHANT_KEYWORDS = setOf(
        "total", "subtotal", "grand", "diskon", "potongan", "ppn", "tax", "pajak",
        "bayar", "dibayar", "kembali", "change", "tunai", "cash", "kartu", "card",
        "debit", "kredit", "qris", "gopay", "ovo", "dana", "shopeepay",
        "terima kasih", "thank you", "thanks", "struk", "receipt", "nota",
        "cabang", "branch", "kasir", "cashier", "operator", "waktu", "time",
        "tanggal", "date", "no", "nomor", "invoice", "transaksi", "transaction",
        "item", "barang", "produk", "qty", "jumlah", "harga", "price",
    )

    data class ParsedResult(
        val merchantName: String?,
        val totalAmount: Double?,
        val dateMillis: Long?,
        val rawText: String,
        val confidence: Float
    )

    fun parse(rawText: String): ParsedResult {
        if (rawText.isBlank()) {
            return ParsedResult(null, null, null, rawText, 0f)
        }

        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

        val merchantName = extractMerchantName(lines)
        val totalAmount = extractTotalAmount(rawText, lines)
        val dateMillis = extractDate(rawText, lines)

        // Confidence heuristik
        var confidence = 0.5f
        if (merchantName != null) confidence += 0.15f
        if (totalAmount != null) confidence += 0.2f
        if (dateMillis != null) confidence += 0.1f
        confidence = confidence.coerceIn(0.3f, 0.9f)

        return ParsedResult(
            merchantName = merchantName,
            totalAmount = totalAmount,
            dateMillis = dateMillis,
            rawText = rawText,
            confidence = confidence
        )
    }

    private fun extractMerchantName(lines: List<String>): String? {
        // Ambil 5 baris pertama, cari yang paling mirip nama toko
        val candidates = lines.take(5).filter { line ->
            val lower = line.lowercase(Locale.ROOT)
            // Skip baris yang terlalu pendek atau mengandung kata kunci non-merchant
            line.length >= 3 &&
                    NON_MERCHANT_KEYWORDS.none { lower.contains(it) } &&
                    !line.matches(Regex("^[\\d\\s.,:/\\-]+$")) && // bukan cuma angka/simbol
                    // Skip baris yang mirip item (ada angka harga di akhir)
                    !line.matches(Regex(".*\\d[.,]\\d{3}.*")) // item lines biasanya punya harga
        }

        // Prioritaskan baris paling awal (index terkecil) yang valid
        // Biasanya nama toko ada di baris 1-2
        return candidates.firstOrNull()?.takeIf { it.length <= 60 }
            ?: candidates.maxByOrNull { it.length }?.takeIf { it.length <= 60 }
    }

    private fun extractTotalAmount(rawText: String, lines: List<String>): Double? {
        val amounts = mutableListOf<Pair<Double, Int>>() // Pair<amount, priority>

        // 1. Cari dengan pola currency eksplisit (Rp, Total, dll) - PRIORITAS TINGGI
        for ((index, pattern) in CURRENCY_PATTERNS.withIndex()) {
            val matcher = pattern.matcher(rawText)
            while (matcher.find()) {
                val numStr = matcher.group(1)
                    .replace(",", "")
                    .replace(".", "")
                try {
                    val amount = numStr.toDouble()
                    if (amount > 0 && amount < 100_000_000) {
                        // Prioritas: pola 0 (Rp/IDR explicit) > pola 1 (Total/Grand Total) > pola 2 (Bayar) > pola 3 (angka besar)
                        val priority = when (index) {
                            0 -> 100 // Rp/IDR explicit
                            1 -> 90  // Total/Grand Total/Jumlah/Amount
                            2 -> 80  // Bayar/Dibayar
                            else -> 50 // angka besar generic
                        }
                        amounts.add(amount to priority)
                    }
                } catch (e: NumberFormatException) {
                }
            }
        }

        // 2. Fallback: cari angka di baris yang mengandung kata "total" - PRIORITAS SEDANG
        val totalLines = lines.filter { it.lowercase(Locale.ROOT).contains("total") }
        for (line in totalLines) {
            val numberMatches = Regex("([\\d]{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{1,2})?)").findAll(line)
            for (match in numberMatches) {
                val numStr = match.groupValues[1]
                    .replace(",", "")
                    .replace(".", "")
                try {
                    val amount = numStr.toDouble()
                    if (amount > 0 && amount < 100_000_000) {
                        amounts.add(amount to 70) // "total" keyword line
                    }
                } catch (e: NumberFormatException) {
                }
            }
        }

        // 3. Fallback terakhir: 5 baris terakhir - PRIORITAS RENDAH
        val searchLines = lines.takeLast(5)
        for (line in searchLines) {
            val numberMatches = Regex("([\\d]{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{1,2})?)").findAll(line)
            for (match in numberMatches) {
                val numStr = match.groupValues[1]
                    .replace(",", "")
                    .replace(".", "")
                try {
                    val amount = numStr.toDouble()
                    if (amount > 0 && amount < 100_000_000) {
                        amounts.add(amount to 30) // generic last lines
                    }
                } catch (e: NumberFormatException) {
                }
            }
        }

        // Pilih berdasarkan prioritas tertinggi, lalu nilai terbesar
        return amounts
            .maxByOrNull { it.second } // prioritas tertinggi
            ?.first
            ?: amounts.maxByOrNull { it.first }?.first // fallback: nilai terbesar
    }

    private fun extractDate(rawText: String, lines: List<String>): Long? {
        // Coba pola tanggal eksplisit dulu
        for (pattern in DATE_PATTERNS) {
            val matcher = pattern.matcher(rawText)
            if (matcher.find()) {
                val dateStr = matcher.group(1)
                val parsed = parseDateString(dateStr)
                if (parsed != null) return parsed
            }
        }

        // Fallback: cari di 5 baris pertama dan 5 baris terakhir
        val searchLines = lines.take(5) + lines.takeLast(5)
        for (line in searchLines) {
            for (pattern in DATE_PATTERNS) {
                val matcher = pattern.matcher(line)
                if (matcher.find()) {
                    val parsed = parseDateString(matcher.group(1))
                    if (parsed != null) return parsed
                }
            }
        }

        return null
    }

    private fun parseDateString(dateStr: String): Long? {
        val formatters = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false },
            SimpleDateFormat("yyyy/MM/dd", Locale.ROOT).apply { isLenient = false },
            SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false },
            SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply { isLenient = false },
            SimpleDateFormat("dd/MM/yy", Locale.ROOT).apply { isLenient = false },
            SimpleDateFormat("dd-MM-yy", Locale.ROOT).apply { isLenient = false },
            SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id")).apply { isLenient = false },
            SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id")).apply { isLenient = false },
            SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id")).apply { isLenient = false },
        )

        for (fmt in formatters) {
            try {
                val date: Date = fmt.parse(dateStr.trim())
                return date.time
            } catch (e: Exception) {
                // try next format
            }
        }
        return null
    }
}