package com.example

import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.domain.CurrencyFormatter
import com.example.domain.DateUtils
import com.example.domain.FinancialSummaryCalculator
import com.example.domain.YearMonthKey
import com.example.export.ExcelReportExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class HisabDomainUnitTest {

    @Test
    fun currencyFormatter_formatsIndianNumberingCorrectly() {
        assertEquals("₹0.00", CurrencyFormatter.formatInr(0L))
        assertEquals("₹1,250.00", CurrencyFormatter.formatInr(125000L))
        assertEquals("₹1,23,456.78", CurrencyFormatter.formatInr(12345678L))
        assertEquals("-₹500.50", CurrencyFormatter.formatInr(-50050L))
        assertEquals("+₹999.00", CurrencyFormatter.formatInr(99900L, showPlusSign = true))
    }

    @Test
    fun currencyFormatter_parsesValidAndRejectsInvalidInputs() {
        assertEquals(125050L, CurrencyFormatter.parsePositiveAmountToPaise("1,250.50"))
        assertEquals(10000L, CurrencyFormatter.parsePositiveAmountToPaise("₹100"))
        assertNull(CurrencyFormatter.parsePositiveAmountToPaise("0"))
        assertNull(CurrencyFormatter.parsePositiveAmountToPaise("-50"))
        assertNull(CurrencyFormatter.parsePositiveAmountToPaise("10.123"))
        assertNull(CurrencyFormatter.parsePositiveAmountToPaise("abc"))
    }

    @Test
    fun financialSummaryCalculator_computesExactProfitAndBalanceWithoutDoubleCounting() {
        val today = DateUtils.todayDateKey()
        val ym = DateUtils.currentYearMonth()
        val prevYm = ym.previousMonth()

        val txs = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.CREDIT.name,
                amountPaise = 500000L, // ₹5,000
                dateKey = today,
                timeText = "10:00",
                timestampMillis = 1L,
                category = "Sales"
            ),
            TransactionEntity(
                id = 2,
                type = TransactionType.DEBIT.name,
                amountPaise = 150000L, // ₹1,500
                dateKey = today,
                timeText = "12:00",
                timestampMillis = 2L,
                category = "Food"
            ),
            TransactionEntity(
                id = 3,
                type = TransactionType.CREDIT.name,
                amountPaise = 200000L, // ₹2,000 previous month
                dateKey = DateUtils.formatDateKey(prevYm.year, prevYm.month, 10),
                timeText = "14:00",
                timestampMillis = 3L,
                category = "Business Income"
            )
        )

        val summary = FinancialSummaryCalculator.calculateDashboard(
            transactions = txs,
            openingBalancePaise = 1000000L // ₹10,000
        )

        assertEquals(700000L, summary.totalIncomePaise)
        assertEquals(700000L, summary.totalCreditPaise)
        assertEquals(150000L, summary.totalExpensesPaise)
        assertEquals(150000L, summary.totalDebitPaise)
        assertEquals(550000L, summary.netProfitPaise)
        assertEquals(350000L, summary.todayProfitPaise)
        assertEquals(350000L, summary.monthlyProfitPaise)
        assertEquals(1550000L, summary.currentBalancePaise) // 10,000 + 7,000 - 1,500
    }

    @Test
    fun dateUtils_utcRoundTripAndWeekKeysAreConsistent() {
        val key = "2026-11-25"
        val utcMillis = DateUtils.utcPickerMillisFromDateKey(key)
        assertEquals(key, DateUtils.dateKeyFromUtcPickerMillis(utcMillis))

        val week = DateUtils.weekDateKeys(key)
        assertEquals(7, week.size)
        assertEquals("2026-11-23", week.first()) // Monday
        assertEquals("2026-11-29", week.last()) // Sunday
    }

    @Test
    fun excelExporter_producesValidXlsxZipWithWorksheet() {
        val out = ByteArrayOutputStream()
        val txs = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.CREDIT.name,
                amountPaise = 250000L,
                dateKey = "2026-11-25",
                timeText = "09:30",
                timestampMillis = 1L,
                category = "Sales",
                partyOrMethod = "A & B Traders",
                description = "Invoice <1>"
            )
        )

        ExcelReportExporter.writeReportXlsx(
            outputStream = out,
            reportTitle = "Monthly Report",
            periodLabel = YearMonthKey(2026, 11).displayTitle(),
            totalCreditPaise = 250000L,
            totalDebitPaise = 0L,
            netProfitPaise = 250000L,
            openingBalancePaise = 0L,
            currentBalancePaise = 250000L,
            transactions = txs
        )

        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(out.toByteArray())).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                entry = zip.nextEntry
            }
        }

        assertTrue(entries.containsKey("[Content_Types].xml"))
        assertTrue(entries.containsKey("xl/workbook.xml"))
        val sheet = entries["xl/worksheets/sheet1.xml"] ?: ""
        assertTrue(sheet.contains("A &amp; B Traders"))
        assertTrue(sheet.contains("Invoice &lt;1&gt;"))
        assertTrue(sheet.contains("<v>2500.00</v>"))
    }
}
