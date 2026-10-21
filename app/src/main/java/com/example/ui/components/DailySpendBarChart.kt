package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.MoneyUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DaySpendBucket(
    val dayOfMonth: Int,
    val dayLabel: String,
    val dateFull: String,
    val totalMinor: Long,
    val transactionCount: Int,
    val isToday: Boolean
)

@Composable
fun DailySpendBarChart(
    transactions: List<TransactionEntity>,
    monthKey: String,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors

    // Compute daily buckets for the last 14 days or days elapsed in month
    val daysData = remember(transactions, monthKey) {
        val cal = Calendar.getInstance()
        val nowCal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val fullFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val expenseTxs = transactions.filter { it.kind == "expense" }

        // Group by day of month
        val dayMap = expenseTxs.groupBy { tx ->
            cal.timeInMillis = tx.occurredAt
            cal.get(Calendar.DAY_OF_MONTH)
        }

        // Generate last 10 days
        val list = mutableListOf<DaySpendBucket>()
        val currentDay = nowCal.get(Calendar.DAY_OF_MONTH)
        val startDay = (currentDay - 9).coerceAtLeast(1)

        for (d in startDay..currentDay) {
            val txsForDay = dayMap[d] ?: emptyList()
            val sum = txsForDay.sumOf { it.amountMinor }

            cal.set(Calendar.DAY_OF_MONTH, d)
            val isToday = d == currentDay

            list.add(
                DaySpendBucket(
                    dayOfMonth = d,
                    dayLabel = dayOfWeekFormat.format(cal.time).take(1),
                    dateFull = fullFormat.format(cal.time),
                    totalMinor = sum,
                    transactionCount = txsForDay.size,
                    isToday = isToday
                )
            )
        }
        list
    }

    val maxDaySpend = remember(daysData) {
        daysData.maxOfOrNull { it.totalMinor }?.coerceAtLeast(1000L) ?: 1000L
    }

    var selectedBucket by remember(daysData) {
        mutableStateOf(daysData.lastOrNull { it.isToday } ?: daysData.lastOrNull())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_spend_bar_chart")
    ) {
        // Interactive inspection header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DAILY SPEND VELOCITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = colors.textMuted
                )
                Text(
                    text = selectedBucket?.let { "${it.dateFull}${if (it.isToday) " (Today)" else ""}" } ?: "Tap a bar to inspect",
                    fontSize = 13.sp,
                    color = colors.text
                )
            }

            if (selectedBucket != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = MoneyUtils.formatMoney(selectedBucket!!.totalMinor, currencyCode),
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (selectedBucket!!.totalMinor > 0) colors.expense else colors.textMuted
                        )
                    )
                    Text(
                        text = "${selectedBucket!!.transactionCount} entries",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Bars Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.bg)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            daysData.forEach { bucket ->
                val isSelected = selectedBucket?.dayOfMonth == bucket.dayOfMonth
                val ratio = (bucket.totalMinor.toFloat() / maxDaySpend.toFloat()).coerceIn(0.05f, 1.0f)

                val animatedHeightFraction by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(durationMillis = 300),
                    label = "bar_height_${bucket.dayOfMonth}"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { selectedBucket = bucket }
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Bar pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedHeightFraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                when {
                                    isSelected -> colors.accent
                                    bucket.isToday -> colors.accent.copy(alpha = 0.75f)
                                    bucket.totalMinor > 0 -> colors.textMuted.copy(alpha = 0.35f)
                                    else -> colors.border
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) colors.text else Color.Transparent,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Day label
                    Text(
                        text = "${bucket.dayOfMonth}",
                        fontSize = 10.sp,
                        fontWeight = if (isSelected || bucket.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colors.accent else if (bucket.isToday) colors.text else colors.textMuted
                    )
                }
            }
        }
    }
}
