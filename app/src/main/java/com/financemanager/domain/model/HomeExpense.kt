package com.financemanager.domain.model

enum class ExpenseTrend {
    LESS_THAN_PREVIOUS,
    MORE_THAN_PREVIOUS,
    SAME_AS_PREVIOUS,
    NEW_IN_PERIOD,
}

data class ExpenseCategoryRow(
    val category: Category,
    val amount: Double,
    val trend: ExpenseTrend,
)
