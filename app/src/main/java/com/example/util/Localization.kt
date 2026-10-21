package com.example.util

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    FILIPINO("fil", "Filipino (Tagalog)");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

object Localization {

    private val translations = mapOf(
        // Tabs
        "tab_overview" to mapOf("en" to "Overview", "fil" to "Pangkalahatan"),
        "tab_entries" to mapOf("en" to "Entries", "fil" to "Mga Gastos"),
        "tab_budgets" to mapOf("en" to "Budgets", "fil" to "Mga Budget"),
        "tab_accounts" to mapOf("en" to "Accounts", "fil" to "Mga Account"),
        "tab_settings" to mapOf("en" to "Settings", "fil" to "Mga Setting"),

        // Safe to spend
        "safe_to_spend_title" to mapOf("en" to "SAFE TO SPEND TODAY", "fil" to "LIGTAS GASTUSIN NGAYON"),
        "safe_to_spend_normal" to mapOf("en" to "You can spend %s today.", "fil" to "Maaari kang gumastos ng %s ngayon."),
        "safe_to_spend_over" to mapOf("en" to "You're %s over for this cycle.", "fil" to "Lampas ka ng %s para sa cycle na ito."),
        "safe_to_spend_sub" to mapOf("en" to "%s remaining in cycle · %d days left", "fil" to "%s natitira sa cycle · %d araw na lang"),
        "safe_to_spend_bills_deducted" to mapOf("en" to "(includes %s upcoming bills)", "fil" to "(kasama ang %s paparating na bayarin)"),

        // Mascot microcopy
        "mascot_empty_transactions" to mapOf("en" to "Nothing logged yet. Add your first expense.", "fil" to "Wala pang naka-log. Idagdag ang una mong gastos."),
        "mascot_empty_budgets" to mapOf("en" to "Set your first budget.", "fil" to "Itakda ang iyong unang budget."),
        "mascot_save_success" to mapOf("en" to "Logged.", "fil" to "Naka-log na."),
        "mascot_under_budget" to mapOf("en" to "You're on track this cycle.", "fil" to "Maayos ang takbo ng budget mo ngayong cycle."),
        "mascot_reminder" to mapOf("en" to "Anything to log today?", "fil" to "May gastos ka ba ngayon na hindi pa naka-log?"),

        // Actions
        "action_add_expense" to mapOf("en" to "Log Expense", "fil" to "Magtala ng Gastos"),
        "action_add_income" to mapOf("en" to "Log Income", "fil" to "Magtala ng Kita"),
        "action_transfer" to mapOf("en" to "Transfer", "fil" to "Maglipat ng Pera"),
        "action_save" to mapOf("en" to "Save", "fil" to "I-save"),
        "action_cancel" to mapOf("en" to "Cancel", "fil" to "Kanselahin"),
        "action_clear_filters" to mapOf("en" to "Clear filters", "fil" to "Alisin ang mga filter"),
        "action_reconcile" to mapOf("en" to "Reconcile Balance", "fil" to "Ayusin ang Balanse"),
        "action_mark_paid" to mapOf("en" to "Mark as Paid", "fil" to "Markahan bilang Bayad"),
        "action_add_goal" to mapOf("en" to "Add Goal", "fil" to "Bagong Layunin"),
        "action_add_debt" to mapOf("en" to "Add Utang Entry", "fil" to "Magtala ng Utang"),

        // Utang tracker
        "utang_title" to mapOf("en" to "Utang Tracker (Lend & Borrow)", "fil" to "Talaan ng Utang (Pautang at Utang)"),
        "utang_owed_to_me" to mapOf("en" to "Owed to Me (Pautang)", "fil" to "May Utang sa Akin (Pautang)"),
        "utang_owed_by_me" to mapOf("en" to "I Owe (Utang Ko)", "fil" to "Utang Ko sa Iba"),
        "utang_settled" to mapOf("en" to "Settled", "fil" to "Bayad Na"),
        "utang_partial_payment" to mapOf("en" to "Record Payment", "fil" to "Magbayad"),

        // Bills & subscriptions
        "bills_title" to mapOf("en" to "Bills & Subscriptions", "fil" to "Mga Bayarin at Subskripsyon"),
        "bills_upcoming" to mapOf("en" to "Upcoming in this Cycle", "fil" to "Paparating sa Cycle na Ito"),
        "bills_paid" to mapOf("en" to "Paid this Cycle", "fil" to "Bayad na Ngayong Cycle"),
        "bills_monthly_total" to mapOf("en" to "Monthly Cost", "fil" to "Buwanang Gastos"),

        // Savings goals
        "goals_title" to mapOf("en" to "Savings Goals", "fil" to "Mga Layunin sa Pag-iipon"),
        "goals_add_funds" to mapOf("en" to "Add Money", "fil" to "Magdagdag ng Ipon"),
        "goals_milestone_celebration" to mapOf("en" to "Way to go! Milestone reached!", "fil" to "Galing! Naabot ang milestone!"),

        // Categories localized
        "cat_groceries" to mapOf("en" to "Groceries", "fil" to "Pamamalengke"),
        "cat_dining" to mapOf("en" to "Dining & Coffee", "fil" to "Pagkain at Kape"),
        "cat_housing" to mapOf("en" to "Housing & Rent", "fil" to "Upang Bahay"),
        "cat_transport" to mapOf("en" to "Transport & Fuel", "fil" to "Pasahe at Gasolina"),
        "cat_utilities" to mapOf("en" to "Utilities & Bills", "fil" to "Kuryente, Tubig at Net"),
        "cat_entertainment" to mapOf("en" to "Entertainment", "fil" to "Libangan"),
        "cat_health" to mapOf("en" to "Health & Personal", "fil" to "Kalusugan at Sarili"),
        "cat_shopping" to mapOf("en" to "Shopping", "fil" to "Pamimili"),
        "cat_salary" to mapOf("en" to "Salary & Wages", "fil" to "Sahod at Suweldo"),
        "cat_freelance" to mapOf("en" to "Freelance & Consulting", "fil" to "Raket at Proyekto"),
        "cat_investments" to mapOf("en" to "Investments", "fil" to "Pamumuhunan"),
        "cat_other_income" to mapOf("en" to "Other Income", "fil" to "Ibang Kita"),

        // Accounts
        "acc_cash" to mapOf("en" to "Cash (Wallet)", "fil" to "Pera sa Pitaka (Cash)"),
        "acc_gcash" to mapOf("en" to "GCash", "fil" to "GCash"),
        "acc_maya" to mapOf("en" to "Maya", "fil" to "Maya"),
        "acc_bank" to mapOf("en" to "Bank Account", "fil" to "Bangko"),

        // Settings
        "settings_language" to mapOf("en" to "Language / Wika", "fil" to "Wika / Language"),
        "settings_pay_cycle" to mapOf("en" to "Pay Cycle", "fil" to "Siklo ng Sahod (Pay Cycle)"),
        "settings_hide_balances" to mapOf("en" to "Hide Balances (Privacy)", "fil" to "Itago ang mga Balanse (Privacy)"),
        "settings_backup" to mapOf("en" to "Encrypted Backup & Restore", "fil" to "Naka-encrypt na Backup at Restore"),
        "settings_csv_import" to mapOf("en" to "CSV Import", "fil" to "Mag-import mula sa CSV"),
        "settings_show_cooper" to mapOf("en" to "Show Cooper Mascot", "fil" to "Ipakita si Cooper Mascot")
    )

    fun t(key: String, lang: String = "en", vararg args: Any): String {
        val entry = translations[key]
        val template = entry?.get(lang) ?: entry?.get("en") ?: key
        return if (args.isNotEmpty()) {
            try {
                String.format(template, *args)
            } catch (_: Exception) {
                template
            }
        } else {
            template
        }
    }

    fun getCategoryName(id: String, defaultName: String, lang: String): String {
        val entry = translations[id]
        return entry?.get(lang) ?: entry?.get("en") ?: defaultName
    }
}
