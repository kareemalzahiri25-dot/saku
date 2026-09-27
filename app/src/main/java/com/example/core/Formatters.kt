package com.example.core

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val indonesianLocale = Locale("id", "ID")

    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(indonesianLocale)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    fun formatRupiah(amount: Long): String {
        return formatRupiah(amount.toDouble())
    }

    fun formatDateIndo(dateMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", indonesianLocale)
        return sdf.format(Date(dateMillis))
    }

    fun formatShortDateIndo(dateMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
        return sdf.format(Date(dateMillis))
    }

    fun formatTimeIndo(dateMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", indonesianLocale)
        return sdf.format(Date(dateMillis))
    }

    fun parseAmount(input: String): Double {
        val clean = input.filter { it.isDigit() }
        return clean.toDoubleOrNull() ?: 0.0
    }

    /**
     * Format string angka plain ke format ribuan Indonesia (titik sebagai separator).
     * Contoh: "5000" → "5.000", "1000000" → "1.000.000"
     */
    fun String.toFormattedRupiah(): String {
        if (this.isBlank()) return ""
        val clean = this.replace(".", "")
        return if (clean.length <= 3) clean else {
            val (front, back) = clean.substring(0, clean.length - 3) to clean.substring(clean.length - 3)
            "${front.toFormattedRupiah()}.${back}"
        }
    }
}
