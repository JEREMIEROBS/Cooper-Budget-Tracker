package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MonthOverviewState
import com.example.ui.theme.LedgerTheme
import com.example.util.MoneyUtils

@Composable
fun CooperCompanionCard(
    state: MonthOverviewState,
    isDarkMode: Boolean = false,
    showCooper: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!showCooper) return

    val colors = LedgerTheme.colors
    val view = LocalView.current

    val isOverBudget = state.totalBudgetedMinor > 0 && state.totalSpentMinor > state.totalBudgetedMinor

    // Mascot Guide Rule: Over-budget and error states do not show Cooper.
    if (isOverBudget) {
        val overAmount = state.totalSpentMinor - state.totalBudgetedMinor
        Card(
            modifier = modifier
                .testTag("cooper_neutral_overbudget_card")
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "STATUS NOTICE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Spending is ${MoneyUtils.formatMoney(overAmount, state.currencyCode)} over budget. It happens. Review entries below.",
                    fontSize = 13.sp,
                    color = colors.text,
                    lineHeight = 18.sp
                )
            }
        }
        return
    }

    // Determine pose based on budget health:
    // RUN with coin pouch when on-track / under budget; SIT when calm / balanced
    val pose = if (state.totalBudgetedMinor > 0 && (state.totalSpentMinor.toFloat() / state.totalBudgetedMinor.toFloat()) < 0.8f) {
        CooperPose.RUN
    } else {
        CooperPose.SIT
    }

    var tipIndex by remember { mutableIntStateOf(0) }

    val tips = remember(state, pose) {
        listOf(
            "You're on track this month. Cooper is guarding your savings!",
            "Cooper's Rule: Log immediately after buying. Takes under 5 seconds.",
            "Review your category limits mid-month to stay ahead of surprises.",
            "Good budgeter! Every dollar assigned has a purpose in your ledger."
        )
    }

    Card(
        modifier = modifier
            .testTag("cooper_companion_card")
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                tipIndex = (tipIndex + 1) % tips.size
            },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Official Cooper Husky Mascot
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Cooper(
                    pose = pose,
                    size = 64.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Speech Bubble / Guidance
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Cooper",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = "Dog paw",
                            tint = colors.accent,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = "Tap for tip",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = tips[tipIndex % tips.size],
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "cooper_tip_anim"
                ) { tipText ->
                    Text(
                        text = tipText,
                        fontSize = 13.sp,
                        color = colors.text,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
