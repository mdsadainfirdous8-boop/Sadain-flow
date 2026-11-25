package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class YearMonthKey(val year: Int, val month: Int) { // month: 1..12
    fun previousMonth(): YearMonthKey =
        if (month == 1) YearMonthKey(year - 1, 12) else YearMonthKey(year, month - 1)

    fun nextMonth(): YearMonthKey =
        if (month == 12) YearMonthKey(year + 1, 1) else YearMonthKey(year, month + 1)

    fun prefix(): String = "%04d-%02d".format(Locale.US, year, month)

    fun displayTitle(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun shortTitle(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMM yy", Locale.getDefault()).format(cal.time)
    }
}

object DateUtils {
    private val utcTz = TimeZone.getTimeZone("UTC")

    fun todayDateKey(): String {
        val cal = Calendar.getInstance()
        return formatDateKey(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentYearMonth(): YearMonthKey {
        val cal = Calendar.getInstance()
        return YearMonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun currentTime24h(): String {
        val cal = Calendar.getInstance()
        return "%02d:%02d".format(
            Locale.US,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
    }

    fun currentHourAndMinute(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
    }

    fun formatDateKey(year: Int, month: Int, dayOfMonth: Int): String =
        "%04d-%02d-%02d".format(Locale.US, year, month, dayOfMonth)

    fun parseDateKey(dateKey: String): Triple<Int, Int, Int>? {
        val parts = dateKey.split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        if (m !in 1..12 || d !in 1..31) return null
        return Triple(y, m, d)
    }

    fun parseTime24h(timeText: String): Pair<Int, Int> {
        val parts = timeText.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return h to m
    }

    fun formatTime24h(hour: Int, minute: Int): String =
        "%02d:%02d".format(Locale.US, hour.coerceIn(0, 23), minute.coerceIn(0, 59))

    fun formatDisplayDate(dateKey: String): String {
        val parsed = parseDateKey(dateKey) ?: return dateKey
        val cal = Calendar.getInstance().apply {
            set(parsed.first, parsed.second - 1, parsed.third, 0, 0, 0)
        }
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun formatShortDate(dateKey: String): String {
        val parsed = parseDateKey(dateKey) ?: return dateKey
        val cal = Calendar.getInstance().apply {
            set(parsed.first, parsed.second - 1, parsed.third, 0, 0, 0)
        }
        return SimpleDateFormat("dd MMM", Locale.getDefault()).format(cal.time)
    }

    fun formatDisplayTime(time24h: String): String {
        val (h, m) = parseTime24h(time24h)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
        }
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    fun toEpochMillis(dateKey: String, time24h: String): Long {
        val parsed = parseDateKey(dateKey) ?: return System.currentTimeMillis()
        val (h, m) = parseTime24h(time24h)
        return Calendar.getInstance().apply {
            set(parsed.first, parsed.second - 1, parsed.third, h, m, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Converts Material 3 DatePicker UTC midnight millis to yyyy-MM-dd without local TZ drift.
     */
    fun dateKeyFromUtcPickerMillis(utcMillis: Long): String {
        val cal = Calendar.getInstance(utcTz).apply { timeInMillis = utcMillis }
        return formatDateKey(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Converts yyyy-MM-dd to UTC midnight millis for initializing Material 3 DatePicker.
     */
    fun utcPickerMillisFromDateKey(dateKey: String): Long {
        val parsed = parseDateKey(dateKey) ?: return System.currentTimeMillis()
        return Calendar.getInstance(utcTz).apply {
            clear()
            set(parsed.first, parsed.second - 1, parsed.third, 0, 0, 0)
        }.timeInMillis
    }

    fun addDays(dateKey: String, deltaDays: Int): String {
        val parsed = parseDateKey(dateKey) ?: return dateKey
        val cal = Calendar.getInstance().apply {
            set(parsed.first, parsed.second - 1, parsed.third, 12, 0, 0)
            add(Calendar.DAY_OF_MONTH, deltaDays)
        }
        return formatDateKey(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Returns Monday..Sunday dateKeys (7 items) for the week containing [anchorDateKey].
     */
    fun weekDateKeys(anchorDateKey: String): List<String> {
        val parsed = parseDateKey(anchorDateKey) ?: return emptyList()
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(parsed.first, parsed.second - 1, parsed.third, 12, 0, 0)
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val diffToMonday = when (dayOfWeek) {
                Calendar.SUNDAY -> -6
                else -> Calendar.MONDAY - dayOfWeek
            }
            add(Calendar.DAY_OF_MONTH, diffToMonday)
        }
        return List(7) { idx ->
            val key = formatDateKey(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
            key
        }
    }

    fun dayShortLabel(dateKey: String): String {
        val parsed = parseDateKey(dateKey) ?: return ""
        val cal = Calendar.getInstance().apply {
            set(parsed.first, parsed.second - 1, parsed.third, 12, 0, 0)
        }
        return SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
    }

    fun daysInMonth(yearMonth: YearMonthKey): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, yearMonth.year)
            set(Calendar.MONTH, yearMonth.month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Returns 0 = Monday .. 6 = Sunday for the 1st day of the given month.
     */
    fun firstDayOfWeekOffsetMondayStart(yearMonth: YearMonthKey): Int {
        val cal = Calendar.getInstance().apply {
            set(yearMonth.year, yearMonth.month - 1, 1, 12, 0, 0)
        }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    fun recentMonths(count: Int = 12): List<YearMonthKey> {
        val list = mutableListOf<YearMonthKey>()
        var current = currentYearMonth()
        repeat(count) {
            list.add(current)
            current = current.previousMonth()
        }
        return list
    }
}
