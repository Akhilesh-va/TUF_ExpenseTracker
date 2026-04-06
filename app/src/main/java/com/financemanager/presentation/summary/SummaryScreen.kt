package com.financemanager.presentation.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financemanager.core.ui.theme.AppRadius
import com.financemanager.core.ui.theme.IncomeGreen
import com.financemanager.core.ui.theme.LightIncomeGreen
import com.financemanager.core.ui.theme.isAppInLightTheme
import com.financemanager.core.ui.utils.formatCurrency
import com.financemanager.core.ui.utils.formatMonthYear
import com.financemanager.presentation.summary.charts.AnimatedExpensePieChart
import com.financemanager.presentation.summary.charts.WeeklyIncomeExpenseBarChart

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    modifier: Modifier = Modifier,
) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Monthly summary",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
            }
            Text(
                text = formatMonthYear(month.year, month.monthIndex),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
            }
        }
        Spacer(Modifier.height(8.dp))
        when (val s = state) {
            SummaryUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is SummaryUiState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            is SummaryUiState.Success -> {
                val data = s.summary
                val incomeTint = if (isAppInLightTheme()) LightIncomeGreen else IncomeGreen
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInHorizontally { it / 8 },
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                SummaryCard(
                                    title = "Income",
                                    amount = data.totalIncome,
                                    color = incomeTint,
                                    modifier = Modifier.weight(1f),
                                )
                                SummaryCard(
                                    title = "Expense",
                                    amount = data.totalExpense,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            SummaryCard(
                                title = "Balance",
                                amount = data.balance,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            Text(
                                text = "Expense by category",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            AnimatedExpensePieChart(
                                segments = data.expenseByCategory,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            Text(
                                text = "Income vs expense by week",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            WeeklyIncomeExpenseBarChart(
                                weeks = data.weeklyIncomeExpense,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            Text(
                                text = "Category breakdown",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(
                            data.expenseByCategory,
                            key = { it.category.id },
                        ) { row ->
                            Column(Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(row.category.icon, style = MaterialTheme.typography.titleMedium)
                                        Text(row.category.name, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Text(formatCurrency(row.amount), style = MaterialTheme.typography.titleSmall)
                                }
                                LinearProgressIndicator(
                                    progress = { row.share.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(8.dp),
                                    color = Color(row.category.color),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(AppRadius.Card))
            .padding(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleLarge,
            color = color,
        )
    }
}
