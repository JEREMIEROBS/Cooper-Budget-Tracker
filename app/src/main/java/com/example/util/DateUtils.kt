package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    private val monthDisplayFormat = SimpleDateFormat("MMMM yyyy", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    fun currentMonthKey(): String {
        return monthKeyFormat.format(Date())
    }

    fun formatMonthDisplay(monthKey: String): String {
        return try {
            val date = monthKeyFormat.parse(monthKey)
            if (date != null) monthDisplayFormat.format(date) else monthKey
        } catch (_: Exception) {
            monthKey
        }
    }

    fun getAdjacentMonthKey(monthKey: String, offset: Int): String {
        return try {
            val date = monthKeyFormat.parse(monthKey) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, offset)
            }
            monthKeyFormat.format(cal.time)
        } catch (_: Exception) {
            monthKey
        }
    }

    fun getMonthRange(monthKey: String): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
        }
        try {
            val date = monthKeyFormat.parse(monthKey)
            if (date != null) {
                cal.time = date
            }
        } catch (_: Exception) {}

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMs = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endMs = cal.timeInMillis

        return Pair(startMs, endMs)
    }

    fun getDaysRemainingInMonth(monthKey: String): Int {
        val now = Calendar.getInstance()
        val currentMonth = currentMonthKey()
        if (monthKey != currentMonth) {
            val cal = Calendar.getInstance()
            try {
                cal.time = monthKeyFormat.parse(monthKey) ?: Date()
                return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            } catch (_: Exception) {
                return 30
            }
        }

        val maxDays = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        return (maxDays - currentDay + 1).coerceAtLeast(1)
    }

    fun formatDayGroupHeader(timestamp: Long): String {
        val itemCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance()

        val isSameYear = itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        val isSameDay = isSameYear && itemCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        nowCal.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = isSameYear && itemCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> {
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                "Today, ${fmt.format(Date(timestamp))}"
            }
            isYesterday -> {
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                "Yesterday, ${fmt.format(Date(timestamp))}"
            }
            isSameYear -> {
                val fmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                fmt.format(Date(timestamp))
            }
            else -> {
                val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                fmt.format(Date(timestamp))
            }
        }
    }

    fun formatTransactionTime(timestamp: Long): String {
        val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        return fmt.format(Date(timestamp))
    }

    fun formatFullDate(timestamp: Long): String {
        val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return fmt.format(Date(timestamp))
    }
}
