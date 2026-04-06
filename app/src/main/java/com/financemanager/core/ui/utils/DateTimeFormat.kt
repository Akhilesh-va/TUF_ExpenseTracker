package com.financemanager.core.ui.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val DayMonthYear: SimpleDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
private val MonthYear: SimpleDateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
private val FullDate: SimpleDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

fun formatGreetingDate(timestamp: Long): String = DayMonthYear.format(Date(timestamp))

fun formatTransactionDate(timestamp: Long): String = FullDate.format(Date(timestamp))

fun formatMonthYear(year: Int, monthZeroBased: Int): String {
    val cal = Calendar.getInstance(Locale.getDefault()).apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthZeroBased)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return MonthYear.format(cal.time)
}

fun monthRangeMillis(year: Int, monthZeroBased: Int): Pair<Long, Long> {
    val start = Calendar.getInstance(Locale.getDefault()).apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthZeroBased)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance(Locale.getDefault()).apply {
        timeInMillis = start.timeInMillis
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return start.timeInMillis to end.timeInMillis
}

fun currentMonthYear(): Pair<Int, Int> {
    val c = Calendar.getInstance(Locale.getDefault())
    return c.get(Calendar.YEAR) to c.get(Calendar.MONTH)
}

/** Start of week (locale) from roughly six weeks ago — rolling window for balance bars. */
fun sixWeekRollingStartMillis(): Long {
    val c = Calendar.getInstance(Locale.getDefault())
    c.add(Calendar.WEEK_OF_YEAR, -6)
    c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun formatDayMonthShort(timestamp: Long): String =
    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
