package com.example.ui.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.CategorySpendProgress
import com.example.ui.MonthOverviewState
import com.example.ui.components.AmountText
import com.example.ui.components.BudgetBar
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.CooperPose
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.DateUtils
import com.example.util.MoneyUtils

@Composable
fun BudgetsScreen(
    state: MonthOverviewState,
    onSetBudget: (categoryId: String, amountStr: String) -> Unit,
    onCopyLastMonth: () -> Unit,
    showCooper: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors
    var editingCategoryProgress by remember { mutableStateOf<CategorySpendProgress?>(null) }
    var editAmountText by remember { mutableStateOf("") }
    var copyMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .testTag("budgets_screen")
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Screen Header + Copy Last Cycle action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cycle Budgets",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Text(
                        text = state.cycleLabel.ifBlank { state.cycleKey },
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }

                OutlinedButton(
                    onClick = {
                        onCopyLastMonth()
                        copyMessage = "Copied budgets from previous cycle."
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.accent
                    ),
                    modifier = Modifier.testTag("copy_last_month_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Previous", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (copyMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accentSoft)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = copyMessage!!,
                        fontSize = 12.sp,
                        color = colors.accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Total Budget Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL BUDGETED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AmountText(
                                amountMinor = state.totalBudgetedMinor,
                                currencyCode = state.currencyCode,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "SPENT SO FAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AmountText(
                                amountMinor = state.totalSpentMinor,
                                currencyCode = state.currencyCode,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                overrideColor = if (state.totalSpentMinor > state.totalBudgetedMinor && state.totalBudgetedMinor > 0) colors.expense else colors.text
                            )
                        }
                    }

                    if (state.totalBudgetedMinor > 0L) {
                        Spacer(modifier = Modifier.height(14.dp))
                        BudgetBar(
                            spentMinor = state.totalSpentMinor,
                            budgetMinor = state.totalBudgetedMinor,
                            height = 8.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val ratioPct = ((state.totalSpentMinor.toDouble() / state.totalBudgetedMinor.toDouble()) * 100).toInt()
                            Text(
                                text = "$ratioPct% allocated used",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                            val remaining = state.totalBudgetedMinor - state.totalSpentMinor
                            Text(
                                text = if (remaining >= 0) {
                                    "${MoneyUtils.formatMoney(remaining, state.currencyCode)} remaining"
                                } else {
                                    "${MoneyUtils.formatMoney(-remaining, state.currencyCode)} over budget"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (remaining < 0) colors.expense else colors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // Section label
        item {
            Text(
                text = "Expense Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
        }

        if (state.totalBudgetedMinor == 0L) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        message = "Set your first budget. Tap any category below to set a monthly limit.",
                        cooperPose = CooperPose.SIT,
                        showCooper = showCooper
                    )
                }
            }
        }

        // Categories List
        items(state.allCategoryProgress) { item ->
            val cat = item.category
            val catColor = try {
                Color(android.graphics.Color.parseColor(cat.colorHex))
            } catch (_: Exception) {
                colors.accent
            }
            val icon = CategoryIconHelper.getIcon(cat.iconName)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .clickable {
                        editingCategoryProgress = item
                        editAmountText = if (item.budgetMinor > 0) {
                            MoneyUtils.formatPlain(item.budgetMinor, state.currencyCode)
                        } else {
                            ""
                        }
                    }
                    .testTag("budget_row_${cat.id}"),
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = cat.name,
                                tint = catColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.text
                            )
                            Text(
                                text = if (item.budgetMinor > 0) {
                                    "Limit: ${MoneyUtils.formatMoney(item.budgetMinor, state.currencyCode)}"
                                } else {
                                    "No limit set (Tap to set)"
                                },
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }

                        // Spent Amount
                        Column(horizontalAlignment = Alignment.End) {
                            AmountText(
                                amountMinor = item.spentMinor,
                                currencyCode = state.currencyCode,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (item.budgetMinor > 0) {
                                val remaining = item.remainingMinor
                                Text(
                                    text = if (remaining >= 0) {
                                        "${MoneyUtils.formatMoney(remaining, state.currencyCode)} left"
                                    } else {
                                        "${MoneyUtils.formatMoney(-remaining, state.currencyCode)} over"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (remaining < 0) colors.expense else colors.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = colors.textMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (item.budgetMinor > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        BudgetBar(
                            spentMinor = item.spentMinor,
                            budgetMinor = item.budgetMinor,
                            height = 6.dp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Edit Budget Dialog
    if (editingCategoryProgress != null) {
        val cat = editingCategoryProgress!!.category
        AlertDialog(
            onDismissRequest = { editingCategoryProgress = null },
            title = {
                Text(
                    text = "Set Monthly Budget for ${cat.name}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter target limit for ${state.cycleLabel.ifBlank { state.cycleKey }}. Enter 0 to remove.",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { editAmountText = it },
                        placeholder = { Text("0.00", fontFamily = MonoFontFamily) },
                        prefix = {
                            Text(
                                MoneyUtils.getCurrencySymbol(state.currencyCode),
                                fontFamily = MonoFontFamily,
                                color = colors.textMuted
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_amount_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetBudget(cat.id, editAmountText)
                        editingCategoryProgress = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_budget_button")
                ) {
                    Text("Save Budget")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingCategoryProgress = null }
                ) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surface
        )
    }
}
