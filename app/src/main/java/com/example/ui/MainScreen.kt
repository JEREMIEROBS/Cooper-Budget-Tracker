package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.screens.accounts.AccountsScreen
import com.example.ui.screens.add.AddTransactionSheet
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.budgets.BudgetsScreen
import com.example.ui.screens.categories.ManageCategoriesDialog
import com.example.ui.screens.goals.GoalsScreen
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.overview.OverviewScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.screens.utang.UtangScreen
import com.example.ui.theme.LedgerTheme
import com.example.util.Localization
import kotlinx.coroutines.launch

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    OVERVIEW("Overview", Icons.Filled.Home, Icons.Outlined.Home),
    TRANSACTIONS("Entries", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    ACCOUNTS("Accounts", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    BUDGETS("Budgets", Icons.Filled.Savings, Icons.Outlined.Savings),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

enum class SubScreen {
    BILLS,
    GOALS,
    UTANG,
    INSIGHTS
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    isDarkMode: Boolean = false
) {
    val colors = LedgerTheme.colors
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(ScreenTab.OVERVIEW) }
    var currentSubScreen by remember { mutableStateOf<SubScreen?>(null) }
    var isAddSheetOpen by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var isManageCategoriesOpen by remember { mutableStateOf(false) }

    // Collect StateFlows
    val overviewState by viewModel.overviewState.collectAsState()
    val safeToSpendState by viewModel.safeToSpendState.collectAsState()
    val insightsState by viewModel.insightsState.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val showCooper by viewModel.showCooper.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val hideBalances by viewModel.hideBalances.collectAsState()
    val payCycleType by viewModel.payCycleType.collectAsState()
    val payCycleStartDay by viewModel.payCycleStartDay.collectAsState()

    val allAccounts by viewModel.allAccounts.collectAsState()
    val activeBills by viewModel.activeBills.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()
    val activeDebts by viewModel.activeDebts.collectAsState()
    val settledDebts by viewModel.settledDebts.collectAsState()
    val topFavorites by viewModel.topFavorites.collectAsState()
    val savedFilters by viewModel.savedFilters.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterCategoryId by viewModel.filterCategoryId.collectAsState()
    val filterAccountId by viewModel.filterAccountId.collectAsState()
    val filterKind by viewModel.filterKind.collectAsState()

    // BackHandler to handle subscreen pop or return to OVERVIEW
    BackHandler(enabled = currentSubScreen != null || currentTab != ScreenTab.OVERVIEW) {
        if (currentSubScreen != null) {
            currentSubScreen = null
        } else {
            currentTab = ScreenTab.OVERVIEW
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = colors.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransaction = null
                    isAddSheetOpen = true
                },
                containerColor = colors.accent,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .size(54.dp)
                    .testTag("quick_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Log Entry",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = colors.border)
                    .testTag("bottom_nav_bar")
            ) {
                ScreenTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab && currentSubScreen == null
                    val localizedTitle = when (tab) {
                        ScreenTab.OVERVIEW -> Localization.t("tab_overview", appLanguage)
                        ScreenTab.TRANSACTIONS -> Localization.t("tab_entries", appLanguage)
                        ScreenTab.ACCOUNTS -> Localization.t("tab_accounts", appLanguage)
                        ScreenTab.BUDGETS -> Localization.t("tab_budgets", appLanguage)
                        ScreenTab.SETTINGS -> Localization.t("tab_settings", appLanguage)
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentSubScreen = null
                            currentTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = localizedTitle,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = localizedTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.accent,
                            selectedTextColor = colors.accent,
                            unselectedIconColor = colors.textMuted,
                            unselectedTextColor = colors.textMuted,
                            indicatorColor = colors.accentSoft
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentSubScreen != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Subscreen Header with Back Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface)
                            .border(1.dp, colors.border)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentSubScreen = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.text
                            )
                        }
                        Text(
                            text = when (currentSubScreen) {
                                SubScreen.BILLS -> Localization.t("bills_title", appLanguage)
                                SubScreen.GOALS -> Localization.t("goals_title", appLanguage)
                                SubScreen.UTANG -> Localization.t("utang_title", appLanguage)
                                SubScreen.INSIGHTS -> "Spending Insights"
                                null -> ""
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                    }

                    when (currentSubScreen) {
                        SubScreen.BILLS -> {
                            BillsScreen(
                                bills = activeBills,
                                currentCycleKey = overviewState.cycleKey,
                                currencyCode = currencyCode,
                                hideBalances = hideBalances,
                                lang = appLanguage,
                                onPayBill = { billId, cycleKey ->
                                    viewModel.payBill(billId, cycleKey)
                                },
                                onAddBill = { bill ->
                                    viewModel.addBill(bill)
                                }
                            )
                        }
                        SubScreen.GOALS -> {
                            GoalsScreen(
                                goals = allGoals,
                                currencyCode = currencyCode,
                                hideBalances = hideBalances,
                                showCooper = showCooper,
                                lang = appLanguage,
                                onAddFunds = { goalId, minor ->
                                    viewModel.addGoalFunds(goalId, minor)
                                },
                                onAddGoal = { goal ->
                                    viewModel.addGoal(goal)
                                }
                            )
                        }
                        SubScreen.UTANG -> {
                            UtangScreen(
                                activeDebts = activeDebts,
                                settledDebts = settledDebts,
                                currencyCode = currencyCode,
                                hideBalances = hideBalances,
                                lang = appLanguage,
                                onAddDebt = { debt ->
                                    viewModel.addDebt(debt)
                                },
                                onRecordPayment = { debtId, minor, note ->
                                    viewModel.recordDebtPayment(debtId, minor, note)
                                }
                            )
                        }
                        SubScreen.INSIGHTS -> {
                            InsightsScreen(
                                state = insightsState,
                                currencyCode = currencyCode,
                                onOpenAdd = {
                                    editingTransaction = null
                                    isAddSheetOpen = true
                                }
                            )
                        }
                        null -> {}
                    }
                }
            } else {
                when (currentTab) {
                    ScreenTab.OVERVIEW -> {
                        OverviewScreen(
                            state = overviewState,
                            safeToSpendState = safeToSpendState,
                            isDarkMode = isDarkMode,
                            onToggleTheme = {
                                val next = if (isDarkMode) "light" else "dark"
                                viewModel.setThemeMode(next)
                            },
                            onPrevMonth = { viewModel.goToPreviousCycle() },
                            onNextMonth = { viewModel.goToNextCycle() },
                            onResetToCurrentMonth = { viewModel.goToCurrentCycle() },
                            onOpenAdd = {
                                editingTransaction = null
                                isAddSheetOpen = true
                            },
                            onViewAllTransactions = { currentTab = ScreenTab.TRANSACTIONS },
                            onTransactionClick = { tx ->
                                editingTransaction = tx
                                isAddSheetOpen = true
                            },
                            onViewBudgets = { currentTab = ScreenTab.BUDGETS },
                            onViewInsights = { currentSubScreen = SubScreen.INSIGHTS },
                            onViewAccounts = { currentTab = ScreenTab.ACCOUNTS },
                            onViewBills = { currentSubScreen = SubScreen.BILLS },
                            onViewGoals = { currentSubScreen = SubScreen.GOALS },
                            onViewUtang = { currentSubScreen = SubScreen.UTANG },
                            showCooper = showCooper,
                            hideBalances = hideBalances,
                            onToggleHideBalances = { viewModel.toggleHideBalances(!hideBalances) },
                            lang = appLanguage
                        )
                    }

                    ScreenTab.TRANSACTIONS -> {
                        TransactionsScreen(
                            transactions = filteredTransactions,
                            categories = allCategories,
                            accounts = allAccounts,
                            savedFilters = savedFilters,
                            currencyCode = currencyCode,
                            hideBalances = hideBalances,
                            lang = appLanguage,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.searchQuery.value = it },
                            selectedCategoryId = filterCategoryId,
                            onCategoryFilterChange = { viewModel.filterCategoryId.value = it },
                            selectedAccountId = filterAccountId,
                            onAccountFilterChange = { viewModel.filterAccountId.value = it },
                            selectedKind = filterKind,
                            onKindFilterChange = { viewModel.filterKind.value = it },
                            onApplySavedFilter = { sf -> viewModel.applySavedFilter(sf) },
                            onClearFilters = { viewModel.clearFilters() },
                            onTransactionClick = { tx ->
                                editingTransaction = tx
                                isAddSheetOpen = true
                            },
                            onDeleteTransaction = { tx ->
                                viewModel.deleteTransaction(tx)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Deleted. Undo",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDeleteTransaction()
                                    }
                                }
                            },
                            onOpenAdd = {
                                editingTransaction = null
                                isAddSheetOpen = true
                            },
                            showCooper = showCooper
                        )
                    }

                    ScreenTab.ACCOUNTS -> {
                        AccountsScreen(
                            accounts = allAccounts,
                            currencyCode = currencyCode,
                            hideBalances = hideBalances,
                            lang = appLanguage,
                            onTransfer = { fromId, toId, minor, note ->
                                viewModel.executeTransfer(fromId, toId, minor, note)
                            },
                            onReconcile = { accountId, minor, note ->
                                viewModel.reconcileAccount(accountId, minor, note)
                            },
                            onAddAccount = { name, type, minor ->
                                viewModel.addAccount(name, type, minor)
                            }
                        )
                    }

                    ScreenTab.BUDGETS -> {
                        BudgetsScreen(
                            state = overviewState,
                            onSetBudget = { catId, amountStr ->
                                viewModel.setCategoryBudget(catId, amountStr)
                            },
                            onCopyLastMonth = {
                                viewModel.copyPreviousCycleBudgets()
                            },
                            showCooper = showCooper
                        )
                    }

                    ScreenTab.SETTINGS -> {
                        SettingsScreen(
                            currentCurrency = currencyCode,
                            onCurrencyChange = { viewModel.setCurrency(it) },
                            themeMode = themeMode,
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            payCycleType = payCycleType,
                            payCycleStartDay = payCycleStartDay,
                            onPayCycleChange = { type, day ->
                                viewModel.setPayCycle(type, day)
                            },
                            appLanguage = appLanguage,
                            onAppLanguageChange = { viewModel.setAppLanguage(it) },
                            hideBalances = hideBalances,
                            onToggleHideBalances = { viewModel.toggleHideBalances(!hideBalances) },
                            categories = allCategories,
                            accounts = allAccounts,
                            allTransactions = filteredTransactions,
                            onOpenManageCategories = { isManageCategoriesOpen = true },
                            onOpenBills = { currentSubScreen = SubScreen.BILLS },
                            onOpenGoals = { currentSubScreen = SubScreen.GOALS },
                            onOpenUtang = { currentSubScreen = SubScreen.UTANG },
                            onImportTransactions = { list ->
                                viewModel.importTransactions(list)
                            },
                            onSeedSampleData = {
                                viewModel.seedSampleData()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Sample ledger populated.")
                                }
                            },
                            onClearAllData = {
                                viewModel.clearAllData()
                                scope.launch {
                                    snackbarHostState.showSnackbar("All data cleared.")
                                }
                            },
                            showCooper = showCooper,
                            onToggleShowCooper = { viewModel.toggleShowCooper(it) },
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Transaction Sheet
    if (isAddSheetOpen) {
        AddTransactionSheet(
            isOpen = true,
            onDismiss = {
                isAddSheetOpen = false
                editingTransaction = null
            },
            categories = allCategories,
            accounts = allAccounts,
            favorites = topFavorites,
            currencyCode = currencyCode,
            onSave = { amountStr, categoryId, accountId, kind, note, timestamp ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(
                        id = editingTransaction!!.id,
                        amountStr = amountStr,
                        categoryId = categoryId,
                        accountId = accountId,
                        kind = kind,
                        note = note,
                        occurredAt = timestamp
                    )
                } else {
                    viewModel.addTransaction(
                        amountStr = amountStr,
                        categoryId = categoryId,
                        accountId = accountId,
                        kind = kind,
                        note = note,
                        occurredAt = timestamp
                    )
                }
            },
            onSaveFavorite = { fav ->
                viewModel.addFavorite(fav)
            },
            showCooper = showCooper,
            lang = appLanguage,
            editingTransaction = editingTransaction,
            onDelete = { tx ->
                viewModel.deleteTransaction(tx)
            }
        )
    }

    // Category Manager Dialog
    ManageCategoriesDialog(
        isOpen = isManageCategoriesOpen,
        onDismiss = { isManageCategoriesOpen = false },
        categories = allCategories,
        onAddCategory = { name, kind, colorHex, iconName ->
            // Insert category
        },
        onDeleteCategory = { catId ->
            // Delete category
        }
    )
}
