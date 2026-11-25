package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TransactionCategories
import com.example.data.TransactionEntity
import com.example.domain.CalendarDaySummary
import com.example.domain.CurrencyFormatter
import com.example.domain.DateUtils
import com.example.domain.MonthHistorySummary
import com.example.domain.PeriodReport
import com.example.domain.YearMonthKey
import com.example.ui.HistoryTypeFilter
import com.example.ui.ReportSubTab
import com.example.ui.components.CategoryBreakdownCard
import com.example.ui.components.CreditVsDebitComparisonCard
import com.example.ui.components.FinancialBarChartCard
import com.example.ui.components.HisabDatePickerDialog
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.AmountCardStyle
import com.example.ui.theme.AmountListStyle
import com.example.ui.theme.LocalHisabColors

enum class ExportFormat(val label: String, val extension: String) {
    PDF("PDF Statement", "pdf"),
    EXCEL("Excel Workbook (.xlsx)", "xlsx")
}

@Composable
fun ReportsScreen(
    activeSubTab: ReportSubTab,
    onSelectSubTab: (ReportSubTab) -> Unit,
    selectedMonth: YearMonthKey,
    onSelectMonth: (YearMonthKey) -> Unit,
    onShiftMonth: (Int) -> Unit,
    monthlyReport: PeriodReport,
    previousMonthsSummaries: List<MonthHistorySummary>,
    weeklyReport: PeriodReport,
    onShiftWeek: (Int) -> Unit,
    dailyReport: PeriodReport,
    selectedDay: String,
    onSelectDay: (String) -> Unit,
    onShiftDay: (Int) -> Unit,
    calendarMonth: YearMonthKey,
    onShiftCalendarMonth: (Int) -> Unit,
    calendarMonthMap: Map<String, CalendarDaySummary>,
    calendarSelectedDay: String,
    onSelectCalendarDay: (String) -> Unit,
    calendarDayTransactions: List<TransactionEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    historyTypeFilter: HistoryTypeFilter,
    onHistoryTypeFilterChange: (HistoryTypeFilter) -> Unit,
    historyCategoryFilter: String?,
    onHistoryCategoryFilterChange: (String?) -> Unit,
    historyDateFilter: String?,
    onHistoryDateFilterChange: (String?) -> Unit,
    filteredHistory: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onExportSaveToDevice: (ExportFormat) -> Unit,
    onExportShare: (ExportFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingExportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header + Export Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reports & Ledger",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { pendingExportFormat = ExportFormat.PDF },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("export_pdf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PictureAsPdf,
                                    contentDescription = "Export PDF",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", style = MaterialTheme.typography.labelMedium)
                            }

                            FilledTonalButton(
                                onClick = { pendingExportFormat = ExportFormat.EXCEL },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("export_excel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TableChart,
                                    contentDescription = "Export Excel",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excel", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    // Sub-Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportSubTab.entries.forEach { tab ->
                            FilterChip(
                                selected = activeSubTab == tab,
                                onClick = { onSelectSubTab(tab) },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("report_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            when (activeSubTab) {
                ReportSubTab.MONTHLY -> {
                    item {
                        MonthlyReportSection(
                            selectedMonth = selectedMonth,
                            onSelectMonth = onSelectMonth,
                            onShiftMonth = onShiftMonth,
                            report = monthlyReport,
                            previousMonthsSummaries = previousMonthsSummaries
                        )
                    }
                    if (monthlyReport.transactions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Month Transactions (${monthlyReport.transactionCount})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(monthlyReport.transactions, key = { it.id }) { tx ->
                            TransactionItemCard(
                                transaction = tx,
                                onClick = { onEditTransaction(tx) },
                                onDeleteClick = { onDeleteTransaction(tx.id) }
                            )
                        }
                    }
                }

                ReportSubTab.WEEKLY -> {
                    item {
                        WeeklyReportSection(
                            report = weeklyReport,
                            onShiftWeek = onShiftWeek
                        )
                    }
                    if (weeklyReport.transactions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Week Transactions (${weeklyReport.transactionCount})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(weeklyReport.transactions, key = { it.id }) { tx ->
                            TransactionItemCard(
                                transaction = tx,
                                onClick = { onEditTransaction(tx) },
                                onDeleteClick = { onDeleteTransaction(tx.id) }
                            )
                        }
                    }
                }

                ReportSubTab.DAILY -> {
                    item {
                        DailyReportSection(
                            selectedDay = selectedDay,
                            onSelectDay = onSelectDay,
                            onShiftDay = onShiftDay,
                            report = dailyReport
                        )
                    }
                    if (dailyReport.transactions.isEmpty()) {
                        item {
                            EmptyReportCard("No transactions recorded for ${DateUtils.formatDisplayDate(selectedDay)}.")
                        }
                    } else {
                        items(dailyReport.transactions, key = { it.id }) { tx ->
                            TransactionItemCard(
                                transaction = tx,
                                onClick = { onEditTransaction(tx) },
                                onDeleteClick = { onDeleteTransaction(tx.id) }
                            )
                        }
                    }
                }

                ReportSubTab.CALENDAR -> {
                    item {
                        CalendarSection(
                            calendarMonth = calendarMonth,
                            onShiftCalendarMonth = onShiftCalendarMonth,
                            monthMap = calendarMonthMap,
                            selectedDay = calendarSelectedDay,
                            onSelectDay = onSelectCalendarDay
                        )
                    }
                    if (calendarDayTransactions.isEmpty()) {
                        item {
                            EmptyReportCard("No transactions on ${DateUtils.formatDisplayDate(calendarSelectedDay)}.")
                        }
                    } else {
                        items(calendarDayTransactions, key = { it.id }) { tx ->
                            TransactionItemCard(
                                transaction = tx,
                                onClick = { onEditTransaction(tx) },
                                onDeleteClick = { onDeleteTransaction(tx.id) }
                            )
                        }
                    }
                }

                ReportSubTab.HISTORY -> {
                    item {
                        HistoryFilterSection(
                            searchQuery = searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            typeFilter = historyTypeFilter,
                            onTypeFilterChange = onHistoryTypeFilterChange,
                            categoryFilter = historyCategoryFilter,
                            onCategoryFilterChange = onHistoryCategoryFilterChange,
                            dateFilter = historyDateFilter,
                            onDateFilterChange = onHistoryDateFilterChange,
                            filteredCount = filteredHistory.size
                        )
                    }
                    if (filteredHistory.isEmpty()) {
                        item {
                            EmptyReportCard("No transactions match your current search or filter.")
                        }
                    } else {
                        items(filteredHistory, key = { it.id }) { tx ->
                            TransactionItemCard(
                                transaction = tx,
                                onClick = { onEditTransaction(tx) },
                                onDeleteClick = { onDeleteTransaction(tx.id) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (pendingExportFormat != null) {
        val fmt = pendingExportFormat!!
        AlertDialog(
            onDismissRequest = { pendingExportFormat = null },
            title = { Text("Export ${fmt.label}") },
            text = {
                Text("Choose whether to save the ${fmt.label} file to your device storage or share it directly with another app.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingExportFormat = null
                        onExportSaveToDevice(fmt)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save File")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingExportFormat = null
                        onExportShare(fmt)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        )
    }
}

@Composable
private fun MonthlyReportSection(
    selectedMonth: YearMonthKey,
    onSelectMonth: (YearMonthKey) -> Unit,
    onShiftMonth: (Int) -> Unit,
    report: PeriodReport,
    previousMonthsSummaries: List<MonthHistorySummary>
) {
    var showMonthYearPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PeriodNavigatorRow(
            title = selectedMonth.displayTitle(),
            onPrevious = { onShiftMonth(-1) },
            onNext = { onShiftMonth(1) },
            onTitleClick = { showMonthYearPicker = true }
        )

        PeriodSummaryRow(report = report)

        CreditVsDebitComparisonCard(
            creditPaise = report.totalCreditPaise,
            debitPaise = report.totalDebitPaise
        )

        FinancialBarChartCard(
            title = "Monthly Financial Graph (${selectedMonth.shortTitle()})",
            points = report.chartPoints,
            isMonthly = true
        )

        if (report.creditCategories.isNotEmpty()) {
            CategoryBreakdownCard(
                title = "Income by Category",
                items = report.creditCategories,
                isCredit = true
            )
        }

        if (report.debitCategories.isNotEmpty()) {
            CategoryBreakdownCard(
                title = "Expenses by Category",
                items = report.debitCategories,
                isCredit = false
            )
        }

        // Previous Months Records
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Previous Months' Records",
                    style = MaterialTheme.typography.titleMedium
                )
                previousMonthsSummaries.forEach { item ->
                    val hisabColors = LocalHisabColors.current
                    val profitColor = if (item.netProfitPaise >= 0) hisabColors.credit else hisabColors.debit
                    val isSelected = item.yearMonth == selectedMonth

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onSelectMonth(item.yearMonth) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.yearMonth.displayTitle(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "In: ${CurrencyFormatter.formatCompactInr(item.creditPaise)} • Out: ${CurrencyFormatter.formatCompactInr(item.debitPaise)} (${item.count})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatInr(item.netProfitPaise, showPlusSign = true),
                                    style = AmountListStyle,
                                    color = profitColor
                                )
                                Text(
                                    text = if (item.netProfitPaise >= 0) "Net Profit" else "Net Loss",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = profitColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            initial = selectedMonth,
            onConfirm = {
                onSelectMonth(it)
                showMonthYearPicker = false
            },
            onDismiss = { showMonthYearPicker = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthYearPickerDialog(
    initial: YearMonthKey,
    onConfirm: (YearMonthKey) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.month) }
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month & Year") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Previous year")
                    }
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Next year")
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    monthNames.forEachIndexed { idx, label ->
                        val m = idx + 1
                        FilterChip(
                            selected = month == m,
                            onClick = { month = m },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(YearMonthKey(year, month)) }) {
                Text("View Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WeeklyReportSection(
    report: PeriodReport,
    onShiftWeek: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PeriodNavigatorRow(
            title = report.title,
            onPrevious = { onShiftWeek(-1) },
            onNext = { onShiftWeek(1) },
            onTitleClick = null
        )

        PeriodSummaryRow(report = report)

        CreditVsDebitComparisonCard(
            creditPaise = report.totalCreditPaise,
            debitPaise = report.totalDebitPaise
        )

        FinancialBarChartCard(
            title = "Weekly Cash Flow (Mon–Sun)",
            points = report.chartPoints,
            isMonthly = false
        )
    }
}

@Composable
private fun DailyReportSection(
    selectedDay: String,
    onSelectDay: (String) -> Unit,
    onShiftDay: (Int) -> Unit,
    report: PeriodReport
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PeriodNavigatorRow(
            title = report.title,
            onPrevious = { onShiftDay(-1) },
            onNext = { onShiftDay(1) },
            onTitleClick = { showDatePicker = true }
        )

        PeriodSummaryRow(report = report)

        if (report.creditCategories.isNotEmpty()) {
            CategoryBreakdownCard(
                title = "Today's Income Categories",
                items = report.creditCategories,
                isCredit = true
            )
        }
        if (report.debitCategories.isNotEmpty()) {
            CategoryBreakdownCard(
                title = "Today's Expense Categories",
                items = report.debitCategories,
                isCredit = false
            )
        }
    }

    if (showDatePicker) {
        HisabDatePickerDialog(
            initialDateKey = selectedDay,
            onDateSelected = onSelectDay,
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun CalendarSection(
    calendarMonth: YearMonthKey,
    onShiftCalendarMonth: (Int) -> Unit,
    monthMap: Map<String, CalendarDaySummary>,
    selectedDay: String,
    onSelectDay: (String) -> Unit
) {
    val hisabColors = LocalHisabColors.current
    val daysCount = DateUtils.daysInMonth(calendarMonth)
    val startOffset = DateUtils.firstDayOfWeekOffsetMondayStart(calendarMonth)
    val totalCells = startOffset + daysCount
    val rows = (totalCells + 6) / 7
    val selectedSummary = monthMap[selectedDay]

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PeriodNavigatorRow(
            title = calendarMonth.displayTitle(),
            onPrevious = { onShiftCalendarMonth(-1) },
            onNext = { onShiftCalendarMonth(1) },
            onTitleClick = null
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (c in 0 until 7) {
                            val cellIdx = r * 7 + c
                            val dayNum = cellIdx - startOffset + 1
                            if (dayNum in 1..daysCount) {
                                val key = DateUtils.formatDateKey(calendarMonth.year, calendarMonth.month, dayNum)
                                val summary = monthMap[key]
                                val isSelected = key == selectedDay
                                val isToday = key == DateUtils.todayDateKey()

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    1.5.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.shapes.small
                                                )
                                            } else Modifier
                                        )
                                        .clickable { onSelectDay(key) }
                                        .testTag("calendar_day_$dayNum"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        if (summary != null) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                if (summary.creditPaise > 0L) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(hisabColors.credit)
                                                    )
                                                }
                                                if (summary.debitPaise > 0L) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(hisabColors.debit)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }

        // Selected day summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.formatDisplayDate(selectedDay),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${selectedSummary?.count ?: 0} transaction(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Credit", style = MaterialTheme.typography.labelSmall, color = hisabColors.credit)
                        Text(
                            text = CurrencyFormatter.formatCompactInr(selectedSummary?.creditPaise ?: 0L),
                            style = AmountListStyle,
                            color = hisabColors.credit
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Debit", style = MaterialTheme.typography.labelSmall, color = hisabColors.debit)
                        Text(
                            text = CurrencyFormatter.formatCompactInr(selectedSummary?.debitPaise ?: 0L),
                            style = AmountListStyle,
                            color = hisabColors.debit
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    typeFilter: HistoryTypeFilter,
    onTypeFilterChange: (HistoryTypeFilter) -> Unit,
    categoryFilter: String?,
    onCategoryFilterChange: (String?) -> Unit,
    dateFilter: String?,
    onDateFilterChange: (String?) -> Unit,
    filteredCount: Int
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val availableCategories = remember(typeFilter) {
        when (typeFilter) {
            HistoryTypeFilter.ALL -> TransactionCategories.creditCategories + TransactionCategories.debitCategories
            HistoryTypeFilter.CREDIT -> TransactionCategories.creditCategories
            HistoryTypeFilter.DEBIT -> TransactionCategories.debitCategories
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search description, customer, category, amount") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.cd_search)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.cd_clear_search)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistoryTypeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = typeFilter == filter,
                        onClick = {
                            onTypeFilterChange(filter)
                            onCategoryFilterChange(null)
                        },
                        label = { Text(filter.label) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFilter?.let { DateUtils.formatShortDate(it) } ?: "By Date",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (dateFilter != null) {
                    IconButton(
                        onClick = { onDateFilterChange(null) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear date filter",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Category filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = categoryFilter == null,
                    onClick = { onCategoryFilterChange(null) },
                    label = { Text("All Categories") }
                )
                availableCategories.forEach { cat ->
                    FilterChip(
                        selected = categoryFilter == cat,
                        onClick = { onCategoryFilterChange(if (categoryFilter == cat) null else cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Text(
                text = "$filteredCount matching transaction(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDatePicker) {
        HisabDatePickerDialog(
            initialDateKey = dateFilter ?: DateUtils.todayDateKey(),
            onDateSelected = onDateFilterChange,
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun PeriodNavigatorRow(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: (() -> Unit)?
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.cd_previous_period)
                )
            }

            val titleModifier = if (onTitleClick != null) {
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTitleClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            } else {
                Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            }

            Row(
                modifier = titleModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onTitleClick != null) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.cd_next_period)
                )
            }
        }
    }
}

@Composable
private fun PeriodSummaryRow(report: PeriodReport) {
    val hisabColors = LocalHisabColors.current
    val isProfit = report.netProfitPaise >= 0L
    val profitColor = if (isProfit) hisabColors.credit else hisabColors.debit

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactReportBox(
            label = "Income",
            value = CurrencyFormatter.formatCompactInr(report.totalCreditPaise),
            valueColor = hisabColors.credit,
            modifier = Modifier.weight(1f)
        )
        CompactReportBox(
            label = "Expenses",
            value = CurrencyFormatter.formatCompactInr(report.totalDebitPaise),
            valueColor = hisabColors.debit,
            modifier = Modifier.weight(1f)
        )
        CompactReportBox(
            label = if (isProfit) "Net Profit" else "Net Loss",
            value = CurrencyFormatter.formatInr(report.netProfitPaise, showPlusSign = true),
            valueColor = profitColor,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun CompactReportBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = AmountCardStyle,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyReportCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
    }
}
