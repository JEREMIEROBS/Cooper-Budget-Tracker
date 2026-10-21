package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.Localization
import com.example.util.MoneyUtils

data class SafeToSpendState(
    val safeTodayMinor: Long,
    val isOverBudget: Boolean,
    val overAmountMinor: Long,
    val remainingBudgetMinor: Long,
    val upcomingBillsMinor: Long,
    val daysRemaining: Int,
    val cycleLabel: String,
    val currencyCode: String
)

@Composable
fun SafeToSpendCard(
    state: SafeToSpendState,
    hideBalances: Boolean = false,
    lang: String = "en",
    showCooper: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors

    // CRITICAL MASCOT RULE: No mascot shown in the over-budget state!
    val showDog = showCooper && !state.isOverBudget

    Card(
        modifier = modifier
            .testTag("safe_to_spend_card")
            .fillMaxWidth()
            .border(
                1.dp,
                if (state.isOverBudget) colors.expense.copy(alpha = 0.4f) else colors.accent.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (state.isOverBudget) colors.expense.copy(alpha = 0.06f) else colors.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.isOverBudget) colors.expense else colors.accent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.t("safe_to_spend_title", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.1.sp,
                        color = if (state.isOverBudget) colors.expense else colors.accent
                    )
                }

                // Cycle tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.border.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = state.cycleLabel,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (state.isOverBudget) {
                        // Calm, non-shaming copy: "You're ₱350 over for this cycle." (never a negative number)
                        val formattedOver = if (hideBalances) "••••" else MoneyUtils.formatMoney(
                            state.overAmountMinor,
                            state.currencyCode
                        )
                        Text(
                            text = Localization.t("safe_to_spend_over", lang, formattedOver),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "It happens. Pace your spending for the remaining ${state.daysRemaining} days.",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    } else {
                        // Normal copy: "You can spend ₱420 today."
                        val formattedSafe = if (hideBalances) "••••" else MoneyUtils.formatMoney(
                            state.safeTodayMinor,
                            state.currencyCode
                        )
                        Text(
                            text = Localization.t("safe_to_spend_normal", lang, formattedSafe),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedRemaining = if (hideBalances) "••••" else MoneyUtils.formatMoney(
                            state.remainingBudgetMinor,
                            state.currencyCode
                        )
                        Text(
                            text = Localization.t(
                                "safe_to_spend_sub",
                                lang,
                                formattedRemaining,
                                state.daysRemaining
                            ),
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    if (state.upcomingBillsMinor > 0L) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val formattedBills = if (hideBalances) "••••" else MoneyUtils.formatMoney(
                                state.upcomingBillsMinor,
                                state.currencyCode
                            )
                            Text(
                                text = Localization.t("safe_to_spend_bills_deducted", lang, formattedBills),
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }

                // Mascot only in normal/under-budget state (never in over-budget state!)
                if (showDog) {
                    Cooper(
                        pose = CooperPose.HEAD,
                        size = 54.dp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
