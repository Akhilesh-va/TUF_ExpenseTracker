package com.financemanager.domain.model

data class CategorySpend(
    val category: Category,
    val amount: Double,
    val share: Float,
)

data class WeekTotals(
    val weekIndex: Int,
    val label: String,
    val income: Double,
    val expense: Double,
)

data class MonthlySummary(
    val year: Int,
    val month: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val expenseByCategory: List<CategorySpend>,
    val weeklyIncomeExpense: List<WeekTotals>,
)
