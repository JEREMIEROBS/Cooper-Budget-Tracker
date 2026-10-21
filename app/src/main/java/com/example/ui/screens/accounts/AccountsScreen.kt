package com.example.ui.screens.accounts

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.AccountEntity
import com.example.ui.components.AmountText
import com.example.ui.theme.LedgerTheme
import com.example.util.Localization
import com.example.util.MoneyUtils

@Composable
fun AccountsScreen(
    accounts: List<AccountEntity>,
    currencyCode: String,
    hideBalances: Boolean = false,
    lang: String = "en",
    onTransfer: (fromId: String, toId: String, amountMinor: Long, note: String) -> Unit,
    onReconcile: (accountId: String, actualBalanceMinor: Long, note: String) -> Unit,
    onAddAccount: (name: String, type: String, initialMinor: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors

    var showTransferDialog by remember { mutableStateOf(false) }
    var reconcilingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    val totalNetWorth = accounts.filter { it.includeInNetWorth }.sumOf { it.balanceMinor }

    LazyColumn(
        modifier = modifier
            .testTag("accounts_screen")
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Localization.t("tab_accounts", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Text(
                        text = "Cash, GCash, Maya, and Bank balances",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }

                Button(
                    onClick = { showTransferDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Localization.t("action_transfer", lang), fontSize = 13.sp)
                }
            }
        }

        // Net Worth Hero Card
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
                    Text(
                        text = "TOTAL NET WORTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AmountText(
                        amountMinor = totalNetWorth,
                        currencyCode = currencyCode,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        overrideColor = colors.text,
                        hideBalances = hideBalances,
                        testTag = "total_net_worth"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${accounts.size} active accounts linked",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACCOUNTS & WALLETS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = colors.textMuted
                )

                TextButton(onClick = { showAddAccountDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Account", fontSize = 12.sp)
                }
            }
        }

        items(accounts) { account ->
            AccountRow(
                account = account,
                currencyCode = currencyCode,
                hideBalances = hideBalances,
                onReconcile = { reconcilingAccount = account }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Transfer Modal
    if (showTransferDialog && accounts.size >= 2) {
        TransferDialog(
            accounts = accounts,
            currencyCode = currencyCode,
            onDismiss = { showTransferDialog = false },
            onConfirm = { fromId, toId, amountMinor, note ->
                onTransfer(fromId, toId, amountMinor, note)
                showTransferDialog = false
            }
        )
    }

    // Reconcile Dialog
    reconcilingAccount?.let { acc ->
        ReconcileDialog(
            account = acc,
            currencyCode = currencyCode,
            onDismiss = { reconcilingAccount = null },
            onConfirm = { actualMinor, note ->
                onReconcile(acc.id, actualMinor, note)
                reconcilingAccount = null
            }
        )
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        AddAccountDialog(
            currencyCode = currencyCode,
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, type, minor ->
                onAddAccount(name, type, minor)
                showAddAccountDialog = false
            }
        )
    }
}

@Composable
fun AccountRow(
    account: AccountEntity,
    currencyCode: String,
    hideBalances: Boolean,
    onReconcile: () -> Unit
) {
    val colors = LedgerTheme.colors

    val icon = when (account.type.lowercase()) {
        "gcash", "maya" -> Icons.Default.Smartphone
        "bank" -> Icons.Default.AccountBalance
        "credit_card" -> Icons.Default.Payment
        else -> Icons.Default.Wallet
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = account.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.text
                    )
                    Text(
                        text = account.type.uppercase(),
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountMinor = account.balanceMinor,
                    currencyCode = currencyCode,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    overrideColor = colors.text,
                    hideBalances = hideBalances
                )

                TextButton(
                    onClick = onReconcile,
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reconcile", fontSize = 11.sp, color = colors.accent)
                }
            }
        }
    }
}

@Composable
fun TransferDialog(
    accounts: List<AccountEntity>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (fromId: String, toId: String, amountMinor: Long, note: String) -> Unit
) {
    var fromId by remember { mutableStateOf(accounts.first().id) }
    var toId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.first().id) }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Between Accounts") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Transfers do not count as expenses.", fontSize = 12.sp)

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minor = MoneyUtils.toMinor(amountStr, currencyCode)
                    if (minor > 0L) {
                        onConfirm(fromId, toId, minor, note.ifBlank { "Account Transfer" })
                    }
                }
            ) {
                Text("Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReconcileDialog(
    account: AccountEntity,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (actualBalanceMinor: Long, note: String) -> Unit
) {
    var actualBalanceStr by remember {
        mutableStateOf(MoneyUtils.formatPlain(account.balanceMinor, currencyCode))
    }
    var note by remember { mutableStateOf("Reconciled actual balance") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reconcile ${account.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter the current verified balance. The app will log an adjustment entry for any difference.",
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = actualBalanceStr,
                    onValueChange = { actualBalanceStr = it },
                    label = { Text("Actual Current Balance") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minor = MoneyUtils.toMinor(actualBalanceStr, currencyCode)
                    onConfirm(minor, note)
                }
            ) {
                Text("Save Balance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddAccountDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, initialMinor: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("gcash") }
    var initialStr by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account or E-Wallet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. Maya, BDO, Cash)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialStr,
                    onValueChange = { initialStr = it },
                    label = { Text("Starting Balance") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val minor = MoneyUtils.toMinor(initialStr, currencyCode)
                        onConfirm(name.trim(), type, minor)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
