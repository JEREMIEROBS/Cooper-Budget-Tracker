package com.example.ui.screens.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.Cooper
import com.example.ui.components.CooperLogoShowcaseDialog
import com.example.ui.components.CooperPose
import com.example.ui.screens.importexport.BackupRestoreDialog
import com.example.ui.screens.importexport.CsvImportDialog
import com.example.ui.theme.LedgerTheme
import com.example.util.ExportUtils
import com.example.util.Localization
import com.example.util.MoneyUtils
import com.example.util.PayCycleType

@Composable
fun SettingsScreen(
    currentCurrency: String,
    onCurrencyChange: (String) -> Unit,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    payCycleType: PayCycleType,
    payCycleStartDay: Int,
    onPayCycleChange: (PayCycleType, Int) -> Unit,
    appLanguage: String,
    onAppLanguageChange: (String) -> Unit,
    hideBalances: Boolean,
    onToggleHideBalances: (Boolean) -> Unit,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity> = emptyList(),
    allTransactions: List<TransactionEntity>,
    onOpenManageCategories: () -> Unit,
    onOpenBills: () -> Unit = {},
    onOpenGoals: () -> Unit = {},
    onOpenUtang: () -> Unit = {},
    onImportTransactions: (List<TransactionEntity>) -> Unit = {},
    onSeedSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    showCooper: Boolean = true,
    onToggleShowCooper: (Boolean) -> Unit = {},
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors
    val context = LocalContext.current

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCycleDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSeedConfirmDialog by remember { mutableStateOf(false) }
    var showLogoShowcaseDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isReminderEnabled by remember { mutableStateOf(true) }

    CooperLogoShowcaseDialog(
        isOpen = showLogoShowcaseDialog,
        isDarkMode = isDarkMode,
        onDismiss = { showLogoShowcaseDialog = false },
        onToggleTheme = {
            val next = if (isDarkMode) "light" else "dark"
            onThemeModeChange(next)
        }
    )

    val supportedCurrencies = listOf(
        "PHP" to "Philippine Peso (₱)",
        "USD" to "US Dollar ($)",
        "EUR" to "Euro (€)",
        "GBP" to "British Pound (£)",
        "CAD" to "Canadian Dollar (CA$)",
        "AUD" to "Australian Dollar (A$)",
        "JPY" to "Japanese Yen (¥)",
        "SGD" to "Singapore Dollar (S$)"
    )

    LazyColumn(
        modifier = modifier
            .testTag("settings_screen")
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Localization.t("tab_settings", appLanguage),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Text(
                text = "Preferences, pay cycles, privacy, and local data",
                fontSize = 13.sp,
                color = colors.textMuted
            )
        }

        if (exportSuccessMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.accentSoft)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exportSuccessMessage ?: "",
                            fontSize = 13.sp,
                            color = colors.accent,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { exportSuccessMessage = null }) {
                            Text("OK", color = colors.accent)
                        }
                    }
                }
            }
        }

        // Feature Modules Shortcut Row (Bills, Goals, Utang)
        item {
            Text(
                text = "FINANCIAL MODULES",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = colors.textMuted
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow(
                        icon = Icons.Default.ReceiptLong,
                        title = Localization.t("bills_title", appLanguage),
                        subtitle = "Track recurring charges, subscriptions, and due dates",
                        onClick = onOpenBills
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))
                    SettingsRow(
                        icon = Icons.Default.Savings,
                        title = Localization.t("goals_title", appLanguage),
                        subtitle = "Savings targets with Cooper milestone celebrations",
                        onClick = onOpenGoals
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))
                    SettingsRow(
                        icon = Icons.Default.Handshake,
                        title = Localization.t("utang_title", appLanguage),
                        subtitle = "Peaceful lend & borrow record keeping",
                        onClick = onOpenUtang
                    )
                }
            }
        }

        // Section: Mascot & Branding
        item {
            Text(
                text = "MASCOT & BRANDING",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = colors.textMuted
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Show Cooper toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Cooper(pose = CooperPose.HEAD, size = 32.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Localization.t("settings_show_cooper", appLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.text
                            )
                            Text(
                                text = "Cooper the husky puppy companion & celebration",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = showCooper,
                            onCheckedChange = onToggleShowCooper,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accent
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    SettingsRow(
                        icon = Icons.Default.Pets,
                        title = "Cooper Mascot Showcase",
                        subtitle = "View light and dark mode logos and animations",
                        onClick = { showLogoShowcaseDialog = true }
                    )
                }
            }
        }

        // Section: Preferences
        item {
            Text(
                text = "PREFERENCES & LOCALIZATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = colors.textMuted
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Language Switcher (Feature 6)
                    SettingsRow(
                        icon = Icons.Default.Language,
                        title = Localization.t("settings_language", appLanguage),
                        subtitle = if (appLanguage == "fil") "Filipino (Tagalog)" else "English",
                        onClick = { showLanguageDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Pay Cycle Selector (Feature 1)
                    SettingsRow(
                        icon = Icons.Default.CalendarMonth,
                        title = Localization.t("settings_pay_cycle", appLanguage),
                        subtitle = "${payCycleType.label}${if (payCycleType == PayCycleType.CUSTOM) " (starts day $payCycleStartDay)" else ""}",
                        onClick = { showCycleDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Hide Balances / Privacy (Feature 5)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Localization.t("settings_hide_balances", appLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.text
                            )
                            Text(
                                text = "Mask amounts with •••• everywhere for privacy",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = hideBalances,
                            onCheckedChange = onToggleHideBalances,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accent
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Appearance
                    SettingsRow(
                        icon = Icons.Default.LightMode,
                        title = "Appearance",
                        subtitle = when (themeMode.lowercase()) {
                            "dark" -> "Dark Mode"
                            "light" -> "Light Mode"
                            else -> "System Default"
                        },
                        onClick = { showThemeDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Currency
                    SettingsRow(
                        icon = Icons.Default.Paid,
                        title = "Default Currency",
                        subtitle = "${MoneyUtils.getCurrencySymbol(currentCurrency)} ($currentCurrency)",
                        onClick = { showCurrencyDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Categories Manager
                    SettingsRow(
                        icon = Icons.Default.Category,
                        title = "Manage Categories",
                        subtitle = "${categories.size} categories configured",
                        onClick = onOpenManageCategories
                    )
                }
            }
        }

        // Section: Data & Backup (Feature 11 & Feature 12)
        item {
            Text(
                text = "DATA & BACKUP (OFFLINE-FIRST)",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = colors.textMuted
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Feature 11: Encrypted Backup & Restore (AES-256)
                    SettingsRow(
                        icon = Icons.Default.VpnKey,
                        title = Localization.t("settings_backup", appLanguage),
                        subtitle = "Password-protected AES-256 export & restore",
                        onClick = { showBackupDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Feature 12: CSV Import
                    SettingsRow(
                        icon = Icons.Default.Upload,
                        title = Localization.t("settings_csv_import", appLanguage),
                        subtitle = "Column mapping presets for GCash, Maya, and Banks",
                        onClick = { showCsvImportDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Export CSV
                    SettingsRow(
                        icon = Icons.Default.Download,
                        title = "Export to CSV Spreadsheet",
                        subtitle = "Share raw transaction table",
                        onClick = {
                            val catMap = categories.associateBy { it.id }
                            val csv = ExportUtils.generateCsv(allTransactions, catMap, currentCurrency)
                            ExportUtils.shareExport(context, csv, "text/csv", "Export Ledger CSV")
                            exportSuccessMessage = "CSV exported successfully."
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border).padding(start = 56.dp))

                    // Seed Sample
                    SettingsRow(
                        icon = Icons.Default.DataExploration,
                        title = "Populate Sample Ledger",
                        subtitle = "Add sample Philippine peso expenses & budgets",
                        onClick = { showSeedConfirmDialog = true }
                    )
                }
            }
        }

        // Danger Zone
        item {
            Text(
                text = "DANGER ZONE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = colors.expense
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                SettingsRow(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete All Data",
                    subtitle = "Irreversible: removes all transactions & budgets",
                    iconTint = colors.expense,
                    titleColor = colors.expense,
                    onClick = { showDeleteConfirmDialog = true }
                )
            }
        }

        // About Cooper Card (Section 4 of Mascot Guide)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Cooper(pose = CooperPose.HEAD, size = 64.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Cooper the Husky Puppy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Text(
                            text = "Cheerful, loyal, guards your budget like a good dog. Celebrates quietly, never shames.",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Currency Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(supportedCurrencies) { (code, label) ->
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCurrencyChange(code)
                                    showCurrencyDialog = false
                                }
                                .padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Language Dialog (Feature 6)
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language / Pumili ng Wika") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAppLanguageChange("en")
                                showLanguageDialog = false
                            }
                            .padding(14.dp)
                    ) {
                        Text("English", fontSize = 15.sp, fontWeight = if (appLanguage == "en") FontWeight.Bold else FontWeight.Normal)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAppLanguageChange("fil")
                                showLanguageDialog = false
                            }
                            .padding(14.dp)
                    ) {
                        Text("Filipino (Tagalog)", fontSize = 15.sp, fontWeight = if (appLanguage == "fil") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Pay Cycle Dialog (Feature 1)
    if (showCycleDialog) {
        var selectedType by remember { mutableStateOf(payCycleType) }
        var startDayStr by remember { mutableStateOf(payCycleStartDay.toString()) }

        AlertDialog(
            onDismissRequest = { showCycleDialog = false },
            title = { Text("Configure Pay Cycle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Budgets and Safe-to-spend follow this cycle.", fontSize = 12.sp, color = colors.textMuted)

                    listOf(
                        PayCycleType.MONTHLY to "Monthly (1st to end of month)",
                        PayCycleType.SEMI_MONTHLY to "Semi-monthly (15th & 30th kinsenas)",
                        PayCycleType.WEEKLY to "Weekly",
                        PayCycleType.CUSTOM to "Custom Start Day"
                    ).forEach { (type, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedType == type) colors.accentSoft else Color.Transparent)
                                .clickable { selectedType = type }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedType == type) colors.accent else colors.text
                            )
                        }
                    }

                    if (selectedType == PayCycleType.CUSTOM) {
                        OutlinedTextField(
                            value = startDayStr,
                            onValueChange = { startDayStr = it },
                            label = { Text("Start Day of Month (1-31)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val day = startDayStr.toIntOrNull()?.coerceIn(1, 31) ?: 1
                        onPayCycleChange(selectedType, day)
                        showCycleDialog = false
                    }
                ) {
                    Text("Apply Cycle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCycleDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Appearance") },
            text = {
                Column {
                    listOf("system" to "System Default", "light" to "Light Mode", "dark" to "Dark Mode").forEach { (mode, label) ->
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeModeChange(mode)
                                    showThemeDialog = false
                                }
                                .padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Seed Sample Confirm Dialog
    if (showSeedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSeedConfirmDialog = false },
            title = { Text("Populate Sample Data?") },
            text = { Text("This will add realistic sample Filipino expenses, accounts, bills, and budgets.") },
            confirmButton = {
                Button(onClick = {
                    onSeedSampleData()
                    showSeedConfirmDialog = false
                }) { Text("Populate") }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete All Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete All Data?", color = colors.expense) },
            text = { Text("This will delete all transactions and budgets. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.expense)
                ) { Text("Delete Everything") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Encrypted Backup & Restore Dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            transactions = allTransactions,
            onDismiss = { showBackupDialog = false },
            onRestoreTransactions = { list ->
                onImportTransactions(list)
                showBackupDialog = false
            }
        )
    }

    // CSV Import Dialog
    if (showCsvImportDialog) {
        CsvImportDialog(
            categories = categories,
            accounts = accounts,
            currencyCode = currentCurrency,
            onDismiss = { showCsvImportDialog = false },
            onImport = { list ->
                onImportTransactions(list)
                showCsvImportDialog = false
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = LedgerTheme.colors.accent,
    titleColor: Color = LedgerTheme.colors.text,
    onClick: () -> Unit
) {
    val colors = LedgerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
