package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LedgerTheme

@Composable
fun BudgetBar(
    spentMinor: Long,
    budgetMinor: Long,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp
) {
    val colors = LedgerTheme.colors

    val ratio = if (budgetMinor > 0) {
        (spentMinor.toFloat() / budgetMinor.toFloat()).coerceAtLeast(0f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "budget_bar_progress"
    )

    val fillColor = when {
        ratio >= 1.0f -> colors.expense
        ratio >= 0.8f -> colors.warning
        else -> colors.accent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(colors.border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(fillColor)
        )
    }
}
