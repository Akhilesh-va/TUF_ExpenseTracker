package com.financemanager.presentation.balance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financemanager.core.ui.theme.isAppInLightTheme
import com.financemanager.domain.model.GaugeSegment
import com.financemanager.domain.model.WeeklySpendBar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val ArcTotalDeg = 168f

@Composable
fun BalanceSemiCircleGauge(
    segments: List<GaugeSegment>,
    indicatorFraction: Float,
    centerScore: Int,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val light = isAppInLightTheme()
    val scoreColor = scheme.onSurface
    val dottedArcColor = scheme.outline.copy(alpha = 0.55f)
    val thumbGlowOuter =
        if (light) scheme.primary.copy(alpha = 0.28f) else Color(0x3380DEEA)
    val thumbGlowInner = if (light) scheme.primary else Color(0xFF80DEEA)
    val thumbCenterDot = if (light) scheme.onPrimary else Color.White
    val progress = remember { Animatable(0f) }
    LaunchedEffect(segments, indicatorFraction) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(240.dp),
        ) {
            val width = size.width
            val height = size.height
            val stroke = 28.dp.toPx()
            val r = (min(width, height * 1.15f) / 2f) - stroke / 2
            val cx = width / 2f
            val oy = height - stroke * 0.5f
            val arcCenterY = oy - r
            val topLeft = Offset(cx - r, oy - 2 * r)
            val arcSize = Size(2 * r, 2 * r)
            val anim = progress.value
            var startAngle = 180f
            segments.forEach { seg ->
                val sweep = -ArcTotalDeg * seg.fraction.toFloat() * anim
                drawArc(
                    color = Color(seg.colorArgb),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                startAngle += sweep
            }

            val rDotted = (r - stroke * 0.72f).coerceAtLeast(r * 0.55f)
            val dottedTopLeft = Offset(cx - rDotted, arcCenterY - rDotted)
            val dottedSize = Size(2 * rDotted, 2 * rDotted)
            drawArc(
                color = dottedArcColor,
                startAngle = 180f,
                sweepAngle = -ArcTotalDeg * anim,
                useCenter = false,
                topLeft = dottedTopLeft,
                size = dottedSize,
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f),
                ),
            )

            val t = indicatorFraction.coerceIn(0f, 1f) * anim
            val angleDeg = 180 - ArcTotalDeg * t
            val rad = Math.toRadians(angleDeg.toDouble())
            val ix = (cx + r * cos(rad)).toFloat()
            val iyDot = (arcCenterY + r * sin(rad)).toFloat()
            val thumbCenter = Offset(ix, iyDot)
            drawCircle(
                color = thumbGlowOuter,
                radius = 22.dp.toPx(),
                center = thumbCenter,
            )
            drawCircle(
                color = thumbGlowInner,
                radius = 10.5.dp.toPx(),
                center = thumbCenter,
            )
            drawCircle(
                color = thumbCenterDot,
                radius = 3.2f.dp.toPx(),
                center = thumbCenter,
            )
        }
        Text(
            text = centerScore.toString(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 26.dp),
            color = scoreColor,
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun BalanceSpendingBarChart(
    bars: List<WeeklySpendBar>,
    modifier: Modifier = Modifier,
    chartHeight: androidx.compose.ui.unit.Dp = 168.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val trackFill = scheme.surfaceVariant
    val labelColor = scheme.onSurfaceVariant
    val progress = remember { Animatable(0f) }
    LaunchedEffect(bars) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(750))
    }
    val cap = bars.maxOfOrNull { it.cap }?.takeIf { it > 0 } ?: 1.0
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { bar ->
            val fraction = ((bar.spent / cap).coerceIn(0.0, 1.0) * progress.value).toFloat()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    val th = maxHeight
                    val w = 26.dp
                    Canvas(
                        Modifier
                            .width(w)
                            .height(th),
                    ) {
                        val pw = size.width
                        val ph = size.height
                        val fillH = ph * fraction
                        drawRoundRect(
                            color = trackFill,
                            size = Size(pw, ph),
                            cornerRadius = CornerRadius(pw / 2, pw / 2),
                        )
                        if (fillH > 2f) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFF9C4), Color(0xFF4DB6AC)),
                                    startY = ph - fillH,
                                    endY = ph,
                                ),
                                topLeft = Offset(0f, ph - fillH),
                                size = Size(pw, fillH),
                                cornerRadius = CornerRadius(pw / 2, pw / 2),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
        }
    }
}
