package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.CategoryBreakdown
import com.example.domain.CurrencyFormatter
import com.example.domain.GraphBarPoint
import com.example.ui.theme.AmountListStyle
import com.example.ui.theme.LocalHisabColors
import kotlin.math.max

@Composable
fun CreditVsDebitComparisonCard(
    creditPaise: Long,
    debitPaise: Long,
    modifier: Modifier = Modifier
) {
    val hisabColors = LocalHisabColors.current
    val total = creditPaise + debitPaise
    val creditFraction = if (total > 0L) (creditPaise.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0.5f
    val creditPct = if (total > 0L) ((creditPaise * 100.0) / total).toInt() else 0
    val debitPct = if (total > 0L) 100 - creditPct else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Income vs Expenses Comparison",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (total > 0L) {
                    if (creditFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(creditFraction.coerceAtLeast(0.02f))
                                .height(14.dp)
                                .background(hisabColors.credit)
                        )
                    }
                    if (1f - creditFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .weight((1f - creditFraction).coerceAtLeast(0.02f))
                                .height(14.dp)
                                .background(hisabColors.debit)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(hisabColors.credit)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Credit $creditPct% (${CurrencyFormatter.formatCompactInr(creditPaise)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = hisabColors.credit
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(hisabColors.debit)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Debit $debitPct% (${CurrencyFormatter.formatCompactInr(debitPaise)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = hisabColors.debit
                    )
                }
            }
        }
    }
}

@Composable
fun FinancialBarChartCard(
    title: String,
    points: List<GraphBarPoint>,
    isMonthly: Boolean,
    modifier: Modifier = Modifier
) {
    val hisabColors = LocalHisabColors.current
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val creditColor = hisabColors.credit
    val debitColor = hisabColors.debit
    val maxVal = max(1L, points.maxOfOrNull { max(it.creditPaise, it.debitPaise) } ?: 1L)
    val hasAnyData = points.any { it.creditPaise > 0L || it.debitPaise > 0L }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(color = creditColor, label = "Credit")
                    LegendDot(color = debitColor, label = "Debit")
                }
            }

            if (!hasAnyData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions recorded for this period yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier
                            .height(160.dp)
                            .padding(end = 6.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = CurrencyFormatter.formatCompactInr(maxVal),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCompactInr(maxVal / 2),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                        // Horizontal guide lines
                        listOf(0f, height / 2f, height).forEach { y ->
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.5f,
                                pathEffect = if (y < height) dash else null
                            )
                        }

                        if (points.isNotEmpty()) {
                            val groupWidth = width / points.size
                            val barWidth = (groupWidth * 0.32f).coerceIn(3f, 20f)
                            val innerGap = (groupWidth * 0.08f).coerceAtLeast(1f)

                            points.forEachIndexed { index, point ->
                                val centerX = index * groupWidth + groupWidth / 2f

                                if (point.creditPaise > 0L) {
                                    val barH = (point.creditPaise.toFloat() / maxVal.toFloat()) * (height - 4f)
                                    val clampedH = barH.coerceAtLeast(4f)
                                    drawRoundRect(
                                        color = creditColor,
                                        topLeft = Offset(centerX - barWidth - innerGap / 2f, height - clampedH),
                                        size = Size(barWidth, clampedH),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }

                                if (point.debitPaise > 0L) {
                                    val barH = (point.debitPaise.toFloat() / maxVal.toFloat()) * (height - 4f)
                                    val clampedH = barH.coerceAtLeast(4f)
                                    drawRoundRect(
                                        color = debitColor,
                                        topLeft = Offset(centerX + innerGap / 2f, height - clampedH),
                                        size = Size(barWidth, clampedH),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }
                            }
                        }
                    }
                }

                // X-axis tick labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 42.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labels = if (isMonthly && points.size > 10) {
                        listOf("1", "8", "15", "22", points.last().label)
                    } else {
                        points.map { it.label }
                    }
                    labels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryBreakdownCard(
    title: String,
    items: List<CategoryBreakdown>,
    isCredit: Boolean,
    modifier: Modifier = Modifier
) {
    val hisabColors = LocalHisabColors.current
    val accent = if (isCredit) hisabColors.credit else hisabColors.debit
    val track = if (isCredit) hisabColors.creditContainer else hisabColors.debitContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (items.isEmpty()) {
                Text(
                    text = "No entries in this category group.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.forEach { row ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${row.category} (${row.count})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = CurrencyFormatter.formatInr(row.amountPaise),
                                style = AmountListStyle,
                                color = accent
                            )
                        }
                        LinearProgressIndicator(
                            progress = { row.ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accent,
                            trackColor = track
                        )
                    }
                }
            }
        }
    }
}
