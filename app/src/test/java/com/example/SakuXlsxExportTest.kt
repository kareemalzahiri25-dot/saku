package com.example

import com.example.data.export.SimpleXlsxWriter
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertTrue
import org.junit.Test

class SakuXlsxExportTest {

    private fun buildSampleXlsx(): ByteArrayOutputStream {
        val transactions = listOf(
            Transaction(
                id = "tx1",
                title = "Gaji <Bulan> & Insentif",
                amount = 5000000.0,
                type = TransactionType.INCOME,
                categoryId = "c1",
                categoryName = "Gaji",
                categoryIcon = "payments",
                assetId = "a1",
                assetName = "BCA",
                dateMillis = 1759200000000L,
                note = "catatan"
            ),
            Transaction(
                id = "tx2",
                title = "Belanja Groceries",
                amount = 150000.0,
                type = TransactionType.EXPENSE,
                categoryId = "c2",
                categoryName = "Belanja",
                categoryIcon = "cart",
                assetId = "a1",
                assetName = "BCA",
                dateMillis = 1759113600000L,
                note = ""
            ),
            Transaction(
                id = "tx3",
                title = "Transfer Tabungan",
                amount = 1000000.0,
                type = TransactionType.TRANSFER,
                categoryId = "c3",
                categoryName = "Transfer",
                categoryIcon = "swap",
                assetId = "a1",
                assetName = "BCA",
                targetAssetId = "a2",
                targetAssetName = "Mandiri",
                dateMillis = 1759027200000L,
                note = ""
            )
        )
        val out = ByteArrayOutputStream()
        SimpleXlsxWriter.write(
            outputStream = out,
            periodTitle = "September 2026",
            totalIncome = 5000000.0,
            totalExpense = 150000.0,
            totalTransfer = 1000000.0,
            totalBalance = 5850000.0,
            transactions = transactions
        )
        return out
    }

    @Test
    fun xlsxHasValidZipSignature() {
        val bytes = buildSampleXlsx().toByteArray()
        assertTrue("File tidak kosong", bytes.isNotEmpty())
        assertTrue("Signature harus PK", bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte())
    }

    @Test
    fun xlsxContainsRequiredOoxmlParts() {
        val bytes = buildSampleXlsx().toByteArray()
        val entries = mutableSetOf<String>()
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        val required = listOf(
            "[Content_Types].xml",
            "_rels/.rels",
            "xl/workbook.xml",
            "xl/_rels/workbook.xml.rels",
            "xl/worksheets/sheet1.xml",
            "xl/worksheets/sheet2.xml",
            "xl/styles.xml"
        )
        for (r in required) {
            assertTrue("Missing: $r", entries.contains(r))
        }
    }

    @Test
    fun workbookDeclaresBothSheetsAndDataIsCorrect() {
        val bytes = buildSampleXlsx().toByteArray()
        val parts = mutableMapOf<String, String>()
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                parts[entry.name] = zis.readBytes().decodeToString()
                entry = zis.nextEntry
            }
        }

        val workbook = parts["xl/workbook.xml"] ?: ""
        assertTrue("Sheet Ringkasan harus ada", workbook.contains("Ringkasan"))
        assertTrue("Sheet Transaksi harus ada", workbook.contains("Transaksi"))

        val sheet1 = parts["xl/worksheets/sheet1.xml"] ?: ""
        assertTrue("Periode harus ada", sheet1.contains("September 2026"))
        assertTrue("Pemasukan harus ada", sheet1.contains("Pemasukan"))
        assertTrue("Pengeluaran harus ada", sheet1.contains("Pengeluaran"))
        assertTrue("Transfer harus ada", sheet1.contains("Transfer"))
        assertTrue("Nilai pemasukan 5000000.0", sheet1.contains("5000000.0"))
        assertTrue("Nilai saldo 5850000.0", sheet1.contains("5850000.0"))

        val sheet2 = parts["xl/worksheets/sheet2.xml"] ?: ""
        assertTrue("Header Title", sheet2.contains("Title"))
        assertTrue("Header Category", sheet2.contains("Category"))
        assertTrue("Header Amount", sheet2.contains("Amount"))
        assertTrue("Header Date", sheet2.contains("Date"))
        assertTrue("Header AssetName", sheet2.contains("AssetName"))
        assertTrue("Judul transaksi (XML-escaped)", sheet2.contains("Gaji &lt;Bulan&gt; &amp; Insentif"))
        assertTrue("Transfer source→target", sheet2.contains("BCA → Mandiri"))
        assertTrue("Jumlah transaksi netral (transfer tidak negatif)", sheet2.contains("<v>1000000.0</v>"))
    }
}
