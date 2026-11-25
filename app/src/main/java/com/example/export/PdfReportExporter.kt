package com.example.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.domain.CurrencyFormatter
import com.example.domain.DateUtils
import java.io.OutputStream

object PdfReportExporter {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 36f

    fun writeReportPdf(
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
        val document = PdfDocument()
        try {
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(220, 252, 231)
                textSize = 10.5f
            }
            val headerBgPaint = Paint().apply {
                color = Color.rgb(11, 110, 79)
            }
            val boxStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.rgb(203, 213, 225)
                strokeWidth = 1f
            }
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9f
            }
            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val greenPaint = Paint(valuePaint).apply { color = Color.rgb(13, 138, 78) }
            val redPaint = Paint(valuePaint).apply { color = Color.rgb(200, 35, 51) }
            val tableHeaderBgPaint = Paint().apply { color = Color.rgb(241, 245, 249) }
            val tableHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val rowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 8.5f
            }
            val rowCreditPaint = Paint(rowTextPaint).apply {
                color = Color.rgb(13, 138, 78)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val rowDebitPaint = Paint(rowTextPaint).apply {
                color = Color.rgb(200, 35, 51)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val dividerPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 0.7f
            }

            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var canvas = page.canvas

            // Top Banner
            canvas.drawRoundRect(
                RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + 58f),
                8f,
                8f,
                headerBgPaint
            )
            canvas.drawText("MY DAILY HISAB — $reportTitle", MARGIN + 14f, MARGIN + 24f, titlePaint)
            canvas.drawText("Period: $periodLabel  |  Currency: INR (₹)", MARGIN + 14f, MARGIN + 44f, subTitlePaint)

            var y = MARGIN + 72f

            // Summary Cards Row (4 cards across)
            val cardGap = 8f
            val cardWidth = (PAGE_WIDTH - 2 * MARGIN - 3 * cardGap) / 4f
            val cards = listOf(
                Triple("Total Income (Credit)", CurrencyFormatter.formatInr(totalCreditPaise), greenPaint),
                Triple("Total Expense (Debit)", CurrencyFormatter.formatInr(totalDebitPaise), redPaint),
                Triple(
                    if (netProfitPaise >= 0) "Net Profit" else "Net Loss",
                    CurrencyFormatter.formatInr(netProfitPaise),
                    if (netProfitPaise >= 0) greenPaint else redPaint
                ),
                Triple("Current Balance", CurrencyFormatter.formatInr(currentBalancePaise), valuePaint)
            )
            cards.forEachIndexed { idx, (label, valText, paint) ->
                val left = MARGIN + idx * (cardWidth + cardGap)
                val rect = RectF(left, y, left + cardWidth, y + 46f)
                canvas.drawRoundRect(rect, 6f, 6f, boxStrokePaint)
                canvas.drawText(label, left + 8f, y + 16f, labelPaint)
                canvas.drawText(truncateText(valText, 18), left + 8f, y + 34f, paint)
            }

            y += 60f
            canvas.drawText(
                "Opening Balance: ${CurrencyFormatter.formatInr(openingBalancePaise)}   |   Total Entries: ${transactions.size}",
                MARGIN,
                y,
                labelPaint
            )
            y += 14f

            fun drawTableHeader(c: Canvas, topY: Float): Float {
                c.drawRect(MARGIN, topY, PAGE_WIDTH - MARGIN, topY + 22f, tableHeaderBgPaint)
                c.drawText("Date", MARGIN + 6f, topY + 14f, tableHeaderTextPaint)
                c.drawText("Type", MARGIN + 78f, topY + 14f, tableHeaderTextPaint)
                c.drawText("Category", MARGIN + 124f, topY + 14f, tableHeaderTextPaint)
                c.drawText("Party / Method", MARGIN + 220f, topY + 14f, tableHeaderTextPaint)
                c.drawText("Notes", MARGIN + 320f, topY + 14f, tableHeaderTextPaint)
                c.drawText("Amount", PAGE_WIDTH - MARGIN - 72f, topY + 14f, tableHeaderTextPaint)
                return topY + 26f
            }

            y = drawTableHeader(canvas, y)

            if (transactions.isEmpty()) {
                canvas.drawText("No transactions recorded for this period.", MARGIN + 6f, y + 16f, rowTextPaint)
            } else {
                for (tx in transactions) {
                    if (y > PAGE_HEIGHT - MARGIN - 28f) {
                        document.finishPage(page)
                        pageNumber++
                        page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                        canvas = page.canvas
                        y = MARGIN
                        canvas.drawText("MY DAILY HISAB — $periodLabel (Page $pageNumber)", MARGIN, y + 10f, labelPaint)
                        y = drawTableHeader(canvas, y + 18f)
                    }

                    val isCredit = tx.transactionType == TransactionType.CREDIT
                    canvas.drawText(DateUtils.formatShortDate(tx.dateKey), MARGIN + 6f, y + 12f, rowTextPaint)
                    canvas.drawText(
                        if (isCredit) "CREDIT" else "DEBIT",
                        MARGIN + 78f,
                        y + 12f,
                        if (isCredit) rowCreditPaint else rowDebitPaint
                    )
                    canvas.drawText(truncateText(tx.category, 16), MARGIN + 124f, y + 12f, rowTextPaint)
                    canvas.drawText(
                        truncateText(tx.partyOrMethod.ifBlank { "-" }, 16),
                        MARGIN + 220f,
                        y + 12f,
                        rowTextPaint
                    )
                    canvas.drawText(
                        truncateText(tx.description.ifBlank { "-" }, 22),
                        MARGIN + 320f,
                        y + 12f,
                        rowTextPaint
                    )
                    canvas.drawText(
                        CurrencyFormatter.formatInr(tx.amountPaise),
                        PAGE_WIDTH - MARGIN - 72f,
                        y + 12f,
                        if (isCredit) rowCreditPaint else rowDebitPaint
                    )
                    canvas.drawLine(MARGIN, y + 18f, PAGE_WIDTH - MARGIN, y + 18f, dividerPaint)
                    y += 22f
                }
            }

            document.finishPage(page)
            document.writeTo(outputStream)
        } finally {
            document.close()
        }
    }

    private fun truncateText(text: String, maxChars: Int): String =
        if (text.length <= maxChars) text else text.take(maxChars - 1) + "…"
}
