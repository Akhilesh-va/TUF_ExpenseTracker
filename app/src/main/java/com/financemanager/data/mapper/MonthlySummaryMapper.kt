package com.financemanager.data.mapper

import com.financemanager.core.ui.utils.monthRangeMillis
import com.financemanager.data.local.entity.TransactionWithCategory
import com.financemanager.data.local.entity.CategoryEntity
import com.financemanager.domain.model.CategorySpend
import com.financemanager.domain.model.MonthlySummary
import com.financemanager.domain.model.TransactionType
import com.financemanager.domain.model.WeekTotals
import java.util.Calendar
import java.util.Locale

fun buildMonthlySummary(
    year: Int,
    month: Int,
    rows: List<TransactionWithCategory>,
): MonthlySummary {
    var income = 0.0
    var expense = 0.0
    val expenseByCategory = mutableMapOf<String, Pair<Double, CategoryEntity>>()

    val (_, endRange) = monthRangeMillis(year, month)
    val calStart = Calendar.getInstance(Locale.getDefault()).apply {
        timeInMillis = endRange
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val maxDay = calStart.getActualMaximum(Calendar.DAY_OF_MONTH)
    val bucketCount = ((maxDay - 1) / 7) + 1
    val weeklyIncome = MutableList(bucketCount) { 0.0 }
    val weeklyExpense = MutableList(bucketCount) { 0.0 }

    rows.forEach { row ->
        val t = row.transaction
        val c = row.category
        when (TransactionType.fromStorage(t.type)) {
            TransactionType.INCOME -> {
                income += t.amount
                val wi = weekBucket(t.date, year, month)
                if (wi in weeklyIncome.indices) weeklyIncome[wi] += t.amount
            }
            TransactionType.EXPENSE -> {
                expense += t.amount
                val agg = expenseByCategory[c.id]
                if (agg == null) expenseByCategory[c.id] = t.amount to c
                else expenseByCategory[c.id] = (agg.first + t.amount) to c
                val wi = weekBucket(t.date, year, month)
                if (wi in weeklyExpense.indices) weeklyExpense[wi] += t.amount
            }
        }
    }

    val totalExpenseForShare = expense.coerceAtLeast(1e-9)
    val categorySpends = expenseByCategory.values.map { (amt, entity) ->
        CategorySpend(
            category = entity.toDomain(),
            amount = amt,
            share = (amt / totalExpenseForShare).toFloat(),
        )
    }.sortedByDescending { it.amount }

    val weekLabels = weeklyIncome.indices.map { w -> "W${w + 1}" }
    val weeklyTotals = weeklyIncome.indices.map { i ->
        WeekTotals(
            weekIndex = i,
            label = weekLabels.getOrElse(i) { "W${i + 1}" },
            income = weeklyIncome[i],
            expense = weeklyExpense[i],
        )
    }

    return MonthlySummary(
        year = year,
        month = month,
        totalIncome = income,
        totalExpense = expense,
        balance = income - expense,
        expenseByCategory = categorySpends,
        weeklyIncomeExpense = weeklyTotals,
    )
}

private fun weekBucket(dateMillis: Long, year: Int, month: Int): Int {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.timeInMillis = dateMillis
    if (cal.get(Calendar.YEAR) != year || cal.get(Calendar.MONTH) != month) return 0
    val day = cal.get(Calendar.DAY_OF_MONTH)
    return ((day - 1) / 7).coerceAtLeast(0)
}
