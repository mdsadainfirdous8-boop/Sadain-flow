package com.example.domain

import com.example.data.TransactionEntity
import com.example.data.TransactionType

data class DashboardSummary(
    val totalIncomePaise: Long = 0L,
    val totalExpensesPaise: Long = 0L,
    val totalCreditPaise: Long = 0L,
    val totalDebitPaise: Long = 0L,
    val netProfitPaise: Long = 0L,
    val todayProfitPaise: Long = 0L,
    val todayCreditPaise: Long = 0L,
    val todayDebitPaise: Long = 0L,
    val monthlyProfitPaise: Long = 0L,
    val monthlyCreditPaise: Long = 0L,
    val monthlyDebitPaise: Long = 0L,
    val openingBalancePaise: Long = 0L,
    val currentBalancePaise: Long = 0L,
    val totalCount: Int = 0
)

data class CategoryBreakdown(
    val category: String,
    val amountPaise: Long,
    val type: TransactionType,
    val count: Int,
    val ratio: Float
)

data class GraphBarPoint(
    val label: String,
    val creditPaise: Long,
    val debitPaise: Long
)

data class PeriodReport(
    val title: String,
    val totalCreditPaise: Long,
    val totalDebitPaise: Long,
    val netProfitPaise: Long,
    val transactionCount: Int,
    val creditCount: Int,
    val debitCount: Int,
    val chartPoints: List<GraphBarPoint>,
    val creditCategories: List<CategoryBreakdown>,
    val debitCategories: List<CategoryBreakdown>,
    val transactions: List<TransactionEntity>
)

data class MonthHistorySummary(
    val yearMonth: YearMonthKey,
    val creditPaise: Long,
    val debitPaise: Long,
    val netProfitPaise: Long,
    val count: Int
)

data class CalendarDaySummary(
    val dateKey: String,
    val dayOfMonth: Int,
    val creditPaise: Long,
    val debitPaise: Long,
    val count: Int
) {
    val netPaise: Long get() = creditPaise - debitPaise
}

object FinancialSummaryCalculator {

    fun calculateDashboard(
        transactions: List<TransactionEntity>,
        openingBalancePaise: Long
    ): DashboardSummary {
        val todayKey = DateUtils.todayDateKey()
        val monthPrefix = DateUtils.currentYearMonth().prefix()

        var totalCredit = 0L
        var totalDebit = 0L
        var todayCredit = 0L
        var todayDebit = 0L
        var monthCredit = 0L
        var monthDebit = 0L

        // Single pass over distinct Room rows so no transaction can ever be counted twice
        for (tx in transactions) {
            when (tx.transactionType) {
                TransactionType.CREDIT -> {
                    totalCredit += tx.amountPaise
                    if (tx.dateKey == todayKey) todayCredit += tx.amountPaise
                    if (tx.dateKey.startsWith(monthPrefix)) monthCredit += tx.amountPaise
                }
                TransactionType.DEBIT -> {
                    totalDebit += tx.amountPaise
                    if (tx.dateKey == todayKey) todayDebit += tx.amountPaise
                    if (tx.dateKey.startsWith(monthPrefix)) monthDebit += tx.amountPaise
                }
            }
        }

        return DashboardSummary(
            totalIncomePaise = totalCredit,
            totalExpensesPaise = totalDebit,
            totalCreditPaise = totalCredit,
            totalDebitPaise = totalDebit,
            netProfitPaise = totalCredit - totalDebit,
            todayProfitPaise = todayCredit - todayDebit,
            todayCreditPaise = todayCredit,
            todayDebitPaise = todayDebit,
            monthlyProfitPaise = monthCredit - monthDebit,
            monthlyCreditPaise = monthCredit,
            monthlyDebitPaise = monthDebit,
            openingBalancePaise = openingBalancePaise,
            currentBalancePaise = openingBalancePaise + totalCredit - totalDebit,
            totalCount = transactions.size
        )
    }

    fun buildDailyReport(
        transactions: List<TransactionEntity>,
        dateKey: String
    ): PeriodReport {
        val filtered = transactions.filter { it.dateKey == dateKey }
        val (credit, debit, cCount, dCount) = sumTransactions(filtered)
        val chartPoints = listOf(
            GraphBarPoint(
                label = DateUtils.formatShortDate(dateKey),
                creditPaise = credit,
                debitPaise = debit
            )
        )
        return PeriodReport(
            title = DateUtils.formatDisplayDate(dateKey),
            totalCreditPaise = credit,
            totalDebitPaise = debit,
            netProfitPaise = credit - debit,
            transactionCount = filtered.size,
            creditCount = cCount,
            debitCount = dCount,
            chartPoints = chartPoints,
            creditCategories = buildCategoryBreakdown(filtered, TransactionType.CREDIT, credit),
            debitCategories = buildCategoryBreakdown(filtered, TransactionType.DEBIT, debit),
            transactions = filtered
        )
    }

    fun buildWeeklyReport(
        transactions: List<TransactionEntity>,
        anchorDateKey: String
    ): PeriodReport {
        val weekKeys = DateUtils.weekDateKeys(anchorDateKey)
        val weekSet = weekKeys.toHashSet()
        val filtered = transactions.filter { it.dateKey in weekSet }
        val (credit, debit, cCount, dCount) = sumTransactions(filtered)

        val byDay = filtered.groupBy { it.dateKey }
        val chartPoints = weekKeys.map { key ->
            val dayTxs = byDay[key].orEmpty()
            val (dc, dd) = sumTransactionsPair(dayTxs)
            GraphBarPoint(
                label = DateUtils.dayShortLabel(key),
                creditPaise = dc,
                debitPaise = dd
            )
        }

        val startLabel = weekKeys.firstOrNull()?.let { DateUtils.formatShortDate(it) } ?: ""
        val endLabel = weekKeys.lastOrNull()?.let { DateUtils.formatShortDate(it) } ?: ""

        return PeriodReport(
            title = "$startLabel – $endLabel",
            totalCreditPaise = credit,
            totalDebitPaise = debit,
            netProfitPaise = credit - debit,
            transactionCount = filtered.size,
            creditCount = cCount,
            debitCount = dCount,
            chartPoints = chartPoints,
            creditCategories = buildCategoryBreakdown(filtered, TransactionType.CREDIT, credit),
            debitCategories = buildCategoryBreakdown(filtered, TransactionType.DEBIT, debit),
            transactions = filtered
        )
    }

    fun buildMonthlyReport(
        transactions: List<TransactionEntity>,
        yearMonth: YearMonthKey
    ): PeriodReport {
        val prefix = yearMonth.prefix()
        val filtered = transactions.filter { it.dateKey.startsWith(prefix) }
        val (credit, debit, cCount, dCount) = sumTransactions(filtered)

        // Group month days into 5-day / week buckets or daily bars (use daily bars for crisp month graph)
        val daysCount = DateUtils.daysInMonth(yearMonth)
        val byDay = filtered.groupBy { it.dateKey }
        val chartPoints = (1..daysCount).map { day ->
            val key = DateUtils.formatDateKey(yearMonth.year, yearMonth.month, day)
            val dayTxs = byDay[key].orEmpty()
            val (dc, dd) = sumTransactionsPair(dayTxs)
            GraphBarPoint(
                label = day.toString(),
                creditPaise = dc,
                debitPaise = dd
            )
        }

        return PeriodReport(
            title = yearMonth.displayTitle(),
            totalCreditPaise = credit,
            totalDebitPaise = debit,
            netProfitPaise = credit - debit,
            transactionCount = filtered.size,
            creditCount = cCount,
            debitCount = dCount,
            chartPoints = chartPoints,
            creditCategories = buildCategoryBreakdown(filtered, TransactionType.CREDIT, credit),
            debitCategories = buildCategoryBreakdown(filtered, TransactionType.DEBIT, debit),
            transactions = filtered
        )
    }

    fun buildPreviousMonthsSummaries(
        transactions: List<TransactionEntity>,
        monthsCount: Int = 6
    ): List<MonthHistorySummary> {
        val months = DateUtils.recentMonths(monthsCount)
        return months.map { ym ->
            val prefix = ym.prefix()
            val monthTxs = transactions.filter { it.dateKey.startsWith(prefix) }
            val (credit, debit) = sumTransactionsPair(monthTxs)
            MonthHistorySummary(
                yearMonth = ym,
                creditPaise = credit,
                debitPaise = debit,
                netProfitPaise = credit - debit,
                count = monthTxs.size
            )
        }
    }

    fun buildCalendarMonthMap(
        transactions: List<TransactionEntity>,
        yearMonth: YearMonthKey
    ): Map<String, CalendarDaySummary> {
        val prefix = yearMonth.prefix()
        val monthTxs = transactions.filter { it.dateKey.startsWith(prefix) }
        return monthTxs.groupBy { it.dateKey }.mapValues { (dateKey, dayTxs) ->
            val (credit, debit) = sumTransactionsPair(dayTxs)
            val day = DateUtils.parseDateKey(dateKey)?.third ?: 1
            CalendarDaySummary(
                dateKey = dateKey,
                dayOfMonth = day,
                creditPaise = credit,
                debitPaise = debit,
                count = dayTxs.size
            )
        }
    }

    private fun buildCategoryBreakdown(
        transactions: List<TransactionEntity>,
        type: TransactionType,
        totalForType: Long
    ): List<CategoryBreakdown> {
        if (totalForType <= 0L) return emptyList()
        return transactions
            .filter { it.transactionType == type }
            .groupBy { it.category }
            .map { (category, list) ->
                val sum = list.sumOf { it.amountPaise }
                CategoryBreakdown(
                    category = category,
                    amountPaise = sum,
                    type = type,
                    count = list.size,
                    ratio = (sum.toFloat() / totalForType.toFloat()).coerceIn(0f, 1f)
                )
            }
            .sortedByDescending { it.amountPaise }
    }

    private data class Sums(val credit: Long, val debit: Long, val creditCount: Int, val debitCount: Int)

    private fun sumTransactions(list: List<TransactionEntity>): Sums {
        var credit = 0L
        var debit = 0L
        var cCount = 0
        var dCount = 0
        for (tx in list) {
            if (tx.transactionType == TransactionType.CREDIT) {
                credit += tx.amountPaise
                cCount++
            } else {
                debit += tx.amountPaise
                dCount++
            }
        }
        return Sums(credit, debit, cCount, dCount)
    }

    private fun sumTransactionsPair(list: List<TransactionEntity>): Pair<Long, Long> {
        var credit = 0L
        var debit = 0L
        for (tx in list) {
            if (tx.transactionType == TransactionType.CREDIT) credit += tx.amountPaise
            else debit += tx.amountPaise
        }
        return credit to debit
    }
}
