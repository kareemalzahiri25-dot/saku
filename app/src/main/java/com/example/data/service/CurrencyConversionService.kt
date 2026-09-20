package com.example.data.service

/**
 * Layanan Konversi Mata Uang Aset (Asset Currency Conversion)
 * Disiapkan untuk konversi aset multi-mata uang (IDR, USD, SGD, EUR, JPY)
 */
data class CurrencyRate(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToIdr: Double // Nilai 1 satuan valas dalam Rupiah
)

interface CurrencyConversionService {
    fun getSupportedCurrencies(): List<CurrencyRate>
    fun convertToIdr(amount: Double, fromCurrencyCode: String): Double
    fun convertFromIdr(amountIdr: Double, toCurrencyCode: String): Double
}

class DefaultCurrencyConversionService : CurrencyConversionService {
    private val rates = mapOf(
        "IDR" to CurrencyRate("IDR", "Rupiah Indonesia", "Rp", 1.0),
        "USD" to CurrencyRate("USD", "Dolar Amerika Serikat", "$", 15850.0),
        "SGD" to CurrencyRate("SGD", "Dolar Singapura", "S$", 11800.0),
        "EUR" to CurrencyRate("EUR", "Euro Uni Eropa", "€", 17200.0),
        "JPY" to CurrencyRate("JPY", "Yen Jepang", "¥", 105.0)
    )

    override fun getSupportedCurrencies(): List<CurrencyRate> {
        return rates.values.toList()
    }

    override fun convertToIdr(amount: Double, fromCurrencyCode: String): Double {
        val rate = rates[fromCurrencyCode.uppercase()]?.rateToIdr ?: 1.0
        return amount * rate
    }

    override fun convertFromIdr(amountIdr: Double, toCurrencyCode: String): Double {
        val rate = rates[toCurrencyCode.uppercase()]?.rateToIdr ?: 1.0
        if (rate == 0.0) return 0.0
        return amountIdr / rate
    }
}
