package com.example.ui.screens.overview

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.components.SafeToSpendCard
import com.example.ui.components.SafeToSpendState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.CategorySpendProgress
import com.example.ui.MonthOverviewState
import com.example.ui.components.AmountDisplayMode
import com.example.ui.components.AmountText
import com.example.ui.components.BudgetBar
import com.example.ui.components.BudgetPaceDial
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.CooperBrandHeader
import com.example.ui.components.CooperCompanionCard
import com.example.ui.components.CooperDogIcon
import com.example.ui.components.CooperLogoShowcaseDialog
import com.example.ui.components.CooperMood
import com.example.ui.components.CooperPose
import com.example.ui.components.DailySpendBarChart
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TransactionRow
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.DateUtils
import com.example.util.MoneyUtils

@Composable
fun OverviewScreen(
    state: MonthOverviewState,
    safeToSpendState: SafeToSpendState,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToCurrentMonth: () -> Unit,
    onOpenAdd: () -> Unit,
    onViewAllTransactions: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onViewBudgets: () -> Unit,
    onViewInsights: () -> Unit,
    onViewAccounts: () -> Unit = {},
    onViewBills: () -> Unit = {},
    onViewGoals: () -> Unit = {},
    onViewUtang: () -> Unit = {},
    showCooper: Boolean = true,
    hideBalances: Boolean = false,
    onToggleHideBalances: () -> Unit = {},
    lang: String = "en",
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors
    val view = LocalView.current
    val isCurrentMonth = state.daysRemaining > 0

    // Interactive state for expanded category in dashboard
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }
    var isShowcaseOpen by remember { mutableStateOf(false) }

    // Dialog displaying Light & Dark mode versions of Cooper logo icon and poses
    CooperLogoShowcaseDialog(
        isOpen = isShowcaseOpen,
        isDarkMode = isDarkMode,
        onDismiss = { isShowcaseOpen = false },
        onToggleTheme = onToggleTheme
    )

    LazyColumn(
        modifier = modifier
            .testTag("overview_screen")
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Brand Header with Dog Cooper Logo + Light/Dark mode toggle + Privacy toggle
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CooperBrandHeader(
                    isDarkMode = isDarkMode,
                    onToggleTheme = onToggleTheme,
                    onOpenLogoShowcase = { isShowcaseOpen = true },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onToggleHideBalances,
                    modifier = Modifier.size(36.dp).testTag("hide_balances_button")
                ) {
                    Icon(
                        imageVector = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (hideBalances) "Show Balances" else "Hide Balances",
                        tint = if (hideBalances) colors.accent else colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Feature 2: Prominent Safe-to-Spend Today Card (Mascot rules: No mascot in over-budget state)
        item {
            SafeToSpendCard(
                state = safeToSpendState,
                hideBalances = hideBalances,
                lang = lang,
                showCooper = showCooper
            )
        }

        // 3. Cycle Selector Navigation Pill (Feature 1: Pay-cycle budgets)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Cycle",
                        tint = colors.text,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onResetToCurrentMonth()
                    }
                ) {
                    Text(
                        text = state.cycleLabel.ifBlank { state.cycleKey },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${state.daysRemaining}d left",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Cycle",
                        tint = colors.text,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Interactive Dog Cooper Companion Mascot Card (only if NOT over-budget)
        if (!safeToSpendState.isOverBudget) {
            item {
                CooperCompanionCard(
                    state = state,
                    isDarkMode = isDarkMode,
                    showCooper = showCooper
                )
            }
        }

        // 4. Hero Card: Spent This Month + Interactive Budget Pace Dial
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "SPENT THIS MONTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                color = colors.textMuted
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            AmountText(
                                amountMinor = state.totalSpentMinor,
                                currencyCode = state.currencyCode,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.SemiBold,
                                overrideColor = colors.text,
                                testTag = "total_spent_hero"
                            )

                            if (state.totalBudgetedMinor > 0L) {
                                Text(
                                    text = "of ${MoneyUtils.formatMoney(state.totalBudgetedMinor, state.currencyCode)} budgeted",
                                    fontSize = 13.sp,
                                    color = colors.textMuted,
                                    style = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily)
                                )
                            }
                        }

                        // Cash flow quick tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (state.netBalanceMinor >= 0) colors.accentSoft else colors.expense.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (state.netBalanceMinor >= 0) "Surplus" else "Deficit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.netBalanceMinor >= 0) colors.accent else colors.expense
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive Gauge Dial
                    BudgetPaceDial(
                        spentMinor = state.totalSpentMinor,
                        budgetMinor = state.totalBudgetedMinor,
                        monthKey = state.cycleKey,
                        currencyCode = state.currencyCode
                    )
                }
            }
        }

        // 5. Quick Interactive Action Pills Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    InteractiveActionPill(
                        icon = Icons.Default.Add,
                        label = "Log Expense",
                        isPrimary = true,
                        onClick = onOpenAdd,
                        testTag = "pill_log_expense"
                    )
                }
                item {
                    InteractiveActionPill(
                        icon = Icons.Default.Savings,
                        label = "Set Budgets",
                        isPrimary = false,
                        onClick = onViewBudgets,
                        testTag = "pill_set_budgets"
                    )
                }
                item {
                    InteractiveActionPill(
                        icon = Icons.Default.BarChart,
                        label = "Insights",
                        isPrimary = false,
                        onClick = onViewInsights,
                        testTag = "pill_view_insights"
                    )
                }
                item {
                    InteractiveActionPill(
                        icon = Icons.Default.Pets,
                        label = "Cooper Mascot",
                        isPrimary = false,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isShowcaseOpen = true
                        },
                        testTag = "pill_pet_cooper"
                    )
                }
            }
        }

        // 6. Interactive Daily Spend Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DailySpendBarChart(
                        transactions = state.recentTransactions,
                        monthKey = state.cycleKey,
                        currencyCode = state.currencyCode
                    )
                }
            }
        }

        // 7. Category Budgets Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Limits",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Text(
                    text = "Manage all",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.accent,
                    modifier = Modifier
                        .clickable(onClick = onViewBudgets)
                        .padding(4.dp)
                )
            }
        }

        if (state.topCategories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        message = "No category budgets or spending logged this month.",
                        actionLabel = "Configure Budgets",
                        onAction = onViewBudgets
                    )
                }
            }
        } else {
            items(state.topCategories) { catProgress ->
                val isExpanded = expandedCategoryId == catProgress.category.id

                InteractiveCategoryCard(
                    progress = catProgress,
                    currencyCode = state.currencyCode,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedCategoryId = if (isExpanded) null else catProgress.category.id
                    },
                    onAdjustBudget = onViewBudgets
                )
            }
        }

        // 8. Recent Transactions Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onViewAllTransactions)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "See all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "See all",
                        tint = colors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (state.recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        message = "Nothing logged yet. Add your first expense.",
                        actionLabel = "Add Expense",
                        onAction = onOpenAdd
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        state.recentTransactions.forEachIndexed { index, tx ->
                            val category = state.categoriesMap[tx.categoryId]
                            TransactionRow(
                                transaction = tx,
                                category = category,
                                currencyCode = state.currencyCode,
                                hideBalances = hideBalances,
                                onClick = { onTransactionClick(tx) }
                            )
                            if (index < state.recentTransactions.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(colors.border)
                                        .padding(start = 66.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
fun InteractiveActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "action_pill"
) {
    val colors = LedgerTheme.colors

    Row(
        modifier = modifier
            .testTag(testTag)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPrimary) colors.accent else colors.surface)
            .border(1.dp, if (isPrimary) colors.accent else colors.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isPrimary) Color.White else colors.text,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPrimary) Color.White else colors.text
        )
    }
}

@Composable
fun InteractiveCategoryCard(
    progress: CategorySpendProgress,
    currencyCode: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAdjustBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors
    val cat = progress.category

    val catColor = try {
        Color(android.graphics.Color.parseColor(cat.colorHex))
    } catch (_: Exception) {
        colors.accent
    }

    val icon = CategoryIconHelper.getIcon(cat.iconName)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleExpand),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = cat.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                    modifier = Modifier.weight(1f)
                )

                // Spent / Budget text
                Row(verticalAlignment = Alignment.Bottom) {
                    AmountText(
                        amountMinor = progress.spentMinor,
                        currencyCode = currencyCode,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (progress.budgetMinor > 0L) {
                        Text(
                            text = " / ${MoneyUtils.formatMoney(progress.budgetMinor, currencyCode)}",
                            fontSize = 13.sp,
                            color = colors.textMuted,
                            style = androidx.compose.ui.text.TextStyle(fontFamily = MonoFontFamily)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (progress.budgetMinor > 0L) {
                Spacer(modifier = Modifier.height(10.dp))
                BudgetBar(
                    spentMinor = progress.spentMinor,
                    budgetMinor = progress.budgetMinor,
                    height = 6.dp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val pct = (progress.ratio * 100).toInt()
                    Text(
                        text = "$pct% used",
                        fontSize = 12.sp,
                        color = if (progress.ratio >= 1.0f) colors.expense else colors.textMuted
                    )

                    val remainingText = if (progress.remainingMinor >= 0) {
                        "${MoneyUtils.formatMoney(progress.remainingMinor, currencyCode)} left"
                    } else {
                        "${MoneyUtils.formatMoney(-progress.remainingMinor, currencyCode)} over"
                    }

                    Text(
                        text = remainingText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (progress.remainingMinor < 0) colors.expense else colors.textMuted
                    )
                }
            }

            // Expanded Interactive Quick Actions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.bg)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (progress.budgetMinor > 0) "Status: ${(progress.ratio * 100).toInt()}% consumed" else "No monthly limit set",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.text
                        )

                        Text(
                            text = "Edit Limit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent,
                            modifier = Modifier
                                .clickable(onClick = onAdjustBudget)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
