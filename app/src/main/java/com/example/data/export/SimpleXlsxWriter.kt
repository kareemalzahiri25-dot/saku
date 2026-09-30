package com.example.data.export

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.core.Formatters

/**
 * Minimal OOXML XLSX writer untuk Android API 24+
 * Tanpa dependency POI, menggunakan java.util.zip + manual XML generation
 */
object SimpleXlsxWriter {
    
    fun write(
        outputStream: OutputStream,
        periodTitle: String,
        totalIncome: Double,
        totalExpense: Double,
        totalTransfer: Double,
        totalBalance: Double,
        transactions: List<Transaction>
    ) {
        ZipOutputStream(outputStream).use { zos ->
            // 1. [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write(generateContentTypes().toByteArray())
            zos.closeEntry()
            
            // 2. _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write(generateRels().toByteArray())
            zos.closeEntry()
            
            // 3. xl/workbook.xml
            zos.putNextEntry(ZipEntry("xl/workbook.xml"))
            zos.write(generateWorkbook().toByteArray())
            zos.closeEntry()
            
            // 4. xl/_rels/workbook.xml.rels
            zos.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zos.write(generateWorkbookRels().toByteArray())
            zos.closeEntry()
            
            // 5. xl/worksheets/sheet1.xml (Ringkasan)
            zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zos.write(generateRingkasanSheet(periodTitle, totalIncome, totalExpense, totalTransfer, totalBalance).toByteArray())
            zos.closeEntry()
            
            // 6. xl/worksheets/sheet2.xml (Transaksi)
            zos.putNextEntry(ZipEntry("xl/worksheets/sheet2.xml"))
            zos.write(generateTransaksiSheet(transactions).toByteArray())
            zos.closeEntry()
            
            // 7. xl/styles.xml
            zos.putNextEntry(ZipEntry("xl/styles.xml"))
            zos.write(generateStyles().toByteArray())
            zos.closeEntry()
        }
    }
    
    private fun escapeXml(text: String?): String {
        if (text == null) return ""
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
    
    private fun generateContentTypes(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>
"""
    
    private fun generateRels(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>
"""
    
    private fun generateWorkbook(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
        <sheet name="Ringkasan" sheetId="1" r:id="rId2"/>
        <sheet name="Transaksi" sheetId="2" r:id="rId3"/>
    </sheets>
</workbook>
"""
    
    private fun generateWorkbookRels(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
    <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>
"""
    
    private fun generateRingkasanSheet(
        periodTitle: String,
        totalIncome: Double,
        totalExpense: Double,
        totalTransfer: Double,
        totalBalance: Double
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
""")
        
        var rowNum = 1
        
        // Header
        sb.append("""        <row r="$rowNum">
            <c r="A$rowNum" t="inlineStr"><is><t>LAPORAN SAKU</t></is></c>
        </row>
""")
        rowNum++
        
        // Empty row
        rowNum++
        
        // Periode
        sb.append("""        <row r="$rowNum">
            <c r="A$rowNum" t="inlineStr"><is><t>Periode</t></is></c>
            <c r="B$rowNum" t="inlineStr"><is><t>${escapeXml(periodTitle)}</t></is></c>
        </row>
""")
        rowNum++
        
        // Empty row
        rowNum++
        
        // Pemasukan
        sb.append("""        <row r="$rowNum">
            <c r="A$rowNum" t="inlineStr"><is><t>Pemasukan</t></is></c>
            <c r="B$rowNum"><v>$totalIncome</v></c>
        </row>
""")
        rowNum++
        
        // Pengeluaran
        sb.append("""        <row r="$rowNum">
            <c r="A$rowNum" t="inlineStr"><is><t>Pengeluaran</t></is></c>
            <c r="B$rowNum"><v>$totalExpense</v></c>
        </row>
""")
        rowNum++
        
        // Transfer
        sb.append("""        <row r="$rowNum">
            <c r="A$rowNum" t="inlineStr"><is><t>Transfer</t></is></c>
            <c r="B$rowNum"><v>$totalTransfer</v></c>
        </row>
""")
        rowNum++
        
        // Saldo
        sb.append("""        <row r="$rowNum">
            <c r="A$rowNum" t="inlineStr"><is><t>Saldo</t></is></c>
            <c r="B$rowNum"><v>$totalBalance</v></c>
        </row>
""")
        
        sb.append("""    </sheetData>
</worksheet>
""")
        return sb.toString()
    }
    
    private fun generateTransaksiSheet(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <sheetData>
""")
        
        var rowNum = 1
        
        // Header
        sb.append("""        <row r="$rowNum">
            <c r="A$rowNum" t="inlineStr"><is><t>Title</t></is></c>
            <c r="B$rowNum" t="inlineStr"><is><t>Category</t></is></c>
            <c r="C$rowNum" t="inlineStr"><is><t>Amount</t></is></c>
            <c r="D$rowNum" t="inlineStr"><is><t>Date</t></is></c>
            <c r="E$rowNum" t="inlineStr"><is><t>AssetName</t></is></c>
        </row>
""")
        rowNum++
        
        // Data rows
        for (tx in transactions) {
            val title = escapeXml(tx.title)
            val category = escapeXml(tx.categoryName)
            val amount = tx.amount
            val date = Formatters.formatShortDateIndo(tx.dateMillis)
            val assetName = if (tx.type == TransactionType.TRANSFER) {
                "${escapeXml(tx.assetName)} → ${escapeXml(tx.targetAssetName ?: "-")}"
            } else {
                escapeXml(tx.assetName)
            }
            
            sb.append("""        <row r="$rowNum">
                <c r="A$rowNum" t="inlineStr"><is><t>$title</t></is></c>
                <c r="B$rowNum" t="inlineStr"><is><t>$category</t></is></c>
                <c r="C$rowNum"><v>$amount</v></c>
                <c r="D$rowNum" t="inlineStr"><is><t>$date</t></is></c>
                <c r="E$rowNum" t="inlineStr"><is><t>$assetName</t></is></c>
            </row>
""")
            rowNum++
        }
        
        sb.append("""    </sheetData>
</worksheet>
""")
        return sb.toString()
    }
    
    private fun generateStyles(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <fonts>
        <font><sz val="11"/><name val="Calibri"/></font>
    </fonts>
    <fills>
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
    </fills>
    <borders>
        <border><left/><right/><top/><bottom/><diagonal/></border>
    </borders>
    <cellStyleXfs>
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
    </cellStyleXfs>
    <cellXfs>
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    </cellXfs>
</styleSheet>
"""
}
