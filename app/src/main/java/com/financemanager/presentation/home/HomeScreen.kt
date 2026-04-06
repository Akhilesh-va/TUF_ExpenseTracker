package com.financemanager.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financemanager.R
import com.financemanager.core.ui.components.AnimatedPillSwitcher
import com.financemanager.core.ui.components.AppTopBar
import com.financemanager.core.ui.utils.HomeExpensePeriod
import com.financemanager.core.ui.utils.formatCurrency
import com.financemanager.domain.model.ExpenseCategoryRow
import com.financemanager.domain.model.ExpenseTrend
import com.financemanager.core.ui.theme.isAppInLightTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val period by viewModel.expensePeriod.collectAsStateWithLifecycle()

    when (val s = state) {
        HomeUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is HomeUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is HomeUiState.Success -> {
            val light = isAppInLightTheme()
            val balanceTarget = s.content.summary.totalBalance.toFloat()
            val animatedBalance by animateFloatAsState(
                targetValue = balanceTarget,
                animationSpec = tween(durationMillis = 900),
                label = "balanceHint",
            )
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(320)) + slideInVertically { it / 6 },
            ) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        AppTopBar(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    }
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.home_greeting,
                                    stringResource(R.string.home_user_name),
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = stringResource(R.string.home_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(
                                    R.string.home_balance_label,
                                    formatCurrency(animatedBalance.toDouble()),
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            )
                        }
                    }
                    item {
                        Image(
                            painter = painterResource(R.drawable.card),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                                .shadow(18.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .aspectRatio(1.586f),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.home_your_expenses),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            AnimatedPillSwitcher(
                                options = listOf(
                                    HomeExpensePeriod.WEEKLY to stringResource(R.string.home_weekly),
                                    HomeExpensePeriod.MONTHLY to stringResource(R.string.home_monthly),
                                ),
                                selected = period,
                                onSelect = { viewModel.setExpensePeriod(it) },
                                modifier = Modifier.fillMaxWidth(),
                                selectedContainerColor =
                                    if (light) MaterialTheme.colorScheme.surface else Color.White,
                                selectedContentColor =
                                    if (light) MaterialTheme.colorScheme.primary else Color.Black,
                                trackColor =
                                    if (light) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        Color(0xFF2C2C2C)
                                    },
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (s.content.expenseRows.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.home_expenses_empty),
                                modifier = Modifier.padding(horizontal = 20.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(
                            s.content.expenseRows,
                            key = { it.category.id },
                        ) { row ->
                            HomeExpenseCategoryRow(
                                row = row,
                                period = period,
                                light = light,
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                        }
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HomeExpenseCategoryRow(
    row: ExpenseCategoryRow,
    period: HomeExpensePeriod,
    light: Boolean,
    modifier: Modifier = Modifier,
) {
    val rowBg =
        if (light) MaterialTheme.colorScheme.surface else Color(0xFF141414)
    val rowStroke =
        if (light) MaterialTheme.colorScheme.outline else Color(0xFF2C2C2C)
    val iconBg =
        if (light) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else Color.White.copy(alpha = 0.08f)
    val iconRing =
        if (light) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else Color.White.copy(alpha = 0.9f)
    val primaryText = MaterialTheme.colorScheme.onSurface
    val amountChipBg =
        if (light) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF2C2C2C)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(rowBg)
            .border(1.dp, rowStroke, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(2.dp, iconRing, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = row.category.icon,
                    fontSize = 22.sp,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = row.category.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = trendText(row.trend, period),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = formatCurrency(row.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(amountChipBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun trendText(trend: ExpenseTrend, period: HomeExpensePeriod): String {
    val monthly = period == HomeExpensePeriod.MONTHLY
    return when (trend) {
        ExpenseTrend.LESS_THAN_PREVIOUS -> if (monthly) {
            stringResource(R.string.trend_less_month)
        } else {
            stringResource(R.string.trend_less_week)
        }
        ExpenseTrend.MORE_THAN_PREVIOUS -> if (monthly) {
            stringResource(R.string.trend_more_month)
        } else {
            stringResource(R.string.trend_more_week)
        }
        ExpenseTrend.SAME_AS_PREVIOUS -> if (monthly) {
            stringResource(R.string.trend_same_month)
        } else {
            stringResource(R.string.trend_same_week)
        }
        ExpenseTrend.NEW_IN_PERIOD -> if (monthly) {
            stringResource(R.string.trend_new_month)
        } else {
            stringResource(R.string.trend_new_week)
        }
    }
}
