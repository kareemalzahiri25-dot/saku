package com.example

import com.example.data.export.SimpleXlsxWriter
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream

fun main() {
    println("=== XLSX Structural Validation Test ===\n")
    
    // 1. Create test transactions
    val testTransactions = listOf(
        Transaction(
            id = "tx1",
            title = "Gaji Bulan Ini",
            amount = 5000000.0,
            type = TransactionType.INCOME,
            categoryId = "cat1",
            categoryName = "Gaji",
            categoryIcon = "payments",
            assetId = "asset1",
            assetName = "BCA",
            dateMillis = System.currentTimeMillis(),
            note = "Gaji bulanan"
        ),
        Transaction(
            id = "tx2",
            title = "Belanja Groceries",
            amount = 150000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat2",
            categoryName = "Belanja",
            categoryIcon = "shopping_cart",
            assetId = "asset1",
            assetName = "BCA",
            dateMillis = System.currentTimeMillis(),
            note = "Belanja minggu ini"
        ),
        Transaction(
            id = "tx3",
            title = "Transfer ke Tabungan",
            amount = 1000000.0,
            type = TransactionType.TRANSFER,
            categoryId = "cat_transfer",
            categoryName = "Transfer",
            categoryIcon = "swap_horiz",
            assetId = "asset1",
            assetName = "BCA",
            targetAssetId = "asset2",
            targetAssetName = "Mandiri",
            dateMillis = System.currentTimeMillis(),
            note = "Transfer tabungan"
        )
    )
    
    // 2. Generate XLSX to ByteArray
    val outputStream = ByteArrayOutputStream()
    SimpleXlsxWriter.write(
        outputStream = outputStream,
        periodTitle = "September 2026",
        totalIncome = 5000000.0,
        totalExpense = 150000.0,
        totalTransfer = 1000000.0,
        totalBalance = 5850000.0,
        transactions = testTransactions
    )
    
    val xlsxBytes = outputStream.toByteArray()
    println("✓ Generated XLSX: ${xlsxBytes.size} bytes")
    
    // 3. Validate ZIP signature
    if (xlsxBytes.size >= 2) {
        val sig = byteArrayOf(xlsxBytes[0], xlsxBytes[1])
        val isZip = sig[0] == 0x50.toByte() && sig[1] == 0x4B.toByte() // "PK"
        println("✓ ZIP Signature: ${if (isZip) "VALID (PK)" else "INVALID"}")
    }
    
    // 4. Extract and list ZIP contents
    println("\n✓ OOXML Files:")
    val requiredFiles = listOf(
        "[Content_Types].xml",
        "_rels/.rels",
        "xl/workbook.xml",
        "xl/_rels/workbook.xml.rels",
        "xl/worksheets/sheet1.xml",
        "xl/worksheets/sheet2.xml",
        "xl/styles.xml"
    )
    
    val foundFiles = mutableSetOf<String>()
    try {
        val zipInput = ZipInputStream(outputStream.toByteArray().inputStream())
        var entry = zipInput.nextEntry
        while (entry != null) {
            foundFiles.add(entry.name)
            println("  - ${entry.name}")
            entry = zipInput.nextEntry
        }
        zipInput.close()
    } catch (e: Exception) {
        println("  ERROR reading ZIP: ${e.message}")
    }
    
    // 5. Verify required files
    println("\n✓ Required Files Check:")
    var allPresent = true
    for (req in requiredFiles) {
        val present = foundFiles.contains(req)
        println("  ${if (present) "✓" else "✗"} $req")
        if (!present) allPresent = false
    }
    
    // 6. Save to disk for manual inspection
    val testFile = File("/tmp/test_laporan.xlsx")
    testFile.writeBytes(xlsxBytes)
    println("\n✓ Saved to: ${testFile.absolutePath}")
    
    // Final verdict
    println("\n" + (if (allPresent) "✓ XLSX VALID" else "✗ XLSX INVALID"))
    println("Structural validation: ${if (allPresent && xlsxBytes.size > 0) "PASS" else "FAIL"}")
}
