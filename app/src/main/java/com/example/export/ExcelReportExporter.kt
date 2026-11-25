package com.example.export

import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.domain.CurrencyFormatter
import com.example.domain.DateUtils
import java.io.OutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Generates a native OpenXML .xlsx spreadsheet using ZipOutputStream so it opens directly
 * in Microsoft Excel, Google Sheets, and WPS Office with typed numeric amounts.
 */
object ExcelReportExporter {

    fun writeReportXlsx(
        outputStream: OutputStream,
        reportTitle: String,
        periodLabel: String,
        totalCreditPaise: Long,
        totalDebitPaise: Long,
        netProfitPaise: Long,
        openingBalancePaise: Long,
        currentBalancePaise: Long,
        transactions: List<TransactionEntity>
    ) {
        val sheetXml = buildWorksheetXml(
            reportTitle = reportTitle,
            periodLabel = periodLabel,
            totalCreditPaise = totalCreditPaise,
            totalDebitPaise = totalDebitPaise,
            netProfitPaise = netProfitPaise,
            openingBalancePaise = openingBalancePaise,
            currentBalancePaise = currentBalancePaise,
            transactions = transactions
        )

        ZipOutputStream(outputStream).use { zip ->
            writeZipEntry(zip, "[Content_Types].xml", CONTENT_TYPES_XML)
            writeZipEntry(zip, "_rels/.rels", ROOT_RELS_XML)
            writeZipEntry(zip, "xl/workbook.xml", WORKBOOK_XML)
            writeZipEntry(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
            writeZipEntry(zip, "xl/styles.xml", STYLES_XML)
            writeZipEntry(zip, "xl/worksheets/sheet1.xml", sheetXml)
        }
    }

    private fun writeZipEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildWorksheetXml(
        reportTitle: String,
        periodLabel: String,
        totalCreditPaise: Long,
        totalDebitPaise: Long,
        netProfitPaise: Long,
        openingBalancePaise: Long,
        currentBalancePaise: Long,
        transactions: List<TransactionEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("""<cols><col min="1" max="1" width="14" customWidth="1"/><col min="2" max="2" width="11" customWidth="1"/><col min="3" max="3" width="10" customWidth="1"/><col min="4" max="4" width="20" customWidth="1"/><col min="5" max="5" width="22" customWidth="1"/><col min="6" max="6" width="32" customWidth="1"/><col min="7" max="7" width="16" customWidth="1"/></cols>""")
        sb.append("<sheetData>")

        var rowIdx = 1
        fun appendStringRow(values: List<String>, styleId: Int = 0) {
            sb.append("""<row r="$rowIdx">""")
            values.forEachIndexed { colIdx, text ->
                val ref = "${colLetter(colIdx)}$rowIdx"
                sb.append("""<c r="$ref" t="inlineStr" s="$styleId"><is><t>${escapeXml(text)}</t></is></c>""")
            }
            sb.append("</row>")
            rowIdx++
        }

        appendStringRow(listOf("MY DAILY HISAB - $reportTitle", "Period: $periodLabel"), styleId = 1)
        appendStringRow(listOf("Total Income (Credit)", CurrencyFormatter.formatInr(totalCreditPaise), "Total Expense (Debit)", CurrencyFormatter.formatInr(totalDebitPaise)))
        appendStringRow(listOf(if (netProfitPaise >= 0) "Net Profit" else "Net Loss", CurrencyFormatter.formatInr(netProfitPaise), "Opening Balance", CurrencyFormatter.formatInr(openingBalancePaise), "Current Balance", CurrencyFormatter.formatInr(currentBalancePaise)))
        rowIdx++ // blank spacer row

        appendStringRow(listOf("Date", "Time", "Type", "Category", "Party / Payment Method", "Description", "Amount (INR)"), styleId = 1)

        for (tx in transactions) {
            val isCredit = tx.transactionType == TransactionType.CREDIT
            val rupees = String.format(Locale.US, "%.2f", tx.amountPaise / 100.0)
            sb.append("""<row r="$rowIdx">""")
            val textCols = listOf(
                DateUtils.formatDisplayDate(tx.dateKey),
                DateUtils.formatDisplayTime(tx.timeText),
                if (isCredit) "Credit" else "Debit",
                tx.category,
                tx.partyOrMethod.ifBlank { "-" },
                tx.description.ifBlank { "-" }
            )
            textCols.forEachIndexed { colIdx, text ->
                val ref = "${colLetter(colIdx)}$rowIdx"
                sb.append("""<c r="$ref" t="inlineStr"><is><t>${escapeXml(text)}</t></is></c>""")
            }
            val amountRef = "${colLetter(6)}$rowIdx"
            sb.append("""<c r="$amountRef"><v>$rupees</v></c>""")
            sb.append("</row>")
            rowIdx++
        }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun colLetter(index: Int): String = ('A'.code + index).toChar().toString()

    private fun escapeXml(input: String): String =
        input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    private const val CONTENT_TYPES_XML =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private const val ROOT_RELS_XML =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private const val WORKBOOK_XML =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Hisab Report" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

    private const val WORKBOOK_RELS_XML =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private const val STYLES_XML =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="2">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><name val="Calibri"/></font>
  </fonts>
  <fills count="2">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
  </fills>
  <borders count="1">
    <border><left/><right/><top/><bottom/><diagonal/></border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
  </cellXfs>
</styleSheet>"""
}
