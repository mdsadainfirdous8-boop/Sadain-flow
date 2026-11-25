package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TransactionCategories
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.domain.CurrencyFormatter
import com.example.domain.DateUtils
import com.example.ui.PendingTransactionInput
import com.example.ui.theme.AmountListStyle
import com.example.ui.theme.LocalHisabColors

fun categoryIcon(category: String, type: TransactionType): ImageVector {
    return when (category) {
        "Sales" -> Icons.Outlined.Storefront
        "Business Income" -> Icons.Outlined.BusinessCenter
        "Online Payment" -> Icons.Outlined.QrCode2
        "Cash Payment" -> Icons.Outlined.Payments
        "Other Income" -> Icons.Outlined.AccountBalanceWallet
        "Shopping" -> Icons.Outlined.ShoppingBag
        "Food" -> Icons.Outlined.Restaurant
        "Transport" -> Icons.Outlined.LocalShipping
        "Business Expenses" -> Icons.Outlined.ReceiptLong
        "Electricity Bill" -> Icons.Outlined.Bolt
        "Rent" -> Icons.Outlined.HomeWork
        else -> if (type == TransactionType.CREDIT) {
            Icons.Outlined.AccountBalanceWallet
        } else {
            Icons.Outlined.Category
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hisabColors = LocalHisabColors.current
    val isCredit = transaction.transactionType == TransactionType.CREDIT
    val accentColor = if (isCredit) hisabColors.credit else hisabColors.debit
    val badgeBg = if (isCredit) hisabColors.creditContainer else hisabColors.debitContainer
    val badgeFg = if (isCredit) hisabColors.onCreditContainer else hisabColors.onDebitContainer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("transaction_card_${transaction.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(transaction.category, transaction.transactionType),
                    contentDescription = transaction.category,
                    tint = badgeFg,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.partyOrMethod.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = transaction.partyOrMethod,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = "${DateUtils.formatDisplayDate(transaction.dateKey)} • ${DateUtils.formatDisplayTime(transaction.timeText)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatInr(
                        paise = if (isCredit) transaction.amountPaise else -transaction.amountPaise,
                        showPlusSign = isCredit
                    ),
                    style = AmountListStyle,
                    color = accentColor
                )
                Text(
                    text = if (isCredit) "CREDIT" else "DEBIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (onDeleteClick != null) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_tx_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_delete_transaction),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisabDatePickerDialog(
    initialDateKey: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.utcPickerMillisFromDateKey(initialDateKey)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        onDateSelected(DateUtils.dateKeyFromUtcPickerMillis(millis))
                    }
                    onDismiss()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisabTimePickerDialog(
    initialTime24h: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (initH, initM) = remember(initialTime24h) { DateUtils.parseTime24h(initialTime24h) }
    val timeState = rememberTimePickerState(
        initialHour = initH,
        initialMinute = initM,
        is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time", style = MaterialTheme.typography.titleLarge) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timeState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(DateUtils.formatTime24h(timeState.hour, timeState.minute))
                    onDismiss()
                }
            ) {
                Text("Confirm")
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
fun DuplicateWarningDialog(
    pending: PendingTransactionInput,
    existingCount: Int,
    onConfirmSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        title = { Text("Possible Duplicate Entry") },
        text = {
            Text(
                "You already have $existingCount ${pending.type.name.lowercase()} entry for " +
                    "${CurrencyFormatter.formatInr(pending.amountPaise)} in ${pending.category} on " +
                    "${DateUtils.formatDisplayDate(pending.dateKey)}. Do you still want to save this?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmSave,
                modifier = Modifier.testTag("confirm_duplicate_save")
            ) {
                Text("Save Anyway")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionEditSheet(
    transaction: TransactionEntity,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hisabColors = LocalHisabColors.current

    var selectedType by remember(transaction.id) { mutableStateOf(transaction.transactionType) }
    var amountText by remember(transaction.id) {
        mutableStateOf(CurrencyFormatter.formatEditableAmount(transaction.amountPaise))
    }
    var dateKey by remember(transaction.id) { mutableStateOf(transaction.dateKey) }
    var timeText by remember(transaction.id) { mutableStateOf(transaction.timeText) }
    var category by remember(transaction.id) { mutableStateOf(transaction.category) }
    var partyOrMethod by remember(transaction.id) { mutableStateOf(transaction.partyOrMethod) }
    var description by remember(transaction.id) { mutableStateOf(transaction.description) }
    var amountError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categories = if (selectedType == TransactionType.CREDIT) {
        TransactionCategories.creditCategories
    } else {
        TransactionCategories.debitCategories
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Transaction",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.testTag("sheet_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_delete_transaction),
                        tint = hisabColors.debit
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { idx, type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            val nextCats = if (type == TransactionType.CREDIT) {
                                TransactionCategories.creditCategories
                            } else {
                                TransactionCategories.debitCategories
                            }
                            if (category !in nextCats) category = nextCats.first()
                            if (type == TransactionType.DEBIT && partyOrMethod !in TransactionCategories.paymentMethods) {
                                partyOrMethod = TransactionCategories.paymentMethods.first()
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(idx, TransactionType.entries.size)
                    ) {
                        Text(if (type == TransactionType.CREDIT) "Credit (Income)" else "Debit (Expense)")
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = null
                },
                label = { Text("Amount (₹)") },
                prefix = { Text("₹ ") },
                isError = amountError != null,
                supportingText = amountError?.let { msg -> { Text(msg) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_amount_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = category == cat
                    FilterChip(
                        selected = selected,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (selectedType == TransactionType.CREDIT) {
                                hisabColors.creditContainer
                            } else {
                                hisabColors.debitContainer
                            },
                            selectedLabelColor = if (selectedType == TransactionType.CREDIT) {
                                hisabColors.onCreditContainer
                            } else {
                                hisabColors.onDebitContainer
                            }
                        )
                    )
                }
            }

            if (selectedType == TransactionType.CREDIT) {
                OutlinedTextField(
                    value = partyOrMethod,
                    onValueChange = { partyOrMethod = it },
                    label = { Text("Customer Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                            label = { Text(method) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val paise = CurrencyFormatter.parsePositiveAmountToPaise(amountText)
                        if (paise == null) {
                            amountError = "Enter a valid amount greater than ₹0"
                            return@Button
                        }
                        onSave(
                            transaction.copy(
                                type = selectedType.name,
                                amountPaise = paise,
                                dateKey = dateKey,
                                timeText = timeText,
                                category = category,
                                partyOrMethod = partyOrMethod,
                                description = description
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.CREDIT) hisabColors.credit else hisabColors.debit
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_edit_button")
                ) {
                    Text("Save Changes")
                }
            }
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry?") },
            text = {
                Text(
                    "Remove this ${transaction.category} entry of ${CurrencyFormatter.formatInr(transaction.amountPaise)}? " +
                        "All balances and reports will update automatically."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(transaction.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = hisabColors.debit)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
