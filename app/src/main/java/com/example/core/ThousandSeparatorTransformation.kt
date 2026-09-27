package com.example.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation untuk menambahkan separator titik (.) pada input nominal.
 * - Value internal tetap plain (5000)
 * - Display ditampilkan terformat (5.000)
 * - OffsetMapping memastikan kursor tetap pada posisi yang benar saat editing
 */
object ThousandSeparatorTransformation : VisualTransformation {
    
    private fun String.toFormattedRupiah(): String {
        if (this.isBlank()) return ""
        val clean = this.replace(".", "")
        return if (clean.length <= 3) clean else {
            val front = clean.substring(0, clean.length - 3)
            val back = clean.substring(clean.length - 3)
            "${front.toFormattedRupiah()}.${back}"
        }
    }
    
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = text.text.toFormattedRupiah()
        
        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    // Hitung jumlah titik yang disisipkan sebelum posisi offset di original
                    val original = text.text
                    var dotsInserted = 0
                    for (i in 0 until offset) {
                        // Titik disisipkan sebelum posisi i jika:
                        // 1. Jarak dari kanan = (original.length - i) habis dibagi 3
                        // 2. Dan i > 0 (tidak disisipkan di paling kiri)
                        if ((original.length - i) % 3 == 0 && i > 0) {
                            dotsInserted++
                        }
                    }
                    return offset + dotsInserted
                }
                
                override fun transformedToOriginal(offset: Int): Int {
                    // Hitung jumlah titik yang ada sebelum posisi offset di display
                    val formattedStr = formatted
                    var dotsBefore = 0
                    for (i in 0 until offset) {
                        if (i < formattedStr.length && formattedStr[i] == '.') {
                            dotsBefore++
                        }
                    }
                    return offset - dotsBefore
                }
            }
        )
    }
}
