package com.example.ui.screens.utang

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.DebtEntity
import com.example.ui.components.AmountText
import com.example.ui.theme.LedgerTheme
import com.example.util.Localization
import com.example.util.MoneyUtils
import java.util.UUID

@Composable
fun UtangScreen(
    activeDebts: List<DebtEntity>,
    settledDebts: List<DebtEntity>,
    currencyCode: String,
    hideBalances: Boolean = false,
    lang: String = "en",
    onAddDebt: (debt: DebtEntity) -> Unit,
    onRecordPayment: (debtId: String, paymentMinor: Long, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Settled
    var showAddDialog by remember { mutableStateOf(false) }
    var payingDebt by remember { mutableStateOf<DebtEntity?>(null) }

    val owedToMeTotal = activeDebts.filter { it.kind == "owed_to_me" }.sumOf { it.currentBalanceMinor }
    val owedByMeTotal = activeDebts.filter { it.kind == "owed_by_me" }.sumOf { it.currentBalanceMinor }

    LazyColumn(
        modifier = modifier
            .testTag("utang_screen")
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
                        text = Localization.t("utang_title", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Text(
                        text = "Track lent and borrowed funds peacefully",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Localization.t("action_add_debt", lang), fontSize = 13.sp)
                }
            }
        }

        // Summary Cards: Owed to me vs Owed by me
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CallReceived, contentDescription = null, tint = colors.income, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("OWED TO ME", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.income)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AmountText(
                            amountMinor = owedToMeTotal,
                            currencyCode = currencyCode,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            overrideColor = colors.text,
                            hideBalances = hideBalances
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CallMade, contentDescription = null, tint = colors.expense, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("I OWE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.expense)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AmountText(
                            amountMinor = owedByMeTotal,
                            currencyCode = currencyCode,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            overrideColor = colors.text,
                            hideBalances = hideBalances
                        )
                    }
                }
            }
        }

        // Tab Row: Active vs Settled
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surface,
                contentColor = colors.accent,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active (${activeDebts.size})", fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Settled (${settledDebts.size})", fontWeight = FontWeight.Medium) }
                )
            }
        }

        val displayList = if (selectedTab == 0) activeDebts else settledDebts

        if (displayList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (selectedTab == 0) "No active utang recorded." else "No settled entries yet.",
                            fontSize = 14.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        } else {
            items(displayList) { debt ->
                DebtCard(
                    debt = debt,
                    currencyCode = currencyCode,
                    hideBalances = hideBalances,
                    lang = lang,
                    onPay = { payingDebt = debt }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddDialog) {
        AddDebtDialog(
            currencyCode = currencyCode,
            onDismiss = { showAddDialog = false },
            onConfirm = { debt ->
                onAddDebt(debt)
                showAddDialog = false
            }
        )
    }

    payingDebt?.let { debt ->
        RecordDebtPaymentDialog(
            debt = debt,
            currencyCode = currencyCode,
            onDismiss = { payingDebt = null },
            onConfirm = { amountMinor, note ->
                onRecordPayment(debt.id, amountMinor, note)
                payingDebt = null
            }
        )
    }
}

@Composable
fun DebtCard(
    debt: DebtEntity,
    currencyCode: String,
    hideBalances: Boolean,
    lang: String,
    onPay: () -> Unit
) {
    val colors = LedgerTheme.colors
    val isOwedToMe = debt.kind == "owed_to_me"

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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${debt.counterparty}'s balance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isOwedToMe) colors.income.copy(alpha = 0.12f) else colors.expense.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isOwedToMe) "Pautang" else "Utang ko",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOwedToMe) colors.income else colors.expense
                        )
                    }
                }

                if (debt.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = debt.note,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }

                Text(
                    text = "Original: ${if (hideBalances) "••••" else MoneyUtils.formatMoney(debt.initialAmountMinor, currencyCode)}",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountMinor = debt.currentBalanceMinor,
                    currencyCode = currencyCode,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    overrideColor = if (debt.isSettled) colors.accent else colors.text,
                    hideBalances = hideBalances
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (debt.isSettled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Settled", fontSize = 11.sp, color = colors.accent, fontWeight = FontWeight.Medium)
                    }
                } else {
                    OutlinedButton(
                        onClick = onPay,
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = Localization.t("utang_partial_payment", lang), fontSize = 11.sp, color = colors.accent)
                    }
                }
            }
        }
    }
}

@Composable
fun AddDebtDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (DebtEntity) -> Unit
) {
    var counterparty by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("owed_to_me") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Utang Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { kind = "owed_to_me" },
                        modifier = Modifier.weight(1f),
                        colors = if (kind == "owed_to_me") ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFE8F5E9)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Owed to me (Pautang)", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { kind = "owed_by_me" },
                        modifier = Modifier.weight(1f),
                        colors = if (kind == "owed_by_me") ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFFEBEE)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("I owe (Utang ko)", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = counterparty,
                    onValueChange = { counterparty = it },
                    label = { Text("Person's Name (e.g. Mark, Sarah)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    label = { Text("Note (e.g. Shared dinner)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (counterparty.isNotBlank()) {
                        val minor = MoneyUtils.toMinor(amountStr, currencyCode)
                        onConfirm(
                            DebtEntity(
                                id = UUID.randomUUID().toString(),
                                kind = kind,
                                counterparty = counterparty.trim(),
                                initialAmountMinor = minor,
                                currentBalanceMinor = minor,
                                note = note.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordDebtPaymentDialog(
    debt: DebtEntity,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (paymentMinor: Long, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(MoneyUtils.formatPlain(debt.currentBalanceMinor, currencyCode)) }
    var note by remember { mutableStateOf("Payment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment for ${debt.counterparty}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Remaining balance: ${MoneyUtils.formatMoney(debt.currentBalanceMinor, currencyCode)}",
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Payment Amount") },
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
                        onConfirm(minor, note)
                    }
                }
            ) {
                Text("Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
