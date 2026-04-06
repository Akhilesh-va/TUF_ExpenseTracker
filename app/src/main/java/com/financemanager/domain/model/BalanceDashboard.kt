package com.financemanager.domain.model

enum class HealthTier {
    EXCELLENT,
    GOOD,
    AVERAGE,
    FAIR,
    POOR,
}

data class GaugeSegment(
    val colorArgb: Long,
    val fraction: Double,
)

data class WeeklySpendBar(
    val label: String,
    val spent: Double,
    val cap: Double,
)

data class BalanceCurrencyRow(
    val code: String,
    val title: String,
    val enabled: Boolean,
    val flagEmoji: String,
)

data class BalanceDashboard(
    val healthScore: Int,
    val healthTier: HealthTier,
    val lastCheckMillis: Long,
    val gaugeSegments: List<GaugeSegment>,
    val indicatorFraction: Float,
    val weeklyBars: List<WeeklySpendBar>,
    val marginMonthLabel: String,
    val marginSpent: Double,
    val marginBudget: Double,
    val currencies: List<BalanceCurrencyRow>,
)
