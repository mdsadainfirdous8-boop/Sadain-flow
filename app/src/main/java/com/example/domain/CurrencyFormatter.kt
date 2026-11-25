package com.example.domain

import kotlin.math.abs
import kotlin.math.roundToLong

object CurrencyFormatter {

    /**
     * Formats integer paise into Indian Rupee format using Indian numbering groups (##,##,###.##).
     * Examples:
     *  0 -> ₹0.00
     *  125000 -> ₹1,250.00
     *  12345678 -> ₹1,23,456.78
     *  -50000 -> -₹500.00
     */
    fun formatInr(paise: Long, showPlusSign: Boolean = false): String {
        val isNegative = paise < 0
        val absPaise = abs(paise)
        val rupees = absPaise / 100
        val remPaise = (absPaise % 100).toInt()
        val formattedRupees = formatIndianInteger(rupees)
        val prefix = when {
            isNegative -> "-₹"
            showPlusSign && paise > 0 -> "+₹"
            else -> "₹"
        }
        return "$prefix$formattedRupees.${"%02d".format(remPaise)}"
    }

    /**
     * Formats whole/compact amounts for graph axes and tight badges.
     */
    fun formatCompactInr(paise: Long): String {
        val isNegative = paise < 0
        val absRupees = abs(paise) / 100.0
        val sign = if (isNegative) "-" else ""
        return when {
            absRupees >= 10_000_000.0 -> "${sign}₹${trimTrailingZero(absRupees / 10_000_000.0)}Cr"
            absRupees >= 100_000.0 -> "${sign}₹${trimTrailingZero(absRupees / 100_000.0)}L"
            absRupees >= 1_000.0 -> "${sign}₹${trimTrailingZero(absRupees / 1_000.0)}K"
            else -> "${sign}₹${absRupees.roundToLong()}"
        }
    }

    /**
     * Formats paise as plain decimal string for edit fields (e.g. "1250.50" or "1250").
     */
    fun formatEditableAmount(paise: Long): String {
        val absPaise = abs(paise)
        val rupees = absPaise / 100
        val remPaise = (absPaise % 100).toInt()
        val sign = if (paise < 0) "-" else ""
        return if (remPaise == 0) "$sign$rupees" else "$sign$rupees.${"%02d".format(remPaise)}"
    }

    /**
     * Parses user-entered INR decimal text into positive paise. Returns null if invalid or <= 0.
     */
    fun parsePositiveAmountToPaise(input: String): Long? {
        val cleaned = input.trim().replace(",", "").replace("₹", "")
        if (cleaned.isEmpty()) return null
        val regex = Regex("""^\d+(\.\d{1,2})?$""")
        if (!regex.matches(cleaned)) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        if (value <= 0.0 || value > 100_000_000_000.0) return null
        val paise = (value * 100.0).roundToLong()
        return if (paise > 0L) paise else null
    }

    /**
     * Parses signed INR amount (allows 0 or negative for opening balance). Returns null if invalid.
     */
    fun parseSignedAmountToPaise(input: String): Long? {
        val cleaned = input.trim().replace(",", "").replace("₹", "")
        if (cleaned.isEmpty()) return null
        val regex = Regex("""^-?\d+(\.\d{1,2})?$""")
        if (!regex.matches(cleaned)) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        if (abs(value) > 100_000_000_000.0) return null
        return (value * 100.0).roundToLong()
    }

    private fun formatIndianInteger(value: Long): String {
        val str = value.toString()
        if (str.length <= 3) return str
        val lastThree = str.takeLast(3)
        val remaining = str.dropLast(3)
        val grouped = remaining.reversed().chunked(2).joinToString(",").reversed()
        return "$grouped,$lastThree"
    }

    private fun trimTrailingZero(value: Double): String {
        val formatted = String.format(java.util.Locale.US, "%.1f", value)
        return if (formatted.endsWith(".0")) formatted.dropLast(2) else formatted
    }
}
