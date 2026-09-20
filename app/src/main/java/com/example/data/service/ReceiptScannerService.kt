package com.example.data.service

/**
 * Arsitektur Layanan Pindai Struk (OCR & AI Receipt Scanning)
 * Disiapkan untuk integrasi Google ML Kit Text Recognition atau Gemini Multimodal Vision API.
 */
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

    override suspend fun scanReceiptOcr(imageBytes: ByteArray): ScannedReceiptResult {
        // Arsitektur offline-ready untuk Google ML Kit OCR
        return ScannedReceiptResult(
            merchantName = "Supermarket Lokal",
            dateMillis = System.currentTimeMillis(),
            totalAmount = 145000.0,
            suggestedCategory = "Makanan & Minuman",
            items = listOf(
                ReceiptItem("Beras Premium 5kg", 1, 75000.0),
                ReceiptItem("Minyak Goreng 2L", 1, 38000.0),
                ReceiptItem("Telur Ayam 1kg", 1, 32000.0)
            ),
            rawText = "STRUK PEMBELIAN\nSUPERMARKET LOKAL\nTOTAL: Rp 145.000",
            confidence = 0.92f
        )
    }

    override suspend fun scanReceiptWithAi(imageBytes: ByteArray, prompt: String?): ScannedReceiptResult {
        // Arsitektur siap dihubungkan ke Gemini API
        return ScannedReceiptResult(
            merchantName = "Restoran Sederhana",
            dateMillis = System.currentTimeMillis(),
            totalAmount = 88000.0,
            suggestedCategory = "Makanan & Minuman",
            items = listOf(
                ReceiptItem("Paket Nasi Rendang Spesial", 1, 55000.0),
                ReceiptItem("Jus Alpukat", 1, 23000.0),
                ReceiptItem("Air Mineral", 1, 10000.0)
            ),
            rawText = "Analisis AI Gemini: Struk makan siang dengan total Rp 88.000",
            confidence = 0.98f
        )
    }

    override fun isAiScannerConfigured(): Boolean {
        return apiKeyConfigService.getGeminiApiKey()?.isNotBlank() == true
    }
}
