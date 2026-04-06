package com.financemanager.data.mapper

import com.financemanager.core.ui.utils.formatMonthYear
import com.financemanager.data.local.entity.TransactionWithCategory
import com.financemanager.domain.model.BalanceCurrencyRow
import com.financemanager.domain.model.BalanceDashboard
import com.financemanager.domain.model.GaugeSegment
import com.financemanager.domain.model.HealthTier
import com.financemanager.domain.model.WeeklySpendBar
import com.financemanager.domain.model.TransactionType
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

/** Fixed two-tone arc: lower health (warm) → higher health (purple); position comes from [indicatorFraction]. */
private val HealthGaugeVisualSegments = listOf(
    GaugeSegment(0xFFFFB74D, 0.52),
    GaugeSegment(0xFFAB47BC, 0.48),
)

fun mapBalanceDashboard(
    monthTransactions: List<TransactionWithCategory>,
    weekExpenseTransactions: List<TransactionWithCategory>,
    year: Int,
    monthIndex: Int,
    nowMillis: Long = System.currentTimeMillis(),
): BalanceDashboard {
    var monthIncome = 0.0
    var monthExpense = 0.0
    monthTransactions.forEach { row ->
        val t = row.transaction
        when (TransactionType.fromStorage(t.type)) {
            TransactionType.INCOME -> monthIncome += t.amount
            TransactionType.EXPENSE -> monthExpense += t.amount
        }
    }

    val gaugeSegments = HealthGaugeVisualSegments

    val healthScore = computeHealthScore(monthIncome, monthExpense)
    val tier = healthScoreToTier(healthScore)
    val indicatorFraction = ((healthScore - 300).coerceIn(0, 550)) / 550f

    val weeklyBars = buildWeeklyBars(weekExpenseTransactions)

    val marginBudget = when {
        monthIncome > 0 && monthExpense > 0 -> max(monthIncome, monthExpense * 1.08)
        monthExpense > 0 -> monthExpense * 1.2
        monthIncome > 0 -> monthIncome
        else -> 1.0
    }

    val currencies = listOf(
        BalanceCurrencyRow(
            code = "INR",
            title = "Indian Rupee",
            enabled = true,
            flagEmoji = "\uD83C\uDDEE\uD83C\uDDF3",
        ),
        BalanceCurrencyRow(
            code = "AED",
            title = "UAE Dirham",
            enabled = false,
            flagEmoji = "\uD83C\uDDE6\uD83C\uDDEA",
        ),
    )

    return BalanceDashboard(
        healthScore = healthScore,
        healthTier = tier,
        lastCheckMillis = nowMillis,
        gaugeSegments = gaugeSegments,
        indicatorFraction = indicatorFraction,
        weeklyBars = weeklyBars,
        marginMonthLabel = formatMonthYear(year, monthIndex),
        marginSpent = monthExpense,
        marginBudget = marginBudget,
        currencies = currencies,
    )
}

/**
 * Financial health score (**300–850**), on a credit-score-like scale for the gauge.
 *
 * **Primary signal:** this month’s **income share of total cash flow**:
 * `income / (income + expense)` → mapped linearly toward **[300, 850]** (all income → 850, half/half → 575).
 *
 * **Surplus bonus:** when **income ≥ expenses**, add up to **+25** from the savings rate
 * `(income − expense) / income`, so consistent surpluses sit higher in “excellent”.
 *
 * **Edge cases:** no income and no expenses → **600** (neutral placeholder); expenses with no
 * recorded income → **420** (needs income data).
 */
private fun computeHealthScore(income: Double, expense: Double): Int {
    if (income <= 0.0 && expense <= 0.0) return 600
    if (income <= 0.0) return 420
    val incomeShare = (income / (income + expense)).coerceIn(0.0, 1.0)
    var score = 300.0 + incomeShare * 550.0
    val savingsRate = ((income - expense) / income).coerceIn(0.0, 1.0)
    score += savingsRate * 25.0
    return score.toInt().coerceIn(300, 850)
}

private fun healthScoreToTier(score: Int): HealthTier = when {
    score >= 750 -> HealthTier.EXCELLENT
    score >= 700 -> HealthTier.GOOD
    score >= 640 -> HealthTier.AVERAGE
    score >= 560 -> HealthTier.FAIR
    else -> HealthTier.POOR
}

private fun buildWeeklyBars(rows: List<TransactionWithCategory>): List<WeeklySpendBar> {
    val cal = Calendar.getInstance(Locale.getDefault())
    val sums = linkedMapOf<String, Double>()
    for (row in rows) {
        cal.timeInMillis = row.transaction.date
        val k = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.WEEK_OF_YEAR)}"
        sums[k] = (sums[k] ?: 0.0) + row.transaction.amount
    }
    var pairs = sums.entries.toList()
    if (pairs.size > 6) pairs = pairs.takeLast(6)
    val maxVal = pairs.maxOfOrNull { it.value } ?: 0.0
    val cap = maxVal.coerceAtLeast(1.0) * 1.15
    val padCount = (6 - pairs.size).coerceAtLeast(0)
    return buildList {
        repeat(padCount) {
            add(WeeklySpendBar(label = "—", spent = 0.0, cap = cap))
        }
        pairs.forEachIndexed { idx, e ->
            add(
                WeeklySpendBar(
                    label = "W${idx + 1}",
                    spent = e.value,
                    cap = cap,
                ),
            )
        }
    }
}
