package com.financemanager.presentation.balance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financemanager.R
import com.financemanager.core.ui.components.AppTopBar
import com.financemanager.core.ui.utils.formatCurrency
import com.financemanager.core.ui.utils.formatDayMonthShort
import com.financemanager.domain.model.BalanceCurrencyRow
import com.financemanager.domain.model.HealthTier
import com.financemanager.core.ui.theme.isAppInLightTheme

@Composable
fun BalanceScreen(
    viewModel: BalanceViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = state) {
        BalanceUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is BalanceUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is BalanceUiState.Success -> {
            val d = s.data
            val light = isAppInLightTheme()
            val scheme = MaterialTheme.colorScheme
            val cap = d.weeklyBars.maxOfOrNull { it.cap }?.takeIf { it > 0 } ?: 1.0
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(scheme.background),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    AppTopBar(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                item {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.balance_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBackground,
                        )
                        Text(
                            text = stringResource(R.string.balance_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    Column(
                        Modifier.padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BalanceSemiCircleGauge(
                            segments = d.gaugeSegments,
                            indicatorFraction = d.indicatorFraction,
                            centerScore = d.healthScore,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = healthTierLine(d.healthTier),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = scheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.balance_last_check,
                                formatDayMonthShort(d.lastCheckMillis),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.balance_available_currencies),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                items(
                    d.currencies,
                    key = { it.code },
                ) { row ->
                    CurrencyCard(
                        row = row,
                        light = light,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.balance_margin_label,
                                d.marginMonthLabel,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${formatCurrency(d.marginSpent)} / ${formatCurrency(d.marginBudget)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (light) scheme.primary else Color(0xFFCE93D8),
                        )
                    }
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(188.dp)
                            .padding(start = 12.dp, end = 16.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(
                            modifier = Modifier
                                .width(46.dp)
                                .height(160.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                formatCurrency(cap),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                            Text(
                                formatCurrency(cap * 0.5),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                            Text(
                                formatCurrency(0.0),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        BalanceSpendingBarChart(
                            bars = d.weeklyBars,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp),
                            chartHeight = 160.dp,
                        )
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
private fun healthTierLine(tier: HealthTier): String = when (tier) {
    HealthTier.EXCELLENT -> stringResource(R.string.health_line_excellent)
    HealthTier.GOOD -> stringResource(R.string.health_line_good)
    HealthTier.AVERAGE -> stringResource(R.string.health_line_average)
    HealthTier.FAIR -> stringResource(R.string.health_line_fair)
    HealthTier.POOR -> stringResource(R.string.health_line_poor)
}

@Composable
private fun CurrencyCard(
    row: BalanceCurrencyRow,
    light: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val cardBg = if (light) scheme.surface else Color(0xFF1A1A1A)
    val cardStroke = if (light) scheme.outline else Color(0xFF2C2C2C)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardStroke, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = row.flagEmoji, fontSize = 28.sp)
            Column {
                Text(
                    text = row.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = row.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = scheme.onSurface.copy(alpha = 0.85f),
            )
            if (!row.enabled) {
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (light) scheme.primary else Color(0xFF2C2C2C),
                        contentColor = if (light) scheme.onPrimary else Color.White,
                    ),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.balance_enable))
                }
            } else {
                Text(
                    text = stringResource(R.string.balance_primary),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}
