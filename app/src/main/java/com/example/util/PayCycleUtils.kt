package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class PayCycleType(val code: String, val label: String) {
    MONTHLY("monthly", "Monthly (Calendar)"),
    SEMI_MONTHLY("semimonthly", "Semi-monthly (15th & 30th)"),
    WEEKLY("weekly", "Weekly"),
    CUSTOM("custom", "Custom Start Day");

    companion object {
        fun fromCode(code: String): PayCycleType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: MONTHLY
        }
    }
}

data class PayCycleInfo(
    val cycleKey: String,
    val displayLabel: String,
    val startMs: Long,
    val endMs: Long,
    val daysRemaining: Int,
    val isCurrentCycle: Boolean
)

object PayCycleUtils {

    fun getCurrentCycleKey(type: PayCycleType, startDay: Int = 1): String {
        val now = Calendar.getInstance()
        return getCycleKeyForTimestamp(now.timeInMillis, type, startDay)
    }

    fun getCycleKeyForTimestamp(timestamp: Long, type: PayCycleType, startDay: Int = 1): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        return when (type) {
            PayCycleType.MONTHLY -> {
                String.format(Locale.US, "%04d-%02d", year, month)
            }
            PayCycleType.SEMI_MONTHLY -> {
                val half = if (day <= 15) "H1" else "H2"
                String.format(Locale.US, "%04d-%02d-%s", year, month, half)
            }
            PayCycleType.WEEKLY -> {
                val week = cal.get(Calendar.WEEK_OF_YEAR)
                String.format(Locale.US, "%04d-W%02d", year, week)
            }
            PayCycleType.CUSTOM -> {
                // If today is before startDay, cycle started in previous month
                if (day < startDay) {
                    val prevCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    val prevYear = prevCal.get(Calendar.YEAR)
                    val prevMonth = prevCal.get(Calendar.MONTH) + 1
                    String.format(Locale.US, "%04d-%02d-D%02d", prevYear, prevMonth, startDay)
                } else {
                    String.format(Locale.US, "%04d-%02d-D%02d", year, month, startDay)
                }
            }
        }
    }

    fun getCycleRange(cycleKey: String, type: PayCycleType, startDay: Int = 1): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        try {
            when (type) {
                PayCycleType.MONTHLY -> {
                    val parts = cycleKey.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val startMs = cal.timeInMillis

                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val endMs = cal.timeInMillis
                    return Pair(startMs, endMs)
                }
                PayCycleType.SEMI_MONTHLY -> {
                    val parts = cycleKey.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val half = parts.getOrNull(2) ?: "H1"

                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)

                    if (half == "H1") {
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        val startMs = cal.timeInMillis

                        cal.set(Calendar.DAY_OF_MONTH, 15)
                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        cal.set(Calendar.MILLISECOND, 999)
                        return Pair(startMs, cal.timeInMillis)
                    } else {
                        cal.set(Calendar.DAY_OF_MONTH, 16)
                        val startMs = cal.timeInMillis

                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        cal.set(Calendar.MILLISECOND, 999)
                        return Pair(startMs, cal.timeInMillis)
                    }
                }
                PayCycleType.WEEKLY -> {
                    val parts = cycleKey.split("-W")
                    val year = parts[0].toInt()
                    val week = parts[1].toInt()

                    cal.clear()
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.WEEK_OF_YEAR, week)
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val startMs = cal.timeInMillis

                    cal.add(Calendar.DAY_OF_WEEK, 6)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    return Pair(startMs, cal.timeInMillis)
                }
                PayCycleType.CUSTOM -> {
                    val parts = cycleKey.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val sDay = parts.getOrNull(2)?.replace("D", "")?.toIntOrNull() ?: startDay

                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, sDay.coerceAtMost(maxDay))
                    val startMs = cal.timeInMillis

                    // Next cycle start minus 1 ms
                    cal.add(Calendar.MONTH, 1)
                    val nextMaxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, sDay.coerceAtMost(nextMaxDay))
                    cal.add(Calendar.MILLISECOND, -1)
                    val endMs = cal.timeInMillis
                    return Pair(startMs, endMs)
                }
            }
        } catch (_: Exception) {
            return DateUtils.getMonthRange(DateUtils.currentMonthKey())
        }
    }

    fun getCycleInfo(cycleKey: String, type: PayCycleType, startDay: Int = 1): PayCycleInfo {
        val (startMs, endMs) = getCycleRange(cycleKey, type, startDay)
        val nowMs = System.currentTimeMillis()
        val isCurrent = nowMs in startMs..endMs

        val daysRemaining = if (nowMs > endMs) {
            1
        } else if (nowMs < startMs) {
            val totalDays = ((endMs - startMs) / (24 * 60 * 60 * 1000L)).toInt() + 1
            totalDays.coerceAtLeast(1)
        } else {
            // Days left including today. Divides by 1 on last day, never by 0!
            val msLeft = endMs - nowMs
            val days = (msLeft / (24 * 60 * 60 * 1000L)).toInt() + 1
            days.coerceAtLeast(1)
        }

        val startFmt = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(startMs))
        val endFmt = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(endMs))
        val label = when (type) {
            PayCycleType.MONTHLY -> {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(startMs))
            }
            PayCycleType.SEMI_MONTHLY -> {
                if (cycleKey.endsWith("H1")) "$startFmt – 15th (1st Half)" else "$startFmt – End (2nd Half)"
            }
            PayCycleType.WEEKLY -> {
                "$startFmt – $endFmt"
            }
            PayCycleType.CUSTOM -> {
                "$startFmt – $endFmt"
            }
        }

        return PayCycleInfo(
            cycleKey = cycleKey,
            displayLabel = label,
            startMs = startMs,
            endMs = endMs,
            daysRemaining = daysRemaining,
            isCurrentCycle = isCurrent
        )
    }

    fun getAdjacentCycleKey(cycleKey: String, offset: Int, type: PayCycleType, startDay: Int = 1): String {
        val (startMs, _) = getCycleRange(cycleKey, type, startDay)
        val cal = Calendar.getInstance().apply { timeInMillis = startMs }

        when (type) {
            PayCycleType.MONTHLY -> cal.add(Calendar.MONTH, offset)
            PayCycleType.SEMI_MONTHLY -> {
                if (cycleKey.endsWith("H1")) {
                    if (offset > 0) {
                        cal.set(Calendar.DAY_OF_MONTH, 18)
                    } else {
                        cal.add(Calendar.MONTH, -1)
                        cal.set(Calendar.DAY_OF_MONTH, 18)
                    }
                } else {
                    if (offset > 0) {
                        cal.add(Calendar.MONTH, 1)
                        cal.set(Calendar.DAY_OF_MONTH, 5)
                    } else {
                        cal.set(Calendar.DAY_OF_MONTH, 5)
                    }
                }
            }
            PayCycleType.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, offset)
            PayCycleType.CUSTOM -> cal.add(Calendar.MONTH, offset)
        }

        return getCycleKeyForTimestamp(cal.timeInMillis, type, startDay)
    }
}
