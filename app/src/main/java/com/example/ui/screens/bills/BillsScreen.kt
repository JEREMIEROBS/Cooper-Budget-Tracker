package com.example.ui.screens.bills

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillEntity
import com.example.ui.components.AmountText
import com.example.ui.theme.LedgerTheme
import com.example.util.Localization
import com.example.util.MoneyUtils
import java.util.UUID

@Composable
fun BillsScreen(
    bills: List<BillEntity>,
    currentCycleKey: String,
    currencyCode: String,
    hideBalances: Boolean = false,
    lang: String = "en",
    onPayBill: (billId: String, currentCycleKey: String) -> Unit,
    onAddBill: (bill: BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors
    var showAddDialog by remember { mutableStateOf(false) }

    val subscriptions = bills.filter { it.isSubscription }
    val totalSubscriptionsMonthly = subscriptions.sumOf { it.amountMinor }

    LazyColumn(
        modifier = modifier
            .testTag("bills_screen")
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
                        text = Localization.t("bills_title", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Text(
                        text = "Recurring bills, subscriptions, and due dates",
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
                    Text("Add Bill", fontSize = 13.sp)
                }
            }
        }

        // Subscriptions Overview Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL SUBSCRIPTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AmountText(
                            amountMinor = totalSubscriptionsMonthly,
                            currencyCode = currencyCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            overrideColor = colors.text,
                            hideBalances = hideBalances
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${subscriptions.size} active subscriptions",
                            fontSize = 12.sp,
                            color = colors.accent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "UPCOMING & RECURRING",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = colors.textMuted
            )
        }

        items(bills) { bill ->
            val isPaidThisCycle = bill.lastPaidCycle == currentCycleKey
            BillCard(
                bill = bill,
                isPaid = isPaidThisCycle,
                currencyCode = currencyCode,
                hideBalances = hideBalances,
                lang = lang,
                onMarkPaid = { onPayBill(bill.id, currentCycleKey) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddDialog) {
        AddBillDialog(
            currencyCode = currencyCode,
            onDismiss = { showAddDialog = false },
            onConfirm = { bill ->
                onAddBill(bill)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BillCard(
    bill: BillEntity,
    isPaid: Boolean,
    currencyCode: String,
    hideBalances: Boolean,
    lang: String,
    onMarkPaid: () -> Unit
) {
    val colors = LedgerTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPaid) colors.accent.copy(alpha = 0.3f) else colors.border,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) colors.accentSoft.copy(alpha = 0.3f) else colors.surface
        ),
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (bill.isSubscription) colors.accentSoft else colors.border.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (bill.isSubscription) Icons.Default.CreditCard else Icons.Default.Receipt,
                        contentDescription = null,
                        tint = if (bill.isSubscription) colors.accent else colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = bill.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.text
                    )
                    Text(
                        text = "Due every ${bill.dueDay}th of month · ${if (bill.isSubscription) "Subscription" else "Utility"}",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountMinor = bill.amountMinor,
                    currencyCode = currencyCode,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    overrideColor = colors.text,
                    hideBalances = hideBalances
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isPaid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Localization.t("bills_paid", lang),
                            fontSize = 11.sp,
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onMarkPaid,
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = Localization.t("action_mark_paid", lang),
                            fontSize = 11.sp,
                            color = colors.accent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddBillDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (BillEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dueDayStr by remember { mutableStateOf("15") }
    var isSub by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bill / Subscription") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Meralco, Netflix)") },
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
                    value = dueDayStr,
                    onValueChange = { dueDayStr = it },
                    label = { Text("Due Day of Month (1-31)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is this a recurring subscription?")
                    Switch(checked = isSub, onCheckedChange = { isSub = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val minor = MoneyUtils.toMinor(amountStr, currencyCode)
                        val day = dueDayStr.toIntOrNull()?.coerceIn(1, 31) ?: 15
                        onConfirm(
                            BillEntity(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                amountMinor = minor,
                                dueDay = day,
                                categoryId = "cat_utilities",
                                isSubscription = isSub
                            )
                        )
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
