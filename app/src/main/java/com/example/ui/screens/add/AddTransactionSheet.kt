package com.example.ui.screens.add

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.FavoriteEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryChip
import com.example.ui.components.Cooper
import com.example.ui.components.CooperPose
import com.example.ui.components.NumberPad
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.DateUtils
import com.example.util.Localization
import com.example.util.MoneyUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity> = emptyList(),
    favorites: List<FavoriteEntity> = emptyList(),
    currencyCode: String,
    onSave: (amountStr: String, categoryId: String, accountId: String, kind: String, note: String, timestamp: Long) -> Unit,
    onSaveFavorite: ((FavoriteEntity) -> Unit)? = null,
    showCooper: Boolean = true,
    lang: String = "en",
    editingTransaction: TransactionEntity? = null,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = LedgerTheme.colors
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var isSuccessCelebrating by remember { mutableStateOf(false) }

    var kind by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.kind ?: "expense")
    }

    var rawAmountString by remember(editingTransaction) {
        mutableStateOf(
            if (editingTransaction != null) {
                MoneyUtils.formatPlain(editingTransaction.amountMinor, currencyCode)
            } else {
                ""
            }
        )
    }

    var selectedCategoryId by remember(editingTransaction, categories) {
        mutableStateOf(
            editingTransaction?.categoryId
                ?: categories.firstOrNull { it.kind == kind }?.id
                ?: ""
        )
    }

    var selectedAccountId by remember(editingTransaction, accounts) {
        mutableStateOf(
            editingTransaction?.accountId
                ?: accounts.firstOrNull()?.id
                ?: "acc_cash"
        )
    }

    var note by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.note ?: "")
    }

    var occurredAt by remember(editingTransaction) {
        mutableLongStateOf(editingTransaction?.occurredAt ?: System.currentTimeMillis())
    }

    val filteredCategories = remember(categories, kind) {
        categories.filter { it.kind == kind }
    }

    LaunchedEffect(kind, filteredCategories) {
        if (filteredCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = filteredCategories.firstOrNull()?.id ?: ""
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null
    ) {
        if (isSuccessCelebrating) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (showCooper) {
                    Cooper(
                        pose = CooperPose.RUN,
                        size = 160.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = Localization.t("mascot_save_success", lang),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .testTag("add_transaction_sheet")
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.border)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingTransaction != null) "Edit Entry" else "Log Entry",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textMuted
                        )
                    }
                }

                // 1-Tap Favorites Row (Feature 3)
                if (favorites.isNotEmpty() && editingTransaction == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE5A100), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FAVORITES (1-TAP)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favorites.take(4)) { fav ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.accentSoft)
                                    .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        fav.amountMinor?.let {
                                            rawAmountString = MoneyUtils.formatPlain(it, currencyCode)
                                        }
                                        selectedCategoryId = fav.categoryId
                                        selectedAccountId = fav.accountId
                                        kind = fav.kind
                                        note = fav.note
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fav.name + if (fav.amountMinor != null) " · ${MoneyUtils.formatMoney(fav.amountMinor, currencyCode)}" else "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.accent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Kind Selector: Expense / Income Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.bg)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (kind == "expense") colors.surface else Color.Transparent)
                            .clickable { kind = "expense" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            fontSize = 13.sp,
                            fontWeight = if (kind == "expense") FontWeight.SemiBold else FontWeight.Normal,
                            color = if (kind == "expense") colors.expense else colors.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (kind == "income") colors.surface else Color.Transparent)
                            .clickable { kind = "income" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            fontSize = 13.sp,
                            fontWeight = if (kind == "income") FontWeight.SemiBold else FontWeight.Normal,
                            color = if (kind == "income") colors.income else colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Large Amount Display
                val currencySymbol = MoneyUtils.getCurrencySymbol(currencyCode)
                val displayText = if (rawAmountString.isEmpty()) "0" else rawAmountString
                val amountColor = if (kind == "expense") colors.expense else colors.income

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.bg)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$currencySymbol$displayText",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = MonoFontFamily,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (rawAmountString.isEmpty()) colors.textMuted else amountColor
                        ),
                        maxLines = 1,
                        modifier = Modifier.testTag("amount_display_value")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account Selector (Feature 7)
                if (accounts.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCOUNT / WALLET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(accounts) { acc ->
                            val isSelected = acc.id == selectedAccountId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accentSoft else colors.bg)
                                    .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(8.dp))
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) colors.accent else colors.text
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Category Selector Chips Row
                Text(
                    text = "CATEGORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = colors.textMuted,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCategories) { cat ->
                        CategoryChip(
                            name = Localization.getCategoryName(cat.id, cat.name, lang),
                            colorHex = cat.colorHex,
                            isSelected = cat.id == selectedCategoryId,
                            onClick = { selectedCategoryId = cat.id },
                            testTag = "chip_${cat.id}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Note & Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Note (e.g. coffee, fare)", fontSize = 13.sp, color = colors.textMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("note_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        )
                    )

                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = occurredAt }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        cal.set(Calendar.YEAR, year)
                                        cal.set(Calendar.MONTH, month)
                                        cal.set(Calendar.DAY_OF_MONTH, day)
                                        occurredAt = cal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(horizontal = 12.dp)
                            .testTag("date_picker_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick Date",
                                tint = colors.textMuted,
                                modifier = Modifier.height(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateUtils.formatFullDate(occurredAt),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom NumberPad
                NumberPad(
                    onDigit = { digit ->
                        if (digit == ".") {
                            if (!rawAmountString.contains(".")) {
                                rawAmountString = if (rawAmountString.isEmpty()) "0." else "$rawAmountString."
                            }
                        } else {
                            val parts = rawAmountString.split(".")
                            if (parts.size == 2 && parts[1].length >= 2) {
                                // Max 2 decimals
                            } else if (rawAmountString.length < 9) {
                                rawAmountString += digit
                            }
                        }
                    },
                    onBackspace = {
                        if (rawAmountString.isNotEmpty()) {
                            rawAmountString = rawAmountString.dropLast(1)
                        }
                    },
                    onClear = {
                        rawAmountString = ""
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (editingTransaction != null && onDelete != null) {
                        Button(
                            onClick = {
                                onDelete(editingTransaction)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.bg,
                                contentColor = colors.expense
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .testTag("delete_transaction_button")
                        ) {
                            Text("Delete", fontWeight = FontWeight.Medium)
                        }
                    }

                    val hasValidAmount = rawAmountString.isNotBlank() && (rawAmountString.toDoubleOrNull() ?: 0.0) > 0.0
                    Button(
                        onClick = {
                            if (hasValidAmount && selectedCategoryId.isNotBlank()) {
                                if (showCooper) {
                                    isSuccessCelebrating = true
                                    coroutineScope.launch {
                                        delay(1200)
                                        onSave(rawAmountString, selectedCategoryId, selectedAccountId, kind, note, occurredAt)
                                        onDismiss()
                                    }
                                } else {
                                    onSave(rawAmountString, selectedCategoryId, selectedAccountId, kind, note, occurredAt)
                                    onDismiss()
                                }
                            }
                        },
                        enabled = hasValidAmount && selectedCategoryId.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = Color.White,
                            disabledContainerColor = colors.border,
                            disabledContentColor = colors.textMuted
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_transaction_button")
                    ) {
                        Text(
                            text = if (editingTransaction != null) "Update" else "Save ${if (kind == "expense") "Expense" else "Income"}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
