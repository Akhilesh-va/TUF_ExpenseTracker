package com.financemanager.presentation.summary.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.financemanager.core.ui.theme.IncomeGreen
import com.financemanager.core.ui.theme.LightIncomeGreen
import com.financemanager.core.ui.theme.isAppInLightTheme
import com.financemanager.domain.model.CategorySpend
import com.financemanager.domain.model.WeekTotals
import kotlin.math.max

@Composable
fun AnimatedExpensePieChart(
    segments: List<CategorySpend>,
    modifier: Modifier = Modifier,
) {
    val sorted = remember(segments) { segments.filter { it.amount > 0 } }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(sorted) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(900))
    }
    Canvas(modifier = modifier.height(200.dp)) {
        if (sorted.isEmpty()) return@Canvas
        val diameter = size.minDimension
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        var start = -90f
        sorted.forEach { seg ->
            val sweep = 360f * seg.share * progress.value
            drawArc(
                color = Color(seg.category.color),
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = Size(diameter, diameter),
            )
            start += sweep
        }
    }
}

@Composable
fun WeeklyIncomeExpenseBarChart(
    weeks: List<WeekTotals>,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(weeks) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(700))
    }
    val maxVal = remember(weeks) {
        weeks.maxOfOrNull { max(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0
    }
    val incomeBar = if (isAppInLightTheme()) LightIncomeGreen else IncomeGreen
    val expenseBar = MaterialTheme.colorScheme.error
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        weeks.forEach { w ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val incH = (120 * (w.income / maxVal) * progress.value).dp
                    val expH = (120 * (w.expense / maxVal) * progress.value).dp
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(incH.coerceAtLeast(2.dp))
                            .background(incomeBar, RoundedCornerShape(4.dp)),
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(expH.coerceAtLeast(2.dp))
                            .background(expenseBar, RoundedCornerShape(4.dp)),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = w.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
