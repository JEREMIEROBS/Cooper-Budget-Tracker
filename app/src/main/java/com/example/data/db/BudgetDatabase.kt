package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        AppSettingEntity::class,
        AccountEntity::class,
        TransferEntity::class,
        BillEntity::class,
        GoalEntity::class,
        GoalEntryEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        FavoriteEntity::class,
        SavedFilterEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun settingDao(): SettingDao
    abstract fun accountDao(): AccountDao
    abstract fun transferDao(): TransferDao
    abstract fun billDao(): BillDao
    abstract fun goalDao(): GoalDao
    abstract fun debtDao(): DebtDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun savedFilterDao(): SavedFilterDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null

        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget_ledger.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context).seedInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val catCount = categoryDao().getCategoryCount()
        if (catCount == 0) {
            val defaultCategories = listOf(
                CategoryEntity(
                    id = "cat_groceries",
                    name = "Groceries",
                    kind = "expense",
                    colorHex = "#1F5E4B",
                    iconName = "shopping_cart",
                    sortOrder = 1
                ),
                CategoryEntity(
                    id = "cat_dining",
                    name = "Dining & Coffee",
                    kind = "expense",
                    colorHex = "#B4472F",
                    iconName = "restaurant",
                    sortOrder = 2
                ),
                CategoryEntity(
                    id = "cat_housing",
                    name = "Housing & Rent",
                    kind = "expense",
                    colorHex = "#2E5B88",
                    iconName = "home",
                    sortOrder = 3
                ),
                CategoryEntity(
                    id = "cat_transport",
                    name = "Transport & Fuel",
                    kind = "expense",
                    colorHex = "#B8832A",
                    iconName = "directions_car",
                    sortOrder = 4
                ),
                CategoryEntity(
                    id = "cat_utilities",
                    name = "Utilities & Bills",
                    kind = "expense",
                    colorHex = "#6A4C93",
                    iconName = "bolt",
                    sortOrder = 5
                ),
                CategoryEntity(
                    id = "cat_entertainment",
                    name = "Entertainment",
                    kind = "expense",
                    colorHex = "#D05353",
                    iconName = "movie",
                    sortOrder = 6
                ),
                CategoryEntity(
                    id = "cat_health",
                    name = "Health & Personal",
                    kind = "expense",
                    colorHex = "#198754",
                    iconName = "favorite",
                    sortOrder = 7
                ),
                CategoryEntity(
                    id = "cat_shopping",
                    name = "Shopping",
                    kind = "expense",
                    colorHex = "#495057",
                    iconName = "shopping_bag",
                    sortOrder = 8
                ),
                CategoryEntity(
                    id = "cat_salary",
                    name = "Salary & Wages",
                    kind = "income",
                    colorHex = "#2F7D5B",
                    iconName = "payments",
                    sortOrder = 9
                ),
                CategoryEntity(
                    id = "cat_freelance",
                    name = "Freelance & Consulting",
                    kind = "income",
                    colorHex = "#238636",
                    iconName = "work",
                    sortOrder = 10
                ),
                CategoryEntity(
                    id = "cat_investments",
                    name = "Investments",
                    kind = "income",
                    colorHex = "#3B82F6",
                    iconName = "trending_up",
                    sortOrder = 11
                ),
                CategoryEntity(
                    id = "cat_other_income",
                    name = "Other Income",
                    kind = "income",
                    colorHex = "#6B7280",
                    iconName = "account_balance_wallet",
                    sortOrder = 12
                )
            )
            categoryDao().insertCategories(defaultCategories)
        }

        // Seed default Accounts
        val accCount = accountDao().getAccountCount()
        if (accCount == 0) {
            val defaultAccounts = listOf(
                AccountEntity(
                    id = "acc_cash",
                    name = "Cash (Wallet)",
                    type = "cash",
                    balanceMinor = 250000L, // ₱2,500.00
                    colorHex = "#1F5E4B"
                ),
                AccountEntity(
                    id = "acc_gcash",
                    name = "GCash",
                    type = "gcash",
                    balanceMinor = 845000L, // ₱8,450.00
                    colorHex = "#007DFE"
                ),
                AccountEntity(
                    id = "acc_maya",
                    name = "Maya",
                    type = "maya",
                    balanceMinor = 520000L, // ₱5,200.00
                    colorHex = "#00D632"
                ),
                AccountEntity(
                    id = "acc_bank",
                    name = "Main Bank",
                    type = "bank",
                    balanceMinor = 4500000L, // ₱45,000.00
                    colorHex = "#2E5B88"
                )
            )
            accountDao().insertAccounts(defaultAccounts)
        }

        // Seed default favorite templates
        val favs = listOf(
            FavoriteEntity(
                id = "fav_coffee",
                name = "Coffee",
                amountMinor = 14000L, // ₱140.00
                categoryId = "cat_dining",
                accountId = "acc_gcash",
                kind = "expense",
                note = "Iced latte",
                sortOrder = 1
            ),
            FavoriteEntity(
                id = "fav_fare",
                name = "Fare / Jeep / Grab",
                amountMinor = 6500L, // ₱65.00
                categoryId = "cat_transport",
                accountId = "acc_cash",
                kind = "expense",
                note = "Commute fare",
                sortOrder = 2
            ),
            FavoriteEntity(
                id = "fav_lunch",
                name = "Lunch meal",
                amountMinor = 18000L, // ₱180.00
                categoryId = "cat_dining",
                accountId = "acc_gcash",
                kind = "expense",
                note = "Workday lunch",
                sortOrder = 3
            ),
            FavoriteEntity(
                id = "fav_groceries",
                name = "Quick Groceries",
                amountMinor = 50000L, // ₱500.00
                categoryId = "cat_groceries",
                accountId = "acc_maya",
                kind = "expense",
                note = "Essentials & snacks",
                sortOrder = 4
            )
        )
        favoriteDao().insertFavorites(favs)

        // Seed default saved filters
        savedFilterDao().insertFilter(
            SavedFilterEntity(
                id = "filter_food",
                name = "Dining & Coffee",
                categoryId = "cat_dining",
                kind = "expense"
            )
        )
        savedFilterDao().insertFilter(
            SavedFilterEntity(
                id = "filter_gcash",
                name = "GCash Expenses",
                accountId = "acc_gcash",
                kind = "expense"
            )
        )

        // Set default settings if not existing
        if (settingDao().getSettingDirect("currency_code") == null) {
            settingDao().setSetting(AppSettingEntity("currency_code", "PHP"))
        }
        if (settingDao().getSettingDirect("pay_cycle_type") == null) {
            settingDao().setSetting(AppSettingEntity("pay_cycle_type", "monthly"))
        }
        if (settingDao().getSettingDirect("pay_cycle_start_day") == null) {
            settingDao().setSetting(AppSettingEntity("pay_cycle_start_day", "1"))
        }
        if (settingDao().getSettingDirect("app_language") == null) {
            settingDao().setSetting(AppSettingEntity("app_language", "en"))
        }
        if (settingDao().getSettingDirect("hide_balances") == null) {
            settingDao().setSetting(AppSettingEntity("hide_balances", "false"))
        }
    }
}
