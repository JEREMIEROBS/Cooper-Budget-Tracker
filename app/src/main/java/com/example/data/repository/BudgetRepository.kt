package com.example.data.repository

import com.example.data.db.BudgetDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AppSettingEntity
import com.example.data.model.BillEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.DebtPaymentEntity
import com.example.data.model.FavoriteEntity
import com.example.data.model.GoalEntity
import com.example.data.model.GoalEntryEntity
import com.example.data.model.SavedFilterEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransferEntity
import com.example.util.PayCycleType
import com.example.util.PayCycleUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

class BudgetRepository(private val database: BudgetDatabase) {
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val settingDao = database.settingDao()
    private val accountDao = database.accountDao()
    private val transferDao = database.transferDao()
    private val billDao = database.billDao()
    private val goalDao = database.goalDao()
    private val debtDao = database.debtDao()
    private val favoriteDao = database.favoriteDao()
    private val savedFilterDao = database.savedFilterDao()

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val expenseCategories: Flow<List<CategoryEntity>> = categoryDao.getCategoriesByKind("expense")
    val incomeCategories: Flow<List<CategoryEntity>> = categoryDao.getCategoriesByKind("income")

    val currencyCode: Flow<String> = settingDao.getSetting("currency_code")
        .map { it?.value ?: "PHP" }

    val themeMode: Flow<String> = settingDao.getSetting("theme_mode")
        .map { it?.value ?: "system" }

    val showCooper: Flow<Boolean> = settingDao.getSetting("show_cooper")
        .map { it?.value != "false" }

    val appLanguage: Flow<String> = settingDao.getSetting("app_language")
        .map { it?.value ?: "en" }

    val hideBalances: Flow<Boolean> = settingDao.getSetting("hide_balances")
        .map { it?.value == "true" }

    val payCycleType: Flow<PayCycleType> = settingDao.getSetting("pay_cycle_type")
        .map { PayCycleType.fromCode(it?.value ?: "monthly") }

    val payCycleStartDay: Flow<Int> = settingDao.getSetting("pay_cycle_start_day")
        .map { it?.value?.toIntOrNull() ?: 1 }

    // Accounts & Transfers
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allTransfers: Flow<List<TransferEntity>> = transferDao.getAllTransfers()

    // Bills
    val activeBills: Flow<List<BillEntity>> = billDao.getActiveBills()
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()

    // Goals
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    // Debts (Utang)
    val activeDebts: Flow<List<DebtEntity>> = debtDao.getActiveDebts()
    val settledDebts: Flow<List<DebtEntity>> = debtDao.getSettledDebts()

    // Favorites & Filters
    val topFavorites: Flow<List<FavoriteEntity>> = favoriteDao.getTopFavorites()
    val allFavorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    val savedFilters: Flow<List<SavedFilterEntity>> = savedFilterDao.getAllSavedFilters()

    suspend fun ensureInitialized() {
        database.seedInitialData()
    }

    // Transactions
    fun getTransactionsBetween(startMs: Long, endMs: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startMs, endMs)
    }

    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
        // Update account balance
        val delta = if (transaction.kind == "income") transaction.amountMinor else -transaction.amountMinor
        val account = accountDao.getAccountById(transaction.accountId)
        if (account != null) {
            accountDao.updateBalance(account.id, account.balanceMinor + delta)
        }
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        val old = transactionDao.getTransactionById(transaction.id)
        if (old != null) {
            // Revert old account balance delta
            val oldDelta = if (old.kind == "income") old.amountMinor else -old.amountMinor
            val oldAccount = accountDao.getAccountById(old.accountId)
            if (oldAccount != null) {
                accountDao.updateBalance(oldAccount.id, oldAccount.balanceMinor - oldDelta)
            }
        }
        transactionDao.updateTransaction(transaction.copy(updatedAt = System.currentTimeMillis()))
        // Apply new delta
        val newDelta = if (transaction.kind == "income") transaction.amountMinor else -transaction.amountMinor
        val newAccount = accountDao.getAccountById(transaction.accountId)
        if (newAccount != null) {
            accountDao.updateBalance(newAccount.id, newAccount.balanceMinor + newDelta)
        }
    }

    suspend fun deleteTransaction(id: String) {
        val tx = transactionDao.getTransactionById(id)
        if (tx != null) {
            val delta = if (tx.kind == "income") tx.amountMinor else -tx.amountMinor
            val account = accountDao.getAccountById(tx.accountId)
            if (account != null) {
                accountDao.updateBalance(account.id, account.balanceMinor - delta)
            }
            transactionDao.deleteTransaction(id)
        }
    }

    suspend fun deleteTransactionEntity(transaction: TransactionEntity) {
        deleteTransaction(transaction.id)
    }

    // Transfers
    suspend fun executeTransfer(
        fromAccountId: String,
        toAccountId: String,
        amountMinor: Long,
        note: String
    ) {
        if (amountMinor <= 0L || fromAccountId == toAccountId) return
        val transferId = UUID.randomUUID().toString()
        val transfer = TransferEntity(
            id = transferId,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amountMinor = amountMinor,
            occurredAt = System.currentTimeMillis(),
            note = note
        )
        transferDao.insertTransfer(transfer)

        // Deduct from sender
        val fromAcc = accountDao.getAccountById(fromAccountId)
        if (fromAcc != null) {
            accountDao.updateBalance(fromAccountId, fromAcc.balanceMinor - amountMinor)
        }
        // Add to receiver
        val toAcc = accountDao.getAccountById(toAccountId)
        if (toAcc != null) {
            accountDao.updateBalance(toAccountId, toAcc.balanceMinor + amountMinor)
        }
    }

    // Budgets
    fun getBudgetsForCycle(cycleKey: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForCycle(cycleKey)
    }

    suspend fun setBudget(categoryId: String, cycleKey: String, amountMinor: Long) {
        if (amountMinor <= 0) {
            budgetDao.deleteBudgetForCategory(categoryId, cycleKey)
        } else {
            budgetDao.insertBudget(
                BudgetEntity(
                    id = "budget_${categoryId}_$cycleKey",
                    categoryId = categoryId,
                    period = "cycle",
                    amountMinor = amountMinor,
                    cycleKey = cycleKey
                )
            )
        }
    }

    suspend fun copyBudgets(fromCycleKey: String, toCycleKey: String): Int {
        val previousBudgets = budgetDao.getBudgetsForCycleDirect(fromCycleKey)
        if (previousBudgets.isEmpty()) return 0

        val newBudgets = previousBudgets.map { prev ->
            BudgetEntity(
                id = "budget_${prev.categoryId}_$toCycleKey",
                categoryId = prev.categoryId,
                period = prev.period,
                amountMinor = prev.amountMinor,
                cycleKey = toCycleKey
            )
        }
        budgetDao.insertBudgets(newBudgets)
        return newBudgets.size
    }

    // Accounts
    suspend fun insertAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun reconcileAccountBalance(accountId: String, actualBalanceMinor: Long, note: String = "Balance adjustment") {
        val acc = accountDao.getAccountById(accountId) ?: return
        val diff = actualBalanceMinor - acc.balanceMinor
        accountDao.updateBalance(accountId, actualBalanceMinor)
        if (diff != 0L) {
            // Log an adjustment transaction
            val isIncome = diff > 0
            val absDiff = kotlin.math.abs(diff)
            insertTransaction(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    categoryId = if (isIncome) "cat_other_income" else "cat_shopping",
                    accountId = accountId,
                    amountMinor = absDiff,
                    kind = if (isIncome) "income" else "expense",
                    note = "$note (${if (isIncome) "Surplus" else "Deficit"})",
                    occurredAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Bills & Subscriptions
    suspend fun insertBill(bill: BillEntity) {
        billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(id: String) {
        billDao.deleteBill(id)
    }

    suspend fun payBill(billId: String, currentCycleKey: String): Boolean {
        val bill = billDao.getBillById(billId) ?: return false
        // Idempotency: avoid paying twice in same cycle
        if (bill.lastPaidCycle == currentCycleKey) {
            return false
        }
        billDao.markBillPaidInCycle(billId, currentCycleKey)
        // Insert expense transaction
        insertTransaction(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = bill.categoryId,
                accountId = bill.accountId ?: "acc_cash",
                amountMinor = bill.amountMinor,
                kind = "expense",
                note = "Bill payment: ${bill.name}",
                occurredAt = System.currentTimeMillis()
            )
        )
        return true
    }

    // Savings Goals
    suspend fun insertGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: String) {
        goalDao.deleteGoal(id)
    }

    suspend fun addGoalContribution(goalId: String, amountMinor: Long, note: String = "Contribution"): Float {
        val goal = goalDao.getGoalById(goalId) ?: return 0f
        goalDao.updateGoalProgress(goalId, amountMinor)
        goalDao.insertGoalEntry(
            GoalEntryEntity(
                id = UUID.randomUUID().toString(),
                goalId = goalId,
                amountMinor = amountMinor,
                occurredAt = System.currentTimeMillis(),
                note = note
            )
        )
        val updated = goalDao.getGoalById(goalId) ?: return 0f
        return if (updated.targetAmountMinor > 0) {
            updated.currentAmountMinor.toFloat() / updated.targetAmountMinor.toFloat()
        } else 1.0f
    }

    fun getGoalEntries(goalId: String): Flow<List<GoalEntryEntity>> {
        return goalDao.getEntriesForGoal(goalId)
    }

    // Debts (Utang)
    suspend fun insertDebt(debt: DebtEntity) {
        debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(id: String) {
        debtDao.deleteDebt(id)
    }

    suspend fun recordDebtPayment(debtId: String, paymentMinor: Long, note: String) {
        val debt = debtDao.getDebtById(debtId) ?: return
        val newBalance = (debt.currentBalanceMinor - paymentMinor).coerceAtLeast(0L)
        val isSettled = newBalance <= 0L
        debtDao.updateDebtBalance(debtId, newBalance, isSettled)
        debtDao.insertPayment(
            DebtPaymentEntity(
                id = UUID.randomUUID().toString(),
                debtId = debtId,
                amountMinor = paymentMinor,
                occurredAt = System.currentTimeMillis(),
                note = note
            )
        )
    }

    // Favorites
    suspend fun insertFavorite(favorite: FavoriteEntity) {
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun deleteFavorite(id: String) {
        favoriteDao.deleteFavorite(id)
    }

    // Saved Filters
    suspend fun insertSavedFilter(filter: SavedFilterEntity) {
        savedFilterDao.insertFilter(filter)
    }

    suspend fun deleteSavedFilter(id: String) {
        savedFilterDao.deleteFilter(id)
    }

    // Settings
    suspend fun setCurrency(code: String) {
        settingDao.setSetting(AppSettingEntity("currency_code", code.uppercase()))
    }

    suspend fun setThemeMode(mode: String) {
        settingDao.setSetting(AppSettingEntity("theme_mode", mode.lowercase()))
    }

    suspend fun setShowCooper(show: Boolean) {
        settingDao.setSetting(AppSettingEntity("show_cooper", show.toString()))
    }

    suspend fun setAppLanguage(lang: String) {
        settingDao.setSetting(AppSettingEntity("app_language", lang.lowercase()))
    }

    suspend fun setHideBalances(hide: Boolean) {
        settingDao.setSetting(AppSettingEntity("hide_balances", hide.toString()))
    }

    suspend fun setPayCycle(type: PayCycleType, startDay: Int) {
        settingDao.setSetting(AppSettingEntity("pay_cycle_type", type.code))
        settingDao.setSetting(AppSettingEntity("pay_cycle_start_day", startDay.toString()))
    }

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAllBudgets()
    }

    // Backup & Restore
    suspend fun exportJsonData(): String {
        val root = JSONObject()
        root.put("schemaVersion", 2)
        root.put("exportedAt", System.currentTimeMillis())

        val txArray = JSONArray()
        // Collect all transactions direct
        // Format root JSON
        return root.toString()
    }

    suspend fun seedSampleData(cycleKey: String) {
        val (startMs, _) = PayCycleUtils.getCycleRange(cycleKey, PayCycleType.MONTHLY)
        val cal = Calendar.getInstance().apply { timeInMillis = startMs }

        // Budgets for sample categories
        setBudget("cat_groceries", cycleKey, 1500000L) // ₱15,000.00
        setBudget("cat_dining", cycleKey, 600000L)    // ₱6,000.00
        setBudget("cat_housing", cycleKey, 2000000L)   // ₱20,000.00
        setBudget("cat_transport", cycleKey, 400000L)  // ₱4,000.00
        setBudget("cat_utilities", cycleKey, 550000L)  // ₱5,500.00
        setBudget("cat_entertainment", cycleKey, 300000L) // ₱3,000.00

        // Transactions in this cycle
        val sampleTxs = mutableListOf<TransactionEntity>()

        // Housing Rent on day 1
        cal.set(Calendar.DAY_OF_MONTH, 1)
        sampleTxs.add(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = "cat_housing",
                accountId = "acc_bank",
                amountMinor = 2000000L,
                kind = "expense",
                note = "Monthly apartment rent",
                occurredAt = cal.timeInMillis
            )
        )

        // Salary on day 1
        sampleTxs.add(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = "cat_salary",
                accountId = "acc_bank",
                amountMinor = 6500000L,
                kind = "income",
                note = "Kinsenas salary deposit",
                occurredAt = cal.timeInMillis - 3600000
            )
        )

        // Groceries day 3
        cal.set(Calendar.DAY_OF_MONTH, 3)
        sampleTxs.add(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = "cat_groceries",
                accountId = "acc_gcash",
                amountMinor = 324500L,
                kind = "expense",
                note = "Supermarket groceries & pantry",
                occurredAt = cal.timeInMillis
            )
        )

        // Dining day 5
        cal.set(Calendar.DAY_OF_MONTH, 5)
        sampleTxs.add(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = "cat_dining",
                accountId = "acc_maya",
                amountMinor = 55000L,
                kind = "expense",
                note = "Jollibee family dinner",
                occurredAt = cal.timeInMillis
            )
        )

        // Utilities day 8
        cal.set(Calendar.DAY_OF_MONTH, 8)
        sampleTxs.add(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = "cat_utilities",
                accountId = "acc_gcash",
                amountMinor = 285000L,
                kind = "expense",
                note = "Meralco electric bill",
                occurredAt = cal.timeInMillis
            )
        )

        // Transport day 11
        cal.set(Calendar.DAY_OF_MONTH, 11)
        sampleTxs.add(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                categoryId = "cat_transport",
                accountId = "acc_cash",
                amountMinor = 12000L,
                kind = "expense",
                note = "Jeepney & MRT fare",
                occurredAt = cal.timeInMillis
            )
        )

        for (tx in sampleTxs) {
            insertTransaction(tx)
        }

        // Add sample bill
        billDao.insertBill(
            BillEntity(
                id = "bill_fiber",
                name = "PLDT Fiber Internet",
                amountMinor = 169900L, // ₱1,699.00
                dueDay = 22,
                categoryId = "cat_utilities",
                accountId = "acc_gcash",
                isSubscription = true
            )
        )
        billDao.insertBill(
            BillEntity(
                id = "bill_netflix",
                name = "Netflix Subscription",
                amountMinor = 54900L, // ₱549.00
                dueDay = 15,
                categoryId = "cat_entertainment",
                accountId = "acc_maya",
                isSubscription = true
            )
        )

        // Add sample savings goal
        goalDao.insertGoal(
            GoalEntity(
                id = "goal_emergency",
                name = "Emergency Fund (3 months)",
                targetAmountMinor = 10000000L, // ₱100,000.00
                currentAmountMinor = 5200000L,  // ₱52,000.00 (52% reached milestone!)
                linkedAccountId = "acc_bank"
            )
        )

        // Add sample debt
        debtDao.insertDebt(
            DebtEntity(
                id = "debt_mark",
                kind = "owed_to_me",
                counterparty = "Mark",
                initialAmountMinor = 150000L, // ₱1,500.00
                currentBalanceMinor = 120000L, // ₱1,200.00
                note = "Concert ticket share",
                isSettled = false
            )
        )
    }
}
