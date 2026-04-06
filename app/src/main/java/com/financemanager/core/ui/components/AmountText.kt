package com.financemanager.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.financemanager.core.ui.theme.ExpenseRose
import com.financemanager.core.ui.theme.IncomeGreen
import com.financemanager.core.ui.utils.formatCurrency
import com.financemanager.core.ui.utils.formatCurrencySigned

@Composable
fun AmountText(
    amount: Double,
    isExpense: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    useSignedPrefix: Boolean = true,
) {
    val color: Color = if (isExpense) ExpenseRose else IncomeGreen
    val text = if (useSignedPrefix) formatCurrencySigned(amount, isExpense) else formatCurrency(amount)
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
    )
}
