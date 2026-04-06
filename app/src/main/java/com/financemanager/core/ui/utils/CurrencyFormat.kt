package com.financemanager.core.ui.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private val DefaultFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    currency = Currency.getInstance("INR")
}

fun formatCurrency(amount: Double): String = DefaultFormat.format(amount)

fun formatCurrencySigned(amount: Double, isExpense: Boolean): String {
    val base = formatCurrency(kotlin.math.abs(amount))
    return if (isExpense) "-$base" else "+$base"
}
