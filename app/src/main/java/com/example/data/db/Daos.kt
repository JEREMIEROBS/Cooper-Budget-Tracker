package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isArchived = 0 AND kind = :kind ORDER BY sortOrder ASC, name ASC")
    fun getCategoriesByKind(kind: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isTransfer = 0 ORDER BY occurredAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE occurredAt >= :startMs AND occurredAt <= :endMs AND isTransfer = 0 ORDER BY occurredAt DESC")
    fun getTransactionsBetween(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isTransfer = 0 ORDER BY occurredAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND isTransfer = 0 ORDER BY occurredAt DESC")
    fun getTransactionsForAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Delete
    suspend fun deleteTransactionEntity(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE cycleKey = :cycleKey")
    fun getBudgetsForCycle(cycleKey: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE cycleKey = :cycleKey")
    suspend fun getBudgetsForCycleDirect(cycleKey: String): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: String)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId AND cycleKey = :cycleKey")
    suspend fun deleteBudgetForCategory(categoryId: String, cycleKey: String)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<AppSettingEntity?>

    @Query("SELECT * FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingDirect(key: String): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)

    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balanceMinor = :newBalanceMinor WHERE id = :id")
    suspend fun updateBalance(id: String, newBalanceMinor: Long)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY occurredAt DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers WHERE id = :id")
    suspend fun deleteTransfer(id: String)
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE isActive = 1 ORDER BY dueDay ASC")
    fun getActiveBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills ORDER BY dueDay ASC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun getBillById(id: String): BillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<BillEntity>)

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Query("UPDATE bills SET lastPaidCycle = :cycleKey WHERE id = :id")
    suspend fun markBillPaidInCycle(id: String, cycleKey: String)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBill(id: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("UPDATE goals SET currentAmountMinor = currentAmountMinor + :deltaMinor WHERE id = :id")
    suspend fun updateGoalProgress(id: String, deltaMinor: Long)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)

    @Query("SELECT * FROM goal_entries WHERE goalId = :goalId ORDER BY occurredAt DESC")
    fun getEntriesForGoal(goalId: String): Flow<List<GoalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalEntry(entry: GoalEntryEntity)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE isSettled = 0 ORDER BY createdAt DESC")
    fun getActiveDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isSettled = 1 ORDER BY createdAt DESC")
    fun getSettledDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: String): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("UPDATE debts SET currentBalanceMinor = :balanceMinor, isSettled = :isSettled WHERE id = :id")
    suspend fun updateDebtBalance(id: String, balanceMinor: Long, isSettled: Boolean)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebt(id: String)

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY occurredAt DESC")
    fun getPaymentsForDebt(debtId: String): Flow<List<DebtPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: DebtPaymentEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY sortOrder ASC, name ASC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY sortOrder ASC LIMIT 4")
    fun getTopFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)
}

@Dao
interface SavedFilterDao {
    @Query("SELECT * FROM saved_filters ORDER BY name ASC")
    fun getAllSavedFilters(): Flow<List<SavedFilterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilter(filter: SavedFilterEntity)

    @Query("DELETE FROM saved_filters WHERE id = :id")
    suspend fun deleteFilter(id: String)
}
