package com.financemanager.core.ui.utils

import java.util.Calendar
import java.util.Locale

enum class HomeExpensePeriod {
    WEEKLY,
    MONTHLY,
    ;
}

data class MillisRange(val start: Long, val end: Long)

fun rangesForHomeExpensePeriod(period: HomeExpensePeriod): Pair<MillisRange, MillisRange> {
    val cal = Calendar.getInstance(Locale.getDefault())
    return when (period) {
        HomeExpensePeriod.WEEKLY -> {
            val weekStart = Calendar.getInstance(Locale.getDefault()).apply {
                timeInMillis = cal.timeInMillis
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val weekEnd = weekStart.clone() as Calendar
            weekEnd.add(Calendar.DAY_OF_WEEK, 6)
            weekEnd.set(Calendar.HOUR_OF_DAY, 23)
            weekEnd.set(Calendar.MINUTE, 59)
            weekEnd.set(Calendar.SECOND, 59)
            weekEnd.set(Calendar.MILLISECOND, 999)
            val cur = MillisRange(weekStart.timeInMillis, weekEnd.timeInMillis)
            val prevEnd = weekStart.timeInMillis - 1
            val prevStartCal = weekStart.clone() as Calendar
            prevStartCal.add(Calendar.DAY_OF_MONTH, -7)
            val prev = MillisRange(prevStartCal.timeInMillis, prevEnd)
            cur to prev
        }
        HomeExpensePeriod.MONTHLY -> {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val (ms, me) = monthRangeMillis(y, m)
            val cur = MillisRange(ms, me)
            val prevCal = cal.clone() as Calendar
            prevCal.add(Calendar.MONTH, -1)
            val (ps, pe) = monthRangeMillis(
                prevCal.get(Calendar.YEAR),
                prevCal.get(Calendar.MONTH),
            )
            val prev = MillisRange(ps, pe)
            cur to prev
        }
    }
}
