package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.HisabRepository
import com.example.data.HisabSettings
import com.example.data.ThemeMode
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.domain.CalendarDaySummary
import com.example.domain.CurrencyFormatter
import com.example.domain.DashboardSummary
import com.example.domain.DateUtils
import com.example.domain.FinancialSummaryCalculator
import com.example.domain.MonthHistorySummary
import com.example.domain.PeriodReport
import com.example.domain.YearMonthKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReportSubTab(val label: String) {
    MONTHLY("Monthly"),
    WEEKLY("Weekly"),
    DAILY("Daily"),
    CALENDAR("Calendar"),
    HISTORY("History")
}

enum class HistoryTypeFilter(val label: String) {
    ALL("All"),
    CREDIT("Credit"),
    DEBIT("Debit")
}

data class PendingTransactionInput(
    val type: TransactionType,
    val amountPaise: Long,
    val dateKey: String,
    val timeText: String,
    val category: String,
    val partyOrMethod: String,
    val description: String
)

data class ExportPayload(
    val title: String,
    val periodLabel: String,
    val totalCreditPaise: Long,
    val totalDebitPaise: Long,
    val netProfitPaise: Long,
    val openingBalancePaise: Long,
    val currentBalancePaise: Long,
    val transactions: List<TransactionEntity>
)

sealed interface HisabUiEvent {
    data class ShowMessage(val message: String) : HisabUiEvent
    data class DuplicateDetected(val pending: PendingTransactionInput, val existingCount: Int) : HisabUiEvent
}

class HisabViewModel(
    private val repository: HisabRepository
) : ViewModel() {

    val settings: StateFlow<HisabSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HisabSettings()
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.activeTransactionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _reportSubTab = MutableStateFlow(ReportSubTab.MONTHLY)
    val reportSubTab: StateFlow<ReportSubTab> = _reportSubTab.asStateFlow()

    private val _selectedMonth = MutableStateFlow(DateUtils.currentYearMonth())
    val selectedMonth: StateFlow<YearMonthKey> = _selectedMonth.asStateFlow()

    private val _selectedWeekAnchor = MutableStateFlow(DateUtils.todayDateKey())
    val selectedWeekAnchor: StateFlow<String> = _selectedWeekAnchor.asStateFlow()

    private val _selectedDay = MutableStateFlow(DateUtils.todayDateKey())
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    private val _calendarMonth = MutableStateFlow(DateUtils.currentYearMonth())
    val calendarMonth: StateFlow<YearMonthKey> = _calendarMonth.asStateFlow()

    private val _calendarSelectedDay = MutableStateFlow(DateUtils.todayDateKey())
    val calendarSelectedDay: StateFlow<String> = _calendarSelectedDay.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _historyTypeFilter = MutableStateFlow(HistoryTypeFilter.ALL)
    val historyTypeFilter: StateFlow<HistoryTypeFilter> = _historyTypeFilter.asStateFlow()

    private val _historyCategoryFilter = MutableStateFlow<String?>(null)
    val historyCategoryFilter: StateFlow<String?> = _historyCategoryFilter.asStateFlow()

    private val _historyDateFilter = MutableStateFlow<String?>(null)
    val historyDateFilter: StateFlow<String?> = _historyDateFilter.asStateFlow()

    private val _uiEvents = MutableSharedFlow<HisabUiEvent>(extraBufferCapacity = 8)
    val uiEvents: SharedFlow<HisabUiEvent> = _uiEvents.asSharedFlow()

    private var lastSubmittedKey: String? = null
    private var lastSubmittedTimeMs: Long = 0L

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        transactions,
        settings
    ) { txList, cfg ->
        FinancialSummaryCalculator.calculateDashboard(txList, cfg.openingBalancePaise)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardSummary()
    )

    val monthlyReport: StateFlow<PeriodReport> = combine(
        transactions,
        selectedMonth
    ) { txList, ym ->
        FinancialSummaryCalculator.buildMonthlyReport(txList, ym)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = FinancialSummaryCalculator.buildMonthlyReport(emptyList(), DateUtils.currentYearMonth())
    )

    val weeklyReport: StateFlow<PeriodReport> = combine(
        transactions,
        selectedWeekAnchor
    ) { txList, anchor ->
        FinancialSummaryCalculator.buildWeeklyReport(txList, anchor)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = FinancialSummaryCalculator.buildWeeklyReport(emptyList(), DateUtils.todayDateKey())
    )

    val dailyReport: StateFlow<PeriodReport> = combine(
        transactions,
        selectedDay
    ) { txList, day ->
        FinancialSummaryCalculator.buildDailyReport(txList, day)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = FinancialSummaryCalculator.buildDailyReport(emptyList(), DateUtils.todayDateKey())
    )

    val previousMonthsSummaries: StateFlow<List<MonthHistorySummary>> = combine(
        transactions,
        selectedMonth
    ) { txList, _ ->
        FinancialSummaryCalculator.buildPreviousMonthsSummaries(txList, monthsCount = 6)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val calendarMonthMap: StateFlow<Map<String, CalendarDaySummary>> = combine(
        transactions,
        calendarMonth
    ) { txList, ym ->
        FinancialSummaryCalculator.buildCalendarMonthMap(txList, ym)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyMap()
    )

    val calendarDayTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        calendarSelectedDay
    ) { txList, day ->
        txList.filter { it.dateKey == day }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val filteredHistory: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        searchQuery,
        historyTypeFilter,
        historyCategoryFilter,
        historyDateFilter
    ) { txList, query, typeFilter, categoryFilter, dateFilter ->
        val q = query.trim().lowercase()
        txList.filter { tx ->
            val typeOk = when (typeFilter) {
                HistoryTypeFilter.ALL -> true
                HistoryTypeFilter.CREDIT -> tx.transactionType == TransactionType.CREDIT
                HistoryTypeFilter.DEBIT -> tx.transactionType == TransactionType.DEBIT
            }
            val categoryOk = categoryFilter == null || tx.category == categoryFilter
            val dateOk = dateFilter == null || tx.dateKey == dateFilter
            val queryOk = q.isEmpty() ||
                tx.category.lowercase().contains(q) ||
                tx.description.lowercase().contains(q) ||
                tx.partyOrMethod.lowercase().contains(q) ||
                DateUtils.formatDisplayDate(tx.dateKey).lowercase().contains(q) ||
                CurrencyFormatter.formatEditableAmount(tx.amountPaise).contains(q)
            typeOk && categoryOk && dateOk && queryOk
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            val initialSettings = repository.settingsFlow.first()
            if (!initialSettings.isPinEnabled) {
                _isUnlocked.value = true
            }
        }
    }

    fun tryUnlockPin(pin: String): Boolean {
        val ok = repository.verifyPin(pin, settings.value)
        if (ok) _isUnlocked.value = true
        return ok
    }

    fun lockAppIfEnabled() {
        if (settings.value.isPinEnabled) {
            _isUnlocked.value = false
        }
    }

    fun setReportSubTab(tab: ReportSubTab) {
        _reportSubTab.value = tab
    }

    fun setSelectedMonth(ym: YearMonthKey) {
        _selectedMonth.value = ym
    }

    fun shiftSelectedMonth(delta: Int) {
        _selectedMonth.value = if (delta < 0) _selectedMonth.value.previousMonth() else _selectedMonth.value.nextMonth()
    }

    fun shiftSelectedWeek(deltaWeeks: Int) {
        _selectedWeekAnchor.value = DateUtils.addDays(_selectedWeekAnchor.value, deltaWeeks * 7)
    }

    fun setSelectedDay(dateKey: String) {
        _selectedDay.value = dateKey
    }

    fun shiftSelectedDay(deltaDays: Int) {
        _selectedDay.value = DateUtils.addDays(_selectedDay.value, deltaDays)
    }

    fun shiftCalendarMonth(delta: Int) {
        _calendarMonth.value = if (delta < 0) _calendarMonth.value.previousMonth() else _calendarMonth.value.nextMonth()
    }

    fun setCalendarSelectedDay(dateKey: String) {
        _calendarSelectedDay.value = dateKey
        DateUtils.parseDateKey(dateKey)?.let { (y, m, _) ->
            _calendarMonth.value = YearMonthKey(y, m)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setHistoryTypeFilter(filter: HistoryTypeFilter) {
        _historyTypeFilter.value = filter
    }

    fun setHistoryCategoryFilter(category: String?) {
        _historyCategoryFilter.value = category
    }

    fun setHistoryDateFilter(dateKey: String?) {
        _historyDateFilter.value = dateKey
    }

    fun submitTransaction(input: PendingTransactionInput, forceSave: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val rapidKey = "${input.type}|${input.amountPaise}|${input.dateKey}|${input.timeText}|${input.category}|${input.description}"
            if (!forceSave && rapidKey == lastSubmittedKey && (now - lastSubmittedTimeMs) < 3500L) {
                _uiEvents.tryEmit(HisabUiEvent.ShowMessage("Prevented accidental double tap"))
                return@launch
            }

            val isDemo = settings.value.isDemoMode
            if (!forceSave) {
                val matches = repository.findPotentialDuplicates(
                    type = input.type,
                    amountPaise = input.amountPaise,
                    dateKey = input.dateKey,
                    category = input.category,
                    isDemo = isDemo
                )
                if (matches.isNotEmpty()) {
                    _uiEvents.tryEmit(HisabUiEvent.DuplicateDetected(input, matches.size))
                    return@launch
                }
            }

            lastSubmittedKey = rapidKey
            lastSubmittedTimeMs = now

            repository.addTransaction(
                type = input.type,
                amountPaise = input.amountPaise,
                dateKey = input.dateKey,
                timeText = input.timeText,
                category = input.category,
                partyOrMethod = input.partyOrMethod,
                description = input.description,
                isDemo = isDemo
            )
            val label = if (input.type == TransactionType.CREDIT) "Credit" else "Debit"
            _uiEvents.tryEmit(
                HisabUiEvent.ShowMessage(
                    "$label of ${CurrencyFormatter.formatInr(input.amountPaise)} saved"
                )
            )
        }
    }

    fun updateTransaction(updated: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(updated)
            _uiEvents.tryEmit(HisabUiEvent.ShowMessage("Transaction updated"))
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _uiEvents.tryEmit(HisabUiEvent.ShowMessage("Transaction deleted"))
        }
    }

    fun setOpeningBalancePaise(paise: Long) {
        viewModelScope.launch {
            repository.setOpeningBalancePaise(paise)
            _uiEvents.tryEmit(HisabUiEvent.ShowMessage("Opening balance updated"))
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun enablePin(pin: String) {
        viewModelScope.launch {
            repository.enablePin(pin)
            _isUnlocked.value = true
            _uiEvents.tryEmit(HisabUiEvent.ShowMessage("App PIN lock enabled"))
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            repository.disablePin()
            _isUnlocked.value = true
            _uiEvents.tryEmit(HisabUiEvent.ShowMessage("App PIN lock disabled"))
        }
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDemoMode(enabled)
            _uiEvents.tryEmit(
                HisabUiEvent.ShowMessage(
                    if (enabled) "Demo mode enabled with sample data" else "Demo mode turned off"
                )
            )
        }
    }

    fun clearAllRealData() {
        viewModelScope.launch {
            repository.clearAllRealData()
            _uiEvents.tryEmit(HisabUiEvent.ShowMessage("All saved ledger data cleared"))
        }
    }

    suspend fun generateBackupJson(): String {
        return repository.exportBackupJson(settings.value.openingBalancePaise)
    }

    fun restoreBackupJson(jsonString: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            try {
                val count = repository.restoreBackupJson(jsonString, replaceExisting)
                _uiEvents.tryEmit(HisabUiEvent.ShowMessage("Restored $count transaction(s) from backup"))
            } catch (e: Exception) {
                _uiEvents.tryEmit(HisabUiEvent.ShowMessage("Restore failed: ${e.message ?: "Invalid file"}"))
            }
        }
    }

    fun buildActiveExportPayload(): ExportPayload {
        val dash = dashboardSummary.value
        return when (reportSubTab.value) {
            ReportSubTab.MONTHLY -> {
                val r = monthlyReport.value
                ExportPayload(
                    title = "Monthly Report",
                    periodLabel = r.title,
                    totalCreditPaise = r.totalCreditPaise,
                    totalDebitPaise = r.totalDebitPaise,
                    netProfitPaise = r.netProfitPaise,
                    openingBalancePaise = dash.openingBalancePaise,
                    currentBalancePaise = dash.currentBalancePaise,
                    transactions = r.transactions
                )
            }
            ReportSubTab.WEEKLY -> {
                val r = weeklyReport.value
                ExportPayload(
                    title = "Weekly Report",
                    periodLabel = r.title,
                    totalCreditPaise = r.totalCreditPaise,
                    totalDebitPaise = r.totalDebitPaise,
                    netProfitPaise = r.netProfitPaise,
                    openingBalancePaise = dash.openingBalancePaise,
                    currentBalancePaise = dash.currentBalancePaise,
                    transactions = r.transactions
                )
            }
            ReportSubTab.DAILY -> {
                val r = dailyReport.value
                ExportPayload(
                    title = "Daily Report",
                    periodLabel = r.title,
                    totalCreditPaise = r.totalCreditPaise,
                    totalDebitPaise = r.totalDebitPaise,
                    netProfitPaise = r.netProfitPaise,
                    openingBalancePaise = dash.openingBalancePaise,
                    currentBalancePaise = dash.currentBalancePaise,
                    transactions = r.transactions
                )
            }
            ReportSubTab.CALENDAR -> {
                val r = FinancialSummaryCalculator.buildDailyReport(transactions.value, calendarSelectedDay.value)
                ExportPayload(
                    title = "Day Ledger Report",
                    periodLabel = r.title,
                    totalCreditPaise = r.totalCreditPaise,
                    totalDebitPaise = r.totalDebitPaise,
                    netProfitPaise = r.netProfitPaise,
                    openingBalancePaise = dash.openingBalancePaise,
                    currentBalancePaise = dash.currentBalancePaise,
                    transactions = r.transactions
                )
            }
            ReportSubTab.HISTORY -> {
                val list = filteredHistory.value
                val credit = list.filter { it.transactionType == TransactionType.CREDIT }.sumOf { it.amountPaise }
                val debit = list.filter { it.transactionType == TransactionType.DEBIT }.sumOf { it.amountPaise }
                ExportPayload(
                    title = "Transaction History",
                    periodLabel = historyDateFilter.value?.let { DateUtils.formatDisplayDate(it) } ?: "Filtered Records",
                    totalCreditPaise = credit,
                    totalDebitPaise = debit,
                    netProfitPaise = credit - debit,
                    openingBalancePaise = dash.openingBalancePaise,
                    currentBalancePaise = dash.currentBalancePaise,
                    transactions = list
                )
            }
        }
    }

    class Factory(private val repository: HisabRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HisabViewModel(repository) as T
        }
    }
}
