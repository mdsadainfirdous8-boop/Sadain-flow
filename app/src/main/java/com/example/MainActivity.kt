package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.HisabDatabase
import com.example.data.HisabRepository
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.data.UserPreferences
import com.example.domain.DateUtils
import com.example.export.ExcelReportExporter
import com.example.export.PdfReportExporter
import com.example.ui.ExportPayload
import com.example.ui.HisabUiEvent
import com.example.ui.HisabViewModel
import com.example.ui.PendingTransactionInput
import com.example.ui.ReportSubTab
import com.example.ui.components.DuplicateWarningDialog
import com.example.ui.components.TransactionEditSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportFormat
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionEntryScreen
import com.example.ui.theme.LocalHisabColors
import com.example.ui.theme.MyDailyHisabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val ROUTE_HOME = "home"
private const val ROUTE_CREDIT = "credit"
private const val ROUTE_DEBIT = "debit"
private const val ROUTE_REPORTS = "reports"
private const val ROUTE_SETTINGS = "settings"

private data class BottomNavTab(
    val route: String,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = HisabDatabase.getInstance(applicationContext)
        val preferences = UserPreferences(applicationContext)
        val repository = HisabRepository(database.transactionDao(), preferences)

        setContent {
            val viewModel: HisabViewModel = viewModel(
                factory = HisabViewModel.Factory(repository)
            )
            MyDailyHisabApp(viewModel = viewModel)
        }
    }
}

@Composable
fun MyDailyHisabApp(viewModel: HisabViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val reportSubTab by viewModel.reportSubTab.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthlyReport by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val previousMonths by viewModel.previousMonthsSummaries.collectAsStateWithLifecycle()
    val weeklyReport by viewModel.weeklyReport.collectAsStateWithLifecycle()
    val dailyReport by viewModel.dailyReport.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val calendarMonthMap by viewModel.calendarMonthMap.collectAsStateWithLifecycle()
    val calendarSelectedDay by viewModel.calendarSelectedDay.collectAsStateWithLifecycle()
    val calendarDayTransactions by viewModel.calendarDayTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val historyTypeFilter by viewModel.historyTypeFilter.collectAsStateWithLifecycle()
    val historyCategoryFilter by viewModel.historyCategoryFilter.collectAsStateWithLifecycle()
    val historyDateFilter by viewModel.historyDateFilter.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredHistory.collectAsStateWithLifecycle()

    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var duplicateDialogState by remember { mutableStateOf<Pair<PendingTransactionInput, Int>?>(null) }
    var pendingExportPayload by remember { mutableStateOf<ExportPayload?>(null) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    // Event listener
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is HisabUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is HisabUiEvent.DuplicateDetected -> {
                    duplicateDialogState = event.pending to event.existingCount
                }
            }
        }
    }

    // SAF Launchers for PDF, Excel, Backup Export & Restore
    val pdfCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        val payload = pendingExportPayload ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            PdfReportExporter.writeReportPdf(
                                outputStream = out,
                                reportTitle = payload.title,
                                periodLabel = payload.periodLabel,
                                totalCreditPaise = payload.totalCreditPaise,
                                totalDebitPaise = payload.totalDebitPaise,
                                netProfitPaise = payload.netProfitPaise,
                                openingBalancePaise = payload.openingBalancePaise,
                                currentBalancePaise = payload.currentBalancePaise,
                                transactions = payload.transactions
                            )
                        } ?: error("Unable to open output file")
                    }
                }
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "PDF report saved successfully"
                    else "PDF export failed: ${result.exceptionOrNull()?.localizedMessage ?: "Error"}"
                )
            }
        }
    }

    val excelCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        val payload = pendingExportPayload ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            ExcelReportExporter.writeReportXlsx(
                                outputStream = out,
                                reportTitle = payload.title,
                                periodLabel = payload.periodLabel,
                                totalCreditPaise = payload.totalCreditPaise,
                                totalDebitPaise = payload.totalDebitPaise,
                                netProfitPaise = payload.netProfitPaise,
                                openingBalancePaise = payload.openingBalancePaise,
                                currentBalancePaise = payload.currentBalancePaise,
                                transactions = payload.transactions
                            )
                        } ?: error("Unable to open output file")
                    }
                }
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "Excel (.xlsx) report saved successfully"
                    else "Excel export failed: ${result.exceptionOrNull()?.localizedMessage ?: "Error"}"
                )
            }
        }
    }

    val backupCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    val json = viewModel.generateBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        } ?: error("Unable to write backup")
                    }
                }
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "Backup saved successfully"
                    else "Backup failed: ${result.exceptionOrNull()?.localizedMessage ?: "Error"}"
                )
            }
        }
    }

    val backupRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val readResult = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                            ?: error("Cannot read selected file")
                    }
                }
                if (readResult.isSuccess) {
                    pendingRestoreJson = readResult.getOrNull()
                } else {
                    snackbarHostState.showSnackbar("Failed to read backup file")
                }
            }
        }
    }

    fun shareExportedFile(format: ExportFormat, payload: ExportPayload) {
        scope.launch {
            val result = runCatching {
                val file = withContext(Dispatchers.IO) {
                    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                    val safeStamp = DateUtils.todayDateKey()
                    val fileName = "MyDailyHisab_${safeStamp}.${format.extension}"
                    val target = File(exportDir, fileName)
                    FileOutputStream(target).use { out ->
                        when (format) {
                            ExportFormat.PDF -> PdfReportExporter.writeReportPdf(
                                outputStream = out,
                                reportTitle = payload.title,
                                periodLabel = payload.periodLabel,
                                totalCreditPaise = payload.totalCreditPaise,
                                totalDebitPaise = payload.totalDebitPaise,
                                netProfitPaise = payload.netProfitPaise,
                                openingBalancePaise = payload.openingBalancePaise,
                                currentBalancePaise = payload.currentBalancePaise,
                                transactions = payload.transactions
                            )
                            ExportFormat.EXCEL -> ExcelReportExporter.writeReportXlsx(
                                outputStream = out,
                                reportTitle = payload.title,
                                periodLabel = payload.periodLabel,
                                totalCreditPaise = payload.totalCreditPaise,
                                totalDebitPaise = payload.totalDebitPaise,
                                netProfitPaise = payload.netProfitPaise,
                                openingBalancePaise = payload.openingBalancePaise,
                                currentBalancePaise = payload.currentBalancePaise,
                                transactions = payload.transactions
                            )
                        }
                    }
                    target
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val mimeType = when (format) {
                    ExportFormat.PDF -> "application/pdf"
                    ExportFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, "My Daily Hisab - ${payload.title}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share ${format.label}"))
            }

            if (result.isFailure) {
                snackbarHostState.showSnackbar("Share failed: ${result.exceptionOrNull()?.localizedMessage ?: "Error"}")
            }
        }
    }

    MyDailyHisabTheme(themeMode = settings.themeMode) {
        if (settings.isPinEnabled && !isUnlocked) {
            PinLockScreen(onVerifyPin = { viewModel.tryUnlockPin(it) })
            return@MyDailyHisabTheme
        }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME
        val hisabColors = LocalHisabColors.current

        val tabs = remember {
            listOf(
                BottomNavTab(ROUTE_HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
                BottomNavTab(ROUTE_CREDIT, R.string.nav_credit, Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp),
                BottomNavTab(ROUTE_DEBIT, R.string.nav_debit, Icons.AutoMirrored.Filled.TrendingDown, Icons.AutoMirrored.Outlined.TrendingDown),
                BottomNavTab(ROUTE_REPORTS, R.string.nav_reports, Icons.Filled.BarChart, Icons.Outlined.BarChart),
                BottomNavTab(ROUTE_SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
            )
        }

        fun navigateTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 840.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                val label = stringResource(tab.labelResId)
                                val iconTint = when {
                                    selected && tab.route == ROUTE_CREDIT -> hisabColors.credit
                                    selected && tab.route == ROUTE_DEBIT -> hisabColors.debit
                                    selected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label,
                                            tint = iconTint
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("nav_tab_${tab.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                val label = stringResource(tab.labelResId)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("rail_tab_${tab.route}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_HOME
                        ) {
                            composable(ROUTE_HOME) {
                                DashboardScreen(
                                    summary = dashboardSummary,
                                    recentTransactions = transactions,
                                    isDemoMode = settings.isDemoMode,
                                    onAddCreditClick = { navigateTab(ROUTE_CREDIT) },
                                    onAddDebitClick = { navigateTab(ROUTE_DEBIT) },
                                    onOpenHistoryClick = {
                                        viewModel.setReportSubTab(ReportSubTab.HISTORY)
                                        navigateTab(ROUTE_REPORTS)
                                    },
                                    onOpenCalendarClick = {
                                        viewModel.setReportSubTab(ReportSubTab.CALENDAR)
                                        navigateTab(ROUTE_REPORTS)
                                    },
                                    onEditTransaction = { editingTransaction = it },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                    onDisableDemoMode = { viewModel.setDemoMode(false) }
                                )
                            }

                            composable(ROUTE_CREDIT) {
                                TransactionEntryScreen(
                                    type = TransactionType.CREDIT,
                                    allTransactions = transactions,
                                    onSubmit = { viewModel.submitTransaction(it) },
                                    onEditTransaction = { editingTransaction = it },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                                )
                            }

                            composable(ROUTE_DEBIT) {
                                TransactionEntryScreen(
                                    type = TransactionType.DEBIT,
                                    allTransactions = transactions,
                                    onSubmit = { viewModel.submitTransaction(it) },
                                    onEditTransaction = { editingTransaction = it },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                                )
                            }

                            composable(ROUTE_REPORTS) {
                                ReportsScreen(
                                    activeSubTab = reportSubTab,
                                    onSelectSubTab = { viewModel.setReportSubTab(it) },
                                    selectedMonth = selectedMonth,
                                    onSelectMonth = { viewModel.setSelectedMonth(it) },
                                    onShiftMonth = { viewModel.shiftSelectedMonth(it) },
                                    monthlyReport = monthlyReport,
                                    previousMonthsSummaries = previousMonths,
                                    weeklyReport = weeklyReport,
                                    onShiftWeek = { viewModel.shiftSelectedWeek(it) },
                                    dailyReport = dailyReport,
                                    selectedDay = selectedDay,
                                    onSelectDay = { viewModel.setSelectedDay(it) },
                                    onShiftDay = { viewModel.shiftSelectedDay(it) },
                                    calendarMonth = calendarMonth,
                                    onShiftCalendarMonth = { viewModel.shiftCalendarMonth(it) },
                                    calendarMonthMap = calendarMonthMap,
                                    calendarSelectedDay = calendarSelectedDay,
                                    onSelectCalendarDay = { viewModel.setCalendarSelectedDay(it) },
                                    calendarDayTransactions = calendarDayTransactions,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    historyTypeFilter = historyTypeFilter,
                                    onHistoryTypeFilterChange = { viewModel.setHistoryTypeFilter(it) },
                                    historyCategoryFilter = historyCategoryFilter,
                                    onHistoryCategoryFilterChange = { viewModel.setHistoryCategoryFilter(it) },
                                    historyDateFilter = historyDateFilter,
                                    onHistoryDateFilterChange = { viewModel.setHistoryDateFilter(it) },
                                    filteredHistory = filteredHistory,
                                    onEditTransaction = { editingTransaction = it },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                    onExportSaveToDevice = { format ->
                                        val payload = viewModel.buildActiveExportPayload()
                                        pendingExportPayload = payload
                                        val fileName = "MyDailyHisab_${DateUtils.todayDateKey()}.${format.extension}"
                                        when (format) {
                                            ExportFormat.PDF -> pdfCreateLauncher.launch(fileName)
                                            ExportFormat.EXCEL -> excelCreateLauncher.launch(fileName)
                                        }
                                    },
                                    onExportShare = { format ->
                                        val payload = viewModel.buildActiveExportPayload()
                                        shareExportedFile(format, payload)
                                    }
                                )
                            }

                            composable(ROUTE_SETTINGS) {
                                SettingsScreen(
                                    settings = settings,
                                    onUpdateOpeningBalance = { viewModel.setOpeningBalancePaise(it) },
                                    onSelectThemeMode = { viewModel.setThemeMode(it) },
                                    onEnablePin = { viewModel.enablePin(it) },
                                    onDisablePin = { viewModel.disablePin() },
                                    onExportBackup = {
                                        backupCreateLauncher.launch("MyDailyHisab_Backup_${DateUtils.todayDateKey()}.json")
                                    },
                                    onRestoreBackup = {
                                        backupRestoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                    },
                                    onToggleDemoMode = { viewModel.setDemoMode(it) },
                                    onClearAllRealData = { viewModel.clearAllRealData() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Duplicate Entry Confirmation
        duplicateDialogState?.let { (pending, existingCount) ->
            DuplicateWarningDialog(
                pending = pending,
                existingCount = existingCount,
                onConfirmSave = {
                    duplicateDialogState = null
                    viewModel.submitTransaction(pending, forceSave = true)
                },
                onDismiss = { duplicateDialogState = null }
            )
        }

        // Transaction Edit Bottom Sheet
        editingTransaction?.let { tx ->
            TransactionEditSheet(
                transaction = tx,
                onSave = { updated ->
                    viewModel.updateTransaction(updated)
                    editingTransaction = null
                },
                onDelete = { id ->
                    viewModel.deleteTransaction(id)
                    editingTransaction = null
                },
                onDismiss = { editingTransaction = null }
            )
        }

        // Backup Restore Mode Dialog
        pendingRestoreJson?.let { json ->
            AlertDialog(
                onDismissRequest = { pendingRestoreJson = null },
                title = { Text("Restore Backup") },
                text = {
                    Text("Would you like to merge the backup entries with your current ledger, or replace your current ledger with the backup?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.restoreBackupJson(json, replaceExisting = false)
                            pendingRestoreJson = null
                        }
                    ) {
                        Text("Merge")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.restoreBackupJson(json, replaceExisting = true)
                            pendingRestoreJson = null
                        }
                    ) {
                        Text("Replace All")
                    }
                }
            )
        }
    }
}
