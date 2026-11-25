package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.HisabSettings
import com.example.data.ThemeMode
import com.example.domain.CurrencyFormatter
import com.example.ui.theme.AmountCardStyle
import com.example.ui.theme.LocalHisabColors

@Composable
fun SettingsScreen(
    settings: HisabSettings,
    onUpdateOpeningBalance: (Long) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onEnablePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onExportBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onToggleDemoMode: (Boolean) -> Unit,
    onClearAllRealData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hisabColors = LocalHisabColors.current
    var showOpeningBalanceDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

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
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Opening Balance
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOpeningBalanceDialog = true }
                        .testTag("settings_opening_balance"),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Opening Balance",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Starting cash/bank balance before recorded transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CurrencyFormatter.formatInr(settings.openingBalancePaise),
                            style = AmountCardStyle,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Theme Mode
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Appearance",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = settings.themeMode == mode,
                                    onClick = { onSelectThemeMode(mode) },
                                    label = { Text(mode.label) },
                                    modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // PIN App Lock
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "PIN App Lock",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (settings.isPinEnabled) "4-digit PIN protection is active"
                                        else "Require a 4-digit PIN when opening the app",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = settings.isPinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) showPinSetupDialog = true
                                    else onDisablePin()
                                },
                                modifier = Modifier.testTag("pin_lock_switch")
                            )
                        }

                        if (settings.isPinEnabled) {
                            OutlinedButton(
                                onClick = { showPinSetupDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Change 4-Digit PIN")
                            }
                        }
                    }
                }
            }

            // Backup & Restore
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Backup & Restore",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Save a complete JSON backup of your real ledger entries or restore from a previously saved backup file.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onExportBackup,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("backup_export_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Backup,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Backup")
                            }

                            OutlinedButton(
                                onClick = onRestoreBackup,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("backup_restore_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Restore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore")
                            }
                        }
                    }
                }
            }

            // Optional Demo Mode
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Science,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Column {
                                Text(
                                    text = "Demo Mode (Sample Data)",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Temporarily populate sample business entries to preview graphs & reports. Safe to turn off anytime.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.isDemoMode,
                            onCheckedChange = onToggleDemoMode,
                            modifier = Modifier.testTag("demo_mode_switch")
                        )
                    }
                }
            }

            // Clear Real Ledger Data
            item {
                SettingsActionCard(
                    icon = Icons.Outlined.DeleteForever,
                    title = "Reset Ledger Data",
                    subtitle = "Permanently delete all saved transactions and reset opening balance to ₹0.00",
                    tint = hisabColors.debit,
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.testTag("clear_data_card")
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showOpeningBalanceDialog) {
        OpeningBalanceDialog(
            currentPaise = settings.openingBalancePaise,
            onSave = { paise ->
                onUpdateOpeningBalance(paise)
                showOpeningBalanceDialog = false
            },
            onDismiss = { showOpeningBalanceDialog = false }
        )
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            title = if (settings.isPinEnabled) "Change App PIN" else "Set App PIN",
            onConfirmPin = { pin ->
                onEnablePin(pin)
                showPinSetupDialog = false
            },
            onDismiss = { showPinSetupDialog = false }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Reset All Ledger Data?") },
            text = {
                Text("This will permanently delete all your saved Credit and Debit transactions and reset Opening Balance to ₹0.00. Export a backup first if needed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearAllRealData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = hisabColors.debit)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = tint
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OpeningBalanceDialog(
    currentPaise: Long,
    onSave: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(CurrencyFormatter.formatEditableAmount(currentPaise)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Opening Balance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current Balance = Opening Balance + Total Credit - Total Debit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        error = null
                    },
                    label = { Text("Opening Balance (₹)") },
                    prefix = { Text("₹ ") },
                    isError = error != null,
                    supportingText = error?.let { msg -> { Text(msg) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = CurrencyFormatter.parseSignedAmountToPaise(input)
                    if (parsed == null) {
                        error = "Enter a valid INR amount (e.g. 0 or 25000)"
                    } else {
                        onSave(parsed)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
