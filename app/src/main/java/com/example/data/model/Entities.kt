package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["kind"]),
        Index(value = ["sortOrder"])
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kind: String, // "expense" | "income"
    val colorHex: String,
    val iconName: String,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["occurredAt"]),
        Index(value = ["categoryId", "occurredAt"]),
        Index(value = ["accountId"]),
        Index(value = ["kind"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val accountId: String = "acc_cash", // e.g. Cash, GCash, Maya, Bank
    val amountMinor: Long, // exact integer minor units (cents / centavos)
    val kind: String, // "expense" | "income"
    val note: String = "",
    val occurredAt: Long = System.currentTimeMillis(),
    val isTransfer: Boolean = false,
    val transferId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["categoryId", "cycleKey"], unique = true),
        Index(value = ["cycleKey"])
    ]
)
data class BudgetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val period: String = "cycle",
    val amountMinor: Long, // exact budget in minor units
    val cycleKey: String // e.g. "2026-10", "2026-10-H1", "2026-W42"
)

@Entity(tableName = "settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "cash", "bank", "gcash", "maya", "credit_card", "savings"
    val balanceMinor: Long = 0L,
    val creditLimitMinor: Long = 0L,
    val includeInNetWorth: Boolean = true,
    val colorHex: String = "#1F5E4B",
    val isArchived: Boolean = false
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fromAccountId: String,
    val toAccountId: String,
    val amountMinor: Long,
    val occurredAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amountMinor: Long,
    val dueDay: Int, // 1 - 31
    val repeatRule: String = "monthly", // "monthly", "semimonthly", "weekly"
    val categoryId: String,
    val accountId: String? = "acc_cash",
    val isActive: Boolean = true,
    val isSubscription: Boolean = false,
    val lastPaidCycle: String? = null // Idempotency: avoids paying twice in same cycle
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long = 0L,
    val targetDate: Long? = null,
    val linkedAccountId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goal_entries")
data class GoalEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amountMinor: Long, // positive for contribution, negative for withdrawal
    val occurredAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val kind: String, // "owed_to_me" | "owed_by_me"
    val counterparty: String, // e.g. "Mark", "Ate Sarah"
    val initialAmountMinor: Long,
    val currentBalanceMinor: Long,
    val dueDate: Long? = null,
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val debtId: String,
    val amountMinor: Long,
    val occurredAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amountMinor: Long? = null,
    val categoryId: String,
    val accountId: String = "acc_cash",
    val kind: String = "expense",
    val note: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "saved_filters")
data class SavedFilterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val categoryId: String? = null,
    val accountId: String? = null,
    val kind: String = "all",
    val searchQuery: String = ""
)
