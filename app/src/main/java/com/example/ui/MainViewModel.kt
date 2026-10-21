package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountEntity
import com.example.data.model.BillEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.FavoriteEntity
import com.example.data.model.GoalEntity
import com.example.data.model.SavedFilterEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.BudgetRepository
import com.example.ui.components.SafeToSpendState
import com.example.util.MoneyUtils
import com.example.util.PayCycleInfo
import com.example.util.PayCycleType
import com.example.util.PayCycleUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CategorySpendProgress(
    val category: CategoryEntity,
    val spentMinor: Long,
    val budgetMinor: Long,
    val ratio: Float,
    val remainingMinor: Long
)

data class MonthOverviewState(
    val cycleKey: String = "",
    val cycleLabel: String = "",
    val currencyCode: String = "PHP",
    val totalSpentMinor: Long = 0L,
    val totalIncomeMinor: Long = 0L,
    val totalBudgetedMinor: Long = 0L,
    val netBalanceMinor: Long = 0L,
    val remainingBudgetMinor: Long = 0L,
    val daysRemaining: Int = 1,
    val remainingPerDayMinor: Long = 0L,
    val topCategories: List<CategorySpendProgress> = emptyList(),
    val allCategoryProgress: List<CategorySpendProgress> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val categoriesMap: Map<String, CategoryEntity> = emptyMap(),
    val adviceSentence: String = ""
)

data class InsightsState(
    val currentCycleKey: String = "",
    val previousCycleKey: String = "",
    val currentSpentMinor: Long = 0L,
    val previousSpentMinor: Long = 0L,
    val spendingDeltaMinor: Long = 0L,
    val topSpendingCategory: CategorySpendProgress? = null,
    val categoryBreakdown: List<CategorySpendProgress> = emptyList(),
    val summarySentence: String = ""
)

class MainViewModel(private val repository: BudgetRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    val payCycleType = repository.payCycleType.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PayCycleType.MONTHLY
    )

    val payCycleStartDay = repository.payCycleStartDay.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1
    )

    val selectedCycleKey = MutableStateFlow<String?>(null)

    val allCategories = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currencyCode = repository.currencyCode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "PHP"
    )

    val themeMode = repository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )

    val showCooper = repository.showCooper.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val appLanguage = repository.appLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "en"
    )

    val hideBalances = repository.hideBalances.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val allAccounts = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeBills = repository.activeBills.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allGoals = repository.allGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeDebts = repository.activeDebts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settledDebts = repository.settledDebts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val topFavorites = repository.topFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFavorites = repository.allFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedFilters = repository.savedFilters.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Transaction search and filter state
    val searchQuery = MutableStateFlow("")
    val filterCategoryId = MutableStateFlow<String?>(null)
    val filterAccountId = MutableStateFlow<String?>(null)
    val filterKind = MutableStateFlow("all") // "all" | "expense" | "income"

    val lastDeletedTransaction = MutableStateFlow<TransactionEntity?>(null)

    // Active cycle key resolution
    val resolvedCycleKey = combine(selectedCycleKey, payCycleType, payCycleStartDay) { manualKey, cycleType, startDay ->
        manualKey ?: PayCycleUtils.getCurrentCycleKey(cycleType, startDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PayCycleUtils.getCurrentCycleKey(PayCycleType.MONTHLY, 1)
    )

    val payCycleInfo = combine(resolvedCycleKey, payCycleType, payCycleStartDay) { cycleKey, type, startDay ->
        PayCycleUtils.getCycleInfo(cycleKey, type, startDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PayCycleUtils.getCycleInfo(PayCycleUtils.getCurrentCycleKey(PayCycleType.MONTHLY, 1), PayCycleType.MONTHLY, 1)
    )

    // Current cycle transactions flow
    private val currentCycleTransactions = payCycleInfo.flatMapLatest { info ->
        repository.getTransactionsBetween(info.startMs, info.endMs)
    }

    // Previous cycle transactions flow for insights
    private val previousCycleTransactions = combine(payCycleInfo, payCycleType, payCycleStartDay) { info, type, startDay ->
        val prevKey = PayCycleUtils.getAdjacentCycleKey(info.cycleKey, -1, type, startDay)
        val prevInfo = PayCycleUtils.getCycleInfo(prevKey, type, startDay)
        prevInfo
    }.flatMapLatest { prevInfo ->
        repository.getTransactionsBetween(prevInfo.startMs, prevInfo.endMs)
    }

    // Current cycle budgets flow
    private val currentCycleBudgets = resolvedCycleKey.flatMapLatest { cycleKey ->
        repository.getBudgetsForCycle(cycleKey)
    }

    // Overview state combine
    val overviewState: StateFlow<MonthOverviewState> = combine(
        payCycleInfo,
        currentCycleTransactions,
        currentCycleBudgets,
        allCategories,
        currencyCode
    ) { cycleInfo, transactions, budgets, categories, currency ->
        val catMap = categories.associateBy { it.id }

        var spentTotal = 0L
        var incomeTotal = 0L
        val spentPerCategory = mutableMapOf<String, Long>()

        for (tx in transactions) {
            if (tx.kind == "expense") {
                spentTotal += tx.amountMinor
                spentPerCategory[tx.categoryId] = (spentPerCategory[tx.categoryId] ?: 0L) + tx.amountMinor
            } else if (tx.kind == "income") {
                incomeTotal += tx.amountMinor
            }
        }

        val budgetMap = budgets.associateBy { it.categoryId }
        var budgetedTotal = 0L
        for (b in budgets) {
            budgetedTotal += b.amountMinor
        }

        val categoryProgressList = categories
            .filter { it.kind == "expense" }
            .map { cat ->
                val spent = spentPerCategory[cat.id] ?: 0L
                val budget = budgetMap[cat.id]?.amountMinor ?: 0L
                val ratio = if (budget > 0) spent.toFloat() / budget.toFloat() else if (spent > 0) 1.0f else 0.0f
                val remaining = budget - spent
                CategorySpendProgress(
                    category = cat,
                    spentMinor = spent,
                    budgetMinor = budget,
                    ratio = ratio,
                    remainingMinor = remaining
                )
            }
            .sortedByDescending { it.spentMinor }

        val top3 = categoryProgressList.filter { it.spentMinor > 0 || it.budgetMinor > 0 }.take(3)
        val daysRemaining = cycleInfo.daysRemaining
        val remainingBudget = budgetedTotal - spentTotal
        val remainingPerDay = if (remainingBudget > 0 && daysRemaining > 0) {
            remainingBudget / daysRemaining
        } else {
            0L
        }

        val advice = when {
            budgetedTotal == 0L && spentTotal == 0L -> {
                "Set category budgets to track your pace this cycle."
            }
            budgetedTotal == 0L -> {
                "${MoneyUtils.formatMoney(spentTotal, currency)} spent so far this cycle."
            }
            spentTotal > budgetedTotal -> {
                val over = spentTotal - budgetedTotal
                "${MoneyUtils.formatMoney(over, currency)} over total budget. It happens."
            }
            daysRemaining <= 1 -> {
                "Final day of the cycle: ${MoneyUtils.formatMoney(remainingBudget, currency)} unspent."
            }
            else -> {
                "${MoneyUtils.formatMoney(remainingPerDay, currency)} / day left for the remaining $daysRemaining days."
            }
        }

        MonthOverviewState(
            cycleKey = cycleInfo.cycleKey,
            cycleLabel = cycleInfo.displayLabel,
            currencyCode = currency,
            totalSpentMinor = spentTotal,
            totalIncomeMinor = incomeTotal,
            totalBudgetedMinor = budgetedTotal,
            netBalanceMinor = incomeTotal - spentTotal,
            remainingBudgetMinor = remainingBudget,
            daysRemaining = daysRemaining,
            remainingPerDayMinor = remainingPerDay,
            topCategories = top3,
            allCategoryProgress = categoryProgressList,
            recentTransactions = transactions.take(5),
            categoriesMap = catMap,
            adviceSentence = advice
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthOverviewState()
    )

    // Safe-to-Spend Today State
    // Formula: (budget remaining - upcoming bills in this cycle) / days left in cycle
    val safeToSpendState: StateFlow<SafeToSpendState> = combine(
        overviewState,
        activeBills,
        currencyCode
    ) { overview, bills, currency ->
        val currentKey = overview.cycleKey
        val upcomingUnpaidBills = bills
            .filter { it.lastPaidCycle != currentKey }
            .sumOf { it.amountMinor }

        val netAvailable = overview.remainingBudgetMinor - upcomingUnpaidBills
        val days = overview.daysRemaining.coerceAtLeast(1)

        val isOver = overview.totalBudgetedMinor > 0L && netAvailable < 0L
        val overAmount = if (isOver) kotlin.math.abs(netAvailable) else 0L
        val safeToday = if (isOver || netAvailable <= 0L) 0L else (netAvailable / days)

        SafeToSpendState(
            safeTodayMinor = safeToday,
            isOverBudget = isOver,
            overAmountMinor = overAmount,
            remainingBudgetMinor = overview.remainingBudgetMinor,
            upcomingBillsMinor = upcomingUnpaidBills,
            daysRemaining = days,
            cycleLabel = overview.cycleLabel,
            currencyCode = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SafeToSpendState(0L, false, 0L, 0L, 0L, 1, "", "PHP")
    )

    // Insights state combine
    val insightsState: StateFlow<InsightsState> = combine(
        payCycleInfo,
        currentCycleTransactions,
        previousCycleTransactions,
        allCategories,
        currencyCode
    ) { cycleInfo, currentTxs, prevTxs, categories, currency ->
        val catMap = categories.associateBy { it.id }

        val currentSpentMap = mutableMapOf<String, Long>()
        var currentTotalSpent = 0L
        for (tx in currentTxs) {
            if (tx.kind == "expense") {
                currentTotalSpent += tx.amountMinor
                currentSpentMap[tx.categoryId] = (currentSpentMap[tx.categoryId] ?: 0L) + tx.amountMinor
            }
        }

        var prevTotalSpent = 0L
        for (tx in prevTxs) {
            if (tx.kind == "expense") {
                prevTotalSpent += tx.amountMinor
            }
        }

        val delta = currentTotalSpent - prevTotalSpent

        val breakdown = categories
            .filter { it.kind == "expense" }
            .map { cat ->
                val spent = currentSpentMap[cat.id] ?: 0L
                val ratio = if (currentTotalSpent > 0) spent.toFloat() / currentTotalSpent.toFloat() else 0f
                CategorySpendProgress(
                    category = cat,
                    spentMinor = spent,
                    budgetMinor = 0L,
                    ratio = ratio,
                    remainingMinor = 0L
                )
            }
            .filter { it.spentMinor > 0 }
            .sortedByDescending { it.spentMinor }

        val topCategory = breakdown.firstOrNull()

        val summary = when {
            currentTotalSpent == 0L -> "No expenses recorded for this cycle."
            topCategory != null && prevTotalSpent > 0L -> {
                val pct = (topCategory.ratio * 100).toInt()
                val deltaStr = if (delta > 0) {
                    "${MoneyUtils.formatMoney(delta, currency)} more than previous cycle"
                } else if (delta < 0) {
                    "${MoneyUtils.formatMoney(-delta, currency)} less than previous cycle"
                } else {
                    "identical to previous cycle"
                }
                "${topCategory.category.name} is your highest expense ($pct% of spending). Overall, you've spent $deltaStr."
            }
            topCategory != null -> {
                val pct = (topCategory.ratio * 100).toInt()
                "${topCategory.category.name} accounts for $pct% of your total spending this cycle."
            }
            else -> "Clean slate for this cycle."
        }

        InsightsState(
            currentCycleKey = cycleInfo.cycleKey,
            previousCycleKey = "",
            currentSpentMinor = currentTotalSpent,
            previousSpentMinor = prevTotalSpent,
            spendingDeltaMinor = delta,
            topSpendingCategory = topCategory,
            categoryBreakdown = breakdown,
            summarySentence = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsState()
    )

    // Filter state container for type-safe combine
    private data class FilterParams(
        val query: String,
        val catId: String?,
        val accId: String?,
        val kind: String
    )

    private val filterParams = combine(searchQuery, filterCategoryId, filterAccountId, filterKind) { q, cat, acc, k ->
        FilterParams(q, cat, acc, k)
    }

    // Filtered transactions for Transactions Tab
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        currentCycleTransactions,
        filterParams,
        allCategories
    ) { txs, filters, cats ->
        val catMap = cats.associateBy { it.id }
        val q = filters.query.trim().lowercase()

        txs.filter { tx ->
            val matchesKind = filters.kind == "all" || tx.kind == filters.kind
            val matchesCategory = filters.catId == null || tx.categoryId == filters.catId
            val matchesAccount = filters.accId == null || tx.accountId == filters.accId
            val matchesSearch = if (q.isBlank()) {
                true
            } else {
                val catName = catMap[tx.categoryId]?.name?.lowercase() ?: ""
                tx.note.lowercase().contains(q) || catName.contains(q)
            }
            matchesKind && matchesCategory && matchesAccount && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Cycle Navigation
    fun goToPreviousCycle() {
        val currentKey = resolvedCycleKey.value
        val type = payCycleType.value
        val startDay = payCycleStartDay.value
        selectedCycleKey.value = PayCycleUtils.getAdjacentCycleKey(currentKey, -1, type, startDay)
    }

    fun goToNextCycle() {
        val currentKey = resolvedCycleKey.value
        val type = payCycleType.value
        val startDay = payCycleStartDay.value
        selectedCycleKey.value = PayCycleUtils.getAdjacentCycleKey(currentKey, 1, type, startDay)
    }

    fun goToCurrentCycle() {
        val type = payCycleType.value
        val startDay = payCycleStartDay.value
        selectedCycleKey.value = PayCycleUtils.getCurrentCycleKey(type, startDay)
    }

    // Transactions CRUD
    fun addTransaction(
        amountStr: String,
        categoryId: String,
        accountId: String = "acc_cash",
        kind: String = "expense",
        note: String = "",
        occurredAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val currency = currencyCode.value
            val amountMinor = MoneyUtils.toMinor(amountStr, currency)
            if (amountMinor <= 0L) return@launch

            val entity = TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                accountId = accountId,
                amountMinor = amountMinor,
                kind = kind,
                note = note.trim(),
                occurredAt = occurredAt
            )
            repository.insertTransaction(entity)
        }
    }

    fun updateTransaction(
        id: String,
        amountStr: String,
        categoryId: String,
        accountId: String,
        kind: String,
        note: String,
        occurredAt: Long
    ) {
        viewModelScope.launch {
            val currency = currencyCode.value
            val amountMinor = MoneyUtils.toMinor(amountStr, currency)
            if (amountMinor <= 0L) return@launch

            val updated = TransactionEntity(
                id = id,
                categoryId = categoryId,
                accountId = accountId,
                amountMinor = amountMinor,
                kind = kind,
                note = note.trim(),
                occurredAt = occurredAt,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTransaction(updated)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            lastDeletedTransaction.value = transaction
            repository.deleteTransaction(transaction.id)
        }
    }

    fun undoDeleteTransaction() {
        viewModelScope.launch {
            val deleted = lastDeletedTransaction.value ?: return@launch
            repository.insertTransaction(deleted)
            lastDeletedTransaction.value = null
        }
    }

    // Budget CRUD
    fun setCategoryBudget(categoryId: String, amountStr: String) {
        viewModelScope.launch {
            val currency = currencyCode.value
            val amountMinor = MoneyUtils.toMinor(amountStr, currency)
            val currentKey = resolvedCycleKey.value
            repository.setBudget(categoryId, currentKey, amountMinor)
        }
    }

    fun copyPreviousCycleBudgets() {
        viewModelScope.launch {
            val currentKey = resolvedCycleKey.value
            val type = payCycleType.value
            val startDay = payCycleStartDay.value
            val prevKey = PayCycleUtils.getAdjacentCycleKey(currentKey, -1, type, startDay)
            repository.copyBudgets(prevKey, currentKey)
        }
    }

    // Transfers
    fun executeTransfer(fromAccountId: String, toAccountId: String, amountMinor: Long, note: String) {
        viewModelScope.launch {
            repository.executeTransfer(fromAccountId, toAccountId, amountMinor, note)
        }
    }

    // Reconcile Account
    fun reconcileAccount(accountId: String, actualMinor: Long, note: String) {
        viewModelScope.launch {
            repository.reconcileAccountBalance(accountId, actualMinor, note)
        }
    }

    // Add Account
    fun addAccount(name: String, type: String, initialMinor: Long) {
        viewModelScope.launch {
            repository.insertAccount(
                AccountEntity(
                    id = "acc_${UUID.randomUUID().toString().take(8)}",
                    name = name,
                    type = type,
                    balanceMinor = initialMinor
                )
            )
        }
    }

    // Bills
    fun payBill(billId: String, currentCycleKey: String) {
        viewModelScope.launch {
            repository.payBill(billId, currentCycleKey)
        }
    }

    fun addBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.insertBill(bill)
        }
    }

    // Goals
    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun addGoalFunds(goalId: String, amountMinor: Long) {
        viewModelScope.launch {
            repository.addGoalContribution(goalId, amountMinor)
        }
    }

    // Debts (Utang)
    fun addDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.insertDebt(debt)
        }
    }

    fun recordDebtPayment(debtId: String, amountMinor: Long, note: String) {
        viewModelScope.launch {
            repository.recordDebtPayment(debtId, amountMinor, note)
        }
    }

    // Favorites
    fun addFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.insertFavorite(favorite)
        }
    }

    // Saved Filters
    fun applySavedFilter(filter: SavedFilterEntity) {
        searchQuery.value = filter.searchQuery
        filterCategoryId.value = filter.categoryId
        filterAccountId.value = filter.accountId
        filterKind.value = filter.kind
    }

    fun clearFilters() {
        searchQuery.value = ""
        filterCategoryId.value = null
        filterAccountId.value = null
        filterKind.value = "all"
    }

    // Settings
    fun setCurrency(code: String) {
        viewModelScope.launch { repository.setCurrency(code) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun toggleShowCooper(show: Boolean) {
        viewModelScope.launch { repository.setShowCooper(show) }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch { repository.setAppLanguage(lang) }
    }

    fun toggleHideBalances(hide: Boolean) {
        viewModelScope.launch { repository.setHideBalances(hide) }
    }

    fun setPayCycle(type: PayCycleType, startDay: Int) {
        viewModelScope.launch {
            repository.setPayCycle(type, startDay)
            selectedCycleKey.value = null // Resets to current cycle under new rule
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData(resolvedCycleKey.value)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun importTransactions(transactions: List<TransactionEntity>) {
        viewModelScope.launch {
            for (tx in transactions) {
                repository.insertTransaction(tx)
            }
        }
    }
}

class MainViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
