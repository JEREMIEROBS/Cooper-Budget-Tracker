package com.example.ui.screens.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.InsightsState
import com.example.ui.components.AmountDisplayMode
import com.example.ui.components.AmountText
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.DateUtils
import com.example.util.MoneyUtils

@Composable
fun InsightsScreen(
    state: InsightsState,
    currencyCode: String,
    onOpenAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors

    LazyColumn(
        modifier = modifier
            .testTag("insights_screen")
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = "Spending Insights",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Text(
                    text = state.currentCycleKey,
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
            }
        }

        // Plain Language Summary Sentence Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CYCLE DIGEST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.summarySentence,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.text,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Month-Over-Month Comparison
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CYCLE COMPARISON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val maxComparison = maxOf(state.currentSpentMinor, state.previousSpentMinor, 1L).toFloat()

                    // Current Month Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Cycle (${state.currentCycleKey})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.text
                            )
                            AmountText(
                                amountMinor = state.currentSpentMinor,
                                currencyCode = currencyCode,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val currentFraction = (state.currentSpentMinor.toFloat() / maxComparison).coerceIn(0.02f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(currentFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Previous Month Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Previous Cycle",
                                fontSize = 13.sp,
                                color = colors.textMuted
                            )
                            AmountText(
                                amountMinor = state.previousSpentMinor,
                                currencyCode = currencyCode,
                                fontSize = 14.sp,
                                overrideColor = colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val prevFraction = (state.previousSpentMinor.toFloat() / maxComparison).coerceIn(0.02f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(prevFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.textMuted.copy(alpha = 0.5f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Delta badge
                    val isSpendingHigher = state.spendingDeltaMinor > 0
                    val isSpendingLower = state.spendingDeltaMinor < 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSpendingLower) colors.accentSoft else if (isSpendingHigher) colors.expense.copy(alpha = 0.1f) else colors.bg
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSpendingHigher) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = "Trend",
                            tint = if (isSpendingLower) colors.accent else if (isSpendingHigher) colors.expense else colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val deltaLabel = when {
                            isSpendingHigher -> "${MoneyUtils.formatMoney(state.spendingDeltaMinor, currencyCode)} more than last month"
                            isSpendingLower -> "${MoneyUtils.formatMoney(-state.spendingDeltaMinor, currencyCode)} less than last month"
                            else -> "Spending exactly equal to last month"
                        }
                        Text(
                            text = deltaLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSpendingLower) colors.accent else if (isSpendingHigher) colors.expense else colors.text
                        )
                    }
                }
            }
        }

        // Horizontal Category Breakdown (NOT PIE CHARTS!)
        item {
            Text(
                text = "Category Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
        }

        if (state.categoryBreakdown.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        message = "No spending breakdown available yet for this month.",
                        actionLabel = "Add Transaction",
                        onAction = onOpenAdd
                    )
                }
            }
        } else {
            items(state.categoryBreakdown) { item ->
                val cat = item.category
                val catColor = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (_: Exception) {
                    colors.accent
                }
                val icon = CategoryIconHelper.getIcon(cat.iconName)
                val pct = (item.ratio * 100).toInt()

                val animatedRatio by animateFloatAsState(
                    targetValue = item.ratio.coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 350),
                    label = "category_bar"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = cat.name,
                                    tint = catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = cat.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.text,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "$pct%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textMuted,
                                modifier = Modifier.padding(end = 10.dp),
                                style = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily)
                            )

                            AmountText(
                                amountMinor = item.spentMinor,
                                currencyCode = currencyCode,
                                mode = AmountDisplayMode.EXPENSE_ALWAYS,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Clean horizontal bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedRatio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(catColor)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
