package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TransactionCategories
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.domain.CurrencyFormatter
import com.example.domain.DateUtils
import com.example.ui.PendingTransactionInput
import com.example.ui.components.HisabDatePickerDialog
import com.example.ui.components.HisabTimePickerDialog
import com.example.ui.components.TransactionItemCard
import com.example.ui.components.categoryIcon
import com.example.ui.theme.AmountCardStyle
import com.example.ui.theme.LocalHisabColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionEntryScreen(
    type: TransactionType,
    allTransactions: List<TransactionEntity>,
    preselectedDateKey: String? = null,
    onSubmit: (PendingTransactionInput) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val hisabColors = LocalHisabColors.current
    val isCredit = type == TransactionType.CREDIT
    val accent = if (isCredit) hisabColors.credit else hisabColors.debit
    val container = if (isCredit) hisabColors.creditContainer else hisabColors.debitContainer
    val onContainer = if (isCredit) hisabColors.onCreditContainer else hisabColors.onDebitContainer

    val categories = if (isCredit) {
        TransactionCategories.creditCategories
    } else {
        TransactionCategories.debitCategories
    }

    var amountText by rememberSaveable(type) { mutableStateOf("") }
    var dateKey by rememberSaveable(type, preselectedDateKey) {
        mutableStateOf(preselectedDateKey ?: DateUtils.todayDateKey())
    }
    var timeText by rememberSaveable(type) { mutableStateOf(DateUtils.currentTime24h()) }
    var selectedCategory by rememberSaveable(type) { mutableStateOf(categories.first()) }
    var partyOrMethod by rememberSaveable(type) {
        mutableStateOf(if (isCredit) "" else TransactionCategories.paymentMethods.first())
    }
    var description by rememberSaveable(type) { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val typedTransactions = remember(allTransactions, type) {
        allTransactions.filter { it.transactionType == type }
    }
    val typeTotalPaise = remember(typedTransactions) {
        typedTransactions.sumOf { it.amountPaise }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(container)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isCredit) {
                                Icons.AutoMirrored.Outlined.TrendingUp
                            } else {
                                Icons.AutoMirrored.Outlined.TrendingDown
                            },
                            contentDescription = null,
                            tint = onContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = if (isCredit) "Record Credit (Income)" else "Record Debit (Expense)",
                                style = MaterialTheme.typography.titleLarge,
                                color = onContainer
                            )
                            Text(
                                text = "Total ${if (isCredit) "Income" else "Expenses"}: ${CurrencyFormatter.formatInr(typeTotalPaise)}",
                                style = AmountCardStyle,
                                color = accent
                            )
                        }
                    }
                }
            }

            // Entry Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it
                                amountError = null
                            },
                            label = {
                                Text(if (isCredit) "Income Amount (₹)" else "Expense Amount (₹)")
                            },
                            placeholder = { Text("0.00") },
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                            isError = amountError != null,
                            supportingText = amountError?.let { msg -> { Text(msg) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_amount_input")
                        )

                        // Quick amount increment chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100, 500, 1000, 2000, 5000).forEach { addRupees ->
                                AssistChip(
                                    onClick = {
                                        val currentPaise = CurrencyFormatter.parsePositiveAmountToPaise(amountText) ?: 0L
                                        val nextPaise = currentPaise + addRupees * 100L
                                        amountText = CurrencyFormatter.formatEditableAmount(nextPaise)
                                        amountError = null
                                    },
                                    label = { Text("+₹$addRupees") }
                                )
                            }
                        }

                        // Date & Time Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("entry_date_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = stringResource(R.string.cd_pick_date),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DateUtils.formatDisplayDate(dateKey))
                            }

                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("entry_time_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = stringResource(R.string.cd_pick_time),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DateUtils.formatDisplayTime(timeText))
                            }
                        }

                        // Categories
                        Text(
                            text = if (isCredit) "Income Category" else "Expense Category",
                            style = MaterialTheme.typography.labelLarge
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val selected = selectedCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = cat },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = categoryIcon(cat, type),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = container,
                                        selectedLabelColor = onContainer,
                                        selectedLeadingIconColor = onContainer
                                    ),
                                    modifier = Modifier.testTag("category_chip_$cat")
                                )
                            }
                        }

                        if (isCredit) {
                            OutlinedTextField(
                                value = partyOrMethod,
                                onValueChange = { partyOrMethod = it },
                                label = { Text("Customer Name (Optional)") },
                                placeholder = { Text("e.g. Sharma Traders") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("entry_customer_input")
                            )
                        } else {
                            Text(
                                text = "Payment Method",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TransactionCategories.paymentMethods.forEach { method ->
                                    FilterChip(
                                        selected = partyOrMethod == method,
                                        onClick = { partyOrMethod = method },
                                        label = { Text(method) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = container,
                                            selectedLabelColor = onContainer
                                        ),
                                        modifier = Modifier.testTag("method_chip_$method")
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description / Notes (Optional)") },
                            placeholder = {
                                Text(
                                    if (isCredit) "e.g. Invoice #105 retail sale"
                                    else "e.g. Office supplies or lunch bill"
                                )
                            },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_description_input")
                        )

                        Button(
                            onClick = {
                                val parsedPaise = CurrencyFormatter.parsePositiveAmountToPaise(amountText)
                                if (parsedPaise == null) {
                                    amountError = "Enter a valid amount greater than ₹0"
                                    return@Button
                                }
                                onSubmit(
                                    PendingTransactionInput(
                                        type = type,
                                        amountPaise = parsedPaise,
                                        dateKey = dateKey,
                                        timeText = timeText,
                                        category = selectedCategory,
                                        partyOrMethod = partyOrMethod,
                                        description = description
                                    )
                                )
                                amountText = ""
                                description = ""
                                if (isCredit) partyOrMethod = ""
                                timeText = DateUtils.currentTime24h()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_transaction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCredit) "Save Credit Entry" else "Save Debit Entry",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Recent entries of this type
            item {
                Text(
                    text = if (isCredit) "Recent Credit Entries (${typedTransactions.size})"
                    else "Recent Debit Entries (${typedTransactions.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (typedTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = if (isCredit) "No credit entries yet. Add your first income above."
                            else "No debit entries yet. Add your first expense above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(typedTransactions.take(15), key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        onClick = { onEditTransaction(tx) },
                        onDeleteClick = { onDeleteTransaction(tx.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showDatePicker) {
        HisabDatePickerDialog(
            initialDateKey = dateKey,
            onDateSelected = { dateKey = it },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        HisabTimePickerDialog(
            initialTime24h = timeText,
            onTimeSelected = { timeText = it },
            onDismiss = { showTimePicker = false }
        )
    }
}
