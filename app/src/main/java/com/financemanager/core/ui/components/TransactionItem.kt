package com.financemanager.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.financemanager.core.ui.theme.AppRadius
import com.financemanager.core.ui.theme.IncomeGreen
import com.financemanager.core.ui.theme.LightIncomeGreen
import com.financemanager.core.ui.theme.isAppInLightTheme
import com.financemanager.core.ui.utils.formatCurrencySigned
import com.financemanager.core.ui.utils.formatTransactionDate
import com.financemanager.domain.model.Transaction
import com.financemanager.domain.model.TransactionType

@Composable
fun TransactionItem(
    transaction: Transaction,
    onSwipeDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var removed by remember(transaction.id) { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onSwipeDelete(transaction)
                removed = true
                true
            } else false
        },
    )

    AnimatedVisibility(
        visible = !removed,
        exit = shrinkVertically(animationSpec = tween(280)) + fadeOut(animationSpec = tween(200)),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                val color = MaterialTheme.colorScheme.error
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(AppRadius.Card))
                        .background(color),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(end = 20.dp),
                    )
                }
            },
            content = {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppRadius.Card))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = transaction.category.icon, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = transaction.category.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        transaction.note?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = formatTransactionDate(transaction.date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val isExpense = transaction.type == TransactionType.EXPENSE
                    val positive = if (isAppInLightTheme()) LightIncomeGreen else IncomeGreen
                    Text(
                        text = formatCurrencySigned(transaction.amount, isExpense),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isExpense) MaterialTheme.colorScheme.error else positive,
                    )
                }
            },
        )
    }
}
