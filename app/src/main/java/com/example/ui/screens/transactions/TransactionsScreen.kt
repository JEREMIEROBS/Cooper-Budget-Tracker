package com.example.ui.screens.transactions

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SavedFilterEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.AmountDisplayMode
import com.example.ui.components.AmountText
import com.example.ui.components.CategoryChip
import com.example.ui.components.CooperPose
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TransactionRow
import com.example.ui.theme.LedgerTheme
import com.example.util.DateUtils
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity> = emptyList(),
    savedFilters: List<SavedFilterEntity> = emptyList(),
    currencyCode: String,
    hideBalances: Boolean = false,
    lang: String = "en",
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategoryId: String?,
    onCategoryFilterChange: (String?) -> Unit,
    selectedAccountId: String? = null,
    onAccountFilterChange: ((String?) -> Unit)? = null,
    selectedKind: String,
    onKindFilterChange: (String) -> Unit,
    onApplySavedFilter: ((SavedFilterEntity) -> Unit)? = null,
    onClearFilters: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onOpenAdd: () -> Unit,
    showCooper: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors
    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    // Group transactions by date string
    val groupedTransactions = remember(transactions) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        transactions.groupBy { tx ->
            dateFormat.format(Date(tx.occurredAt))
        }
    }

    val filteredTotalMinor = remember(transactions) {
        transactions.sumOf { if (it.kind == "expense") -it.amountMinor else it.amountMinor }
    }

    val isFilterActive = searchQuery.isNotBlank() || selectedCategoryId != null || selectedAccountId != null || selectedKind != "all"

    Column(
        modifier = modifier
            .testTag("transactions_screen")
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // Top Filter Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .border(1.dp, colors.border)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search notes or categories...", fontSize = 13.sp, color = colors.textMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border,
                    focusedContainerColor = colors.bg,
                    unfocusedContainerColor = colors.bg,
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text
                )
            )

            // Saved Filters Chips (Feature 4)
            if (savedFilters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(savedFilters) { sf ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.accentSoft)
                                .clickable { onApplySavedFilter?.invoke(sf) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, tint = colors.accent, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(sf.name, fontSize = 11.sp, color = colors.accent, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kind filter chips (All, Expense, Income)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "all" to "All",
                    "expense" to "Expenses",
                    "income" to "Income"
                ).forEach { (id, label) ->
                    val isSelected = selectedKind == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.accentSoft else colors.bg)
                            .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(8.dp))
                            .clickable { onKindFilterChange(id) }
                            .padding(vertical = 8.dp)
                            .testTag("filter_kind_$id"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.accent else colors.textMuted
                        )
                    }
                }
            }

            // Category filter chips (Feature 4)
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        val isAllCatSelected = selectedCategoryId == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllCatSelected) colors.accentSoft else colors.bg)
                                .border(1.dp, if (isAllCatSelected) colors.accent else colors.border, RoundedCornerShape(8.dp))
                                .clickable { onCategoryFilterChange(null) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("filter_cat_all")
                        ) {
                            Text(
                                text = "All Categories",
                                fontSize = 11.sp,
                                fontWeight = if (isAllCatSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isAllCatSelected) colors.accent else colors.textMuted
                            )
                        }
                    }

                    items(categories) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        val catName = Localization.getCategoryName(cat.id, cat.name, lang)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.accentSoft else colors.bg)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(8.dp))
                                .clickable { onCategoryFilterChange(cat.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("filter_cat_${cat.id}")
                        ) {
                            Text(
                                text = catName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) colors.accent else colors.textMuted
                            )
                        }
                    }
                }
            }

            // Accounts filter chips (Feature 7)
            if (accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        val isAllSelected = selectedAccountId == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isAllSelected) colors.accentSoft else colors.bg)
                                .border(1.dp, if (isAllSelected) colors.accent else colors.border, RoundedCornerShape(6.dp))
                                .clickable { onAccountFilterChange?.invoke(null) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "All Accounts",
                                fontSize = 11.sp,
                                fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isAllSelected) colors.accent else colors.textMuted
                            )
                        }
                    }

                    items(accounts) { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) colors.accentSoft else colors.bg)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(6.dp))
                                .clickable { onAccountFilterChange?.invoke(acc.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = acc.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) colors.accent else colors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // Transactions List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${transactions.size} ${if (transactions.size == 1) "entry" else "entries"}",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )

                    if (isFilterActive) {
                        TextButton(onClick = onClearFilters) {
                            Text(text = Localization.t("action_clear_filters", lang), fontSize = 12.sp, color = colors.accent)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    if (isFilterActive) {
                        // Acceptance criteria 4: Empty result state has one sentence and a "Clear filters" action
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No matching transactions found.",
                                    fontSize = 14.sp,
                                    color = colors.textMuted
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(onClick = onClearFilters) {
                                    Text(text = Localization.t("action_clear_filters", lang))
                                }
                            }
                        }
                    } else {
                        // Empty state: Transactions -> Cooper PLAY pose per Mascot Guide Section 4
                        EmptyStateView(
                            message = Localization.t("mascot_empty_transactions", lang),
                            actionLabel = Localization.t("action_add_expense", lang),
                            onAction = onOpenAdd,
                            cooperPose = CooperPose.PLAY,
                            showCooper = showCooper,
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                }
            } else {
                groupedTransactions.forEach { (_, txsInGroup) ->
                    val firstTimestamp = txsInGroup.first().occurredAt
                    item {
                        Text(
                            text = DateUtils.formatDayGroupHeader(firstTimestamp).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = colors.textMuted,
                            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
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
                                txsInGroup.forEachIndexed { index, tx ->
                                    val category = categoriesMap[tx.categoryId]
                                    TransactionRow(
                                        transaction = tx,
                                        category = category,
                                        currencyCode = currencyCode,
                                        hideBalances = hideBalances,
                                        onClick = { onTransactionClick(tx) },
                                        onDelete = { onDeleteTransaction(tx) }
                                    )
                                    if (index < txsInGroup.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(colors.border)
                                                .padding(start = 64.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
