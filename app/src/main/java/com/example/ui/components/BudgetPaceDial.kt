package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.MoneyUtils
import java.util.Calendar

@Composable
fun BudgetPaceDial(
    spentMinor: Long,
    budgetMinor: Long,
    monthKey: String,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors

    val now = Calendar.getInstance()
    val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
    val maxDays = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthTimeProgress = (dayOfMonth.toFloat() / maxDays.toFloat()).coerceIn(0.01f, 1f)

    val budgetSpentRatio = if (budgetMinor > 0) {
        (spentMinor.toFloat() / budgetMinor.toFloat()).coerceAtLeast(0f)
    } else {
        0f
    }

    // Is spending rate lower than time progress?
    val isAheadOfPace = budgetSpentRatio > (monthTimeProgress + 0.08f)
    val isBelowPace = budgetSpentRatio < (monthTimeProgress - 0.05f)

    val paceColor = when {
        budgetSpentRatio >= 1.0f -> colors.expense
        isAheadOfPace -> colors.warning
        else -> colors.accent
    }

    val animatedSpentAngle by animateFloatAsState(
        targetValue = (budgetSpentRatio.coerceIn(0f, 1f) * 240f),
        animationSpec = tween(durationMillis = 500),
        label = "spent_angle"
    )

    val timeAngle = monthTimeProgress * 240f

    // Interactive tap modes: 0 -> Remaining Cash, 1 -> Daily Allowance, 2 -> Savings Pace
    var displayMode by remember { mutableStateOf(0) }

    Row(
        modifier = modifier
            .testTag("budget_pace_dial")
            .fillMaxWidth()
            .clickable { displayMode = (displayMode + 1) % 3 }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Gauge
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                val strokeW = 8.dp.toPx()
                val arcSize = Size(size.width - strokeW, size.height - strokeW)
                val topLeft = Offset(strokeW / 2, strokeW / 2)
                val startAngle = 150f

                // Track Background Arc (240 degrees)
                drawArc(
                    color = colors.border,
                    startAngle = startAngle,
                    sweepAngle = 240f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Time progress tick marker
                val timeMarkerAngle = startAngle + timeAngle
                drawArc(
                    color = colors.textMuted.copy(alpha = 0.5f),
                    startAngle = timeMarkerAngle - 2f,
                    sweepAngle = 4f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW + 4f, cap = StrokeCap.Square)
                )

                // Spent Progress Arc
                drawArc(
                    color = paceColor,
                    startAngle = startAngle,
                    sweepAngle = animatedSpentAngle.coerceAtLeast(2f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            // Percentage in center
            val pct = (budgetSpentRatio * 100).toInt()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$pct%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    style = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily)
                )
                Text(
                    text = "used",
                    fontSize = 9.sp,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Interactive Info Column
        Column(modifier = Modifier.weight(1f)) {
            val statusTitle = when {
                budgetMinor <= 0L -> "No Limit Set"
                budgetSpentRatio >= 1.0f -> "Over Budget"
                isAheadOfPace -> "Pacing Ahead"
                isBelowPace -> "Pacing Calm & Safe"
                else -> "On Target Pace"
            }

            val subtitleText = when (displayMode) {
                0 -> {
                    val remaining = budgetMinor - spentMinor
                    if (remaining >= 0) {
                        "${MoneyUtils.formatMoney(remaining, currencyCode)} left to spend this month"
                    } else {
                        "${MoneyUtils.formatMoney(-remaining, currencyCode)} over total category limit"
                    }
                }
                1 -> {
                    val daysLeft = (maxDays - dayOfMonth + 1).coerceAtLeast(1)
                    val remaining = (budgetMinor - spentMinor).coerceAtLeast(0L)
                    val daily = remaining / daysLeft
                    "${MoneyUtils.formatMoney(daily, currencyCode)} / day for remaining $daysLeft days"
                }
                else -> {
                    "Month is ${(monthTimeProgress * 100).toInt()}% elapsed • Day $dayOfMonth of $maxDays"
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(paceColor, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = paceColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitleText,
                fontSize = 12.sp,
                color = colors.text,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap to switch info view",
                fontSize = 10.sp,
                color = colors.textMuted
            )
        }
    }
}
