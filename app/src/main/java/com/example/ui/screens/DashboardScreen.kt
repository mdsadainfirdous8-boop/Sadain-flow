package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TransactionEntity
import com.example.domain.CurrencyFormatter
import com.example.domain.DashboardSummary
import com.example.domain.DateUtils
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.AmountCardStyle
import com.example.ui.theme.AmountDisplayStyle
import com.example.ui.theme.LocalHisabColors

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    recentTransactions: List<TransactionEntity>,
    isDemoMode: Boolean,
    onAddCreditClick: () -> Unit,
    onAddDebitClick: () -> Unit,
    onOpenHistoryClick: () -> Unit,
    onOpenCalendarClick: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onDisableDemoMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hisabColors = LocalHisabColors.current

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
            // Top Greeting & Demo Mode Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MY DAILY HISAB",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Today • ${DateUtils.formatDisplayDate(DateUtils.todayDateKey())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isDemoMode) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(onClick = onDisableDemoMode)
                                .testTag("demo_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Demo Mode • Tap to Exit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Hero Current Balance & Net Profit Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(hisabColors.heroStart, hisabColors.heroEnd)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Current Balance",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Opening: ${CurrencyFormatter.formatCompactInr(summary.openingBalancePaise)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = CurrencyFormatter.formatInr(summary.currentBalancePaise),
                            style = AmountDisplayStyle,
                            color = Color.White,
                            modifier = Modifier.testTag("dashboard_current_balance")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HeroPillMetric(
                                label = "Total Credit",
                                value = CurrencyFormatter.formatInr(summary.totalCreditPaise),
                                modifier = Modifier.weight(1f)
                            )
                            HeroPillMetric(
                                label = "Total Debit",
                                value = CurrencyFormatter.formatInr(summary.totalDebitPaise),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2x2 Financial Summary Grid: Total Income, Total Expenses, Today's Profit, Monthly Profit
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryMetricCard(
                            title = "Total Income",
                            subtitle = "All Credit",
                            amountPaise = summary.totalIncomePaise,
                            accentColor = hisabColors.credit,
                            badgeBg = hisabColors.creditContainer,
                            badgeFg = hisabColors.onCreditContainer,
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_total_income")
                        )
                        SummaryMetricCard(
                            title = "Total Expenses",
                            subtitle = "All Debit",
                            amountPaise = summary.totalExpensesPaise,
                            accentColor = hisabColors.debit,
                            badgeBg = hisabColors.debitContainer,
                            badgeFg = hisabColors.onDebitContainer,
                            icon = Icons.AutoMirrored.Outlined.TrendingDown,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_total_expenses")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val todayPositive = summary.todayProfitPaise >= 0L
                        SummaryMetricCard(
                            title = "Today's Profit",
                            subtitle = if (todayPositive) "Surplus" else "Loss",
                            amountPaise = summary.todayProfitPaise,
                            accentColor = if (todayPositive) hisabColors.credit else hisabColors.debit,
                            badgeBg = if (todayPositive) hisabColors.creditContainer else hisabColors.debitContainer,
                            badgeFg = if (todayPositive) hisabColors.onCreditContainer else hisabColors.onDebitContainer,
                            icon = if (todayPositive) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                            showPlus = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_today_profit")
                        )

                        val monthPositive = summary.monthlyProfitPaise >= 0L
                        SummaryMetricCard(
                            title = "Monthly Profit",
                            subtitle = DateUtils.currentYearMonth().shortTitle(),
                            amountPaise = summary.monthlyProfitPaise,
                            accentColor = if (monthPositive) hisabColors.credit else hisabColors.debit,
                            badgeBg = if (monthPositive) hisabColors.creditContainer else hisabColors.debitContainer,
                            badgeFg = if (monthPositive) hisabColors.onCreditContainer else hisabColors.onDebitContainer,
                            icon = if (monthPositive) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                            showPlus = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_monthly_profit")
                        )
                    }
                }
            }

            // Quick Actions Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddCreditClick,
                            colors = ButtonDefaults.buttonColors(containerColor = hisabColors.credit),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("quick_add_credit")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.cd_add_credit),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Credit")
                        }

                        Button(
                            onClick = onAddDebitClick,
                            colors = ButtonDefaults.buttonColors(containerColor = hisabColors.debit),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("quick_add_debit")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Remove,
                                contentDescription = stringResource(R.string.cd_add_debit),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Debit")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onOpenHistoryClick,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_open_history")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("History")
                        }

                        FilledTonalButton(
                            onClick = onOpenCalendarClick,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_open_calendar")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calendar")
                        }
                    }
                }
            }

            // Recent Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (recentTransactions.isNotEmpty()) {
                        TextButton(onClick = onOpenHistoryClick) {
                            Text("See All (${summary.totalCount})")
                        }
                    }
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_ledger_empty_1795627785526),
                                contentDescription = stringResource(R.string.cd_empty_ledger),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "Your Ledger is Ready",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap Add Credit for income or Add Debit for expenses. All profits and reports update automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(recentTransactions.take(8), key = { it.id }) { tx ->
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
}

@Composable
private fun HeroPillMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = value,
                style = AmountCardStyle,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    subtitle: String,
    amountPaise: Long,
    accentColor: Color,
    badgeBg: Color,
    badgeFg: Color,
    icon: ImageVector,
    showPlus: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeFg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = CurrencyFormatter.formatInr(amountPaise, showPlusSign = showPlus),
                style = AmountCardStyle,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
