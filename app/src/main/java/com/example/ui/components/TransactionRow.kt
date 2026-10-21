package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.LedgerTheme
import com.example.util.DateUtils

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    currencyCode: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    hideBalances: Boolean = false,
    modifier: Modifier = Modifier,
    testTag: String = "transaction_row"
) {
    val colors = LedgerTheme.colors
    val catColor = try {
        if (category?.colorHex != null) Color(android.graphics.Color.parseColor(category.colorHex)) else colors.accent
    } catch (_: Exception) {
        colors.accent
    }

    val isExpense = transaction.kind == "expense"
    val icon = CategoryIconHelper.getIcon(category?.iconName ?: "category")

    Row(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Category Icon Circle
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category?.name ?: "Category",
                modifier = Modifier.size(20.dp),
                tint = catColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Middle column: Title + Category/Time subtitle
        Column(
            modifier = Modifier.weight(1f)
        ) {
            val titleText = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Uncategorized")
            Text(
                text = titleText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val catName = category?.name ?: "General"
                Text(
                    text = catName,
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = " • ",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )

                Text(
                    text = DateUtils.formatTransactionTime(transaction.occurredAt),
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 3. Right side: Amount and optional delete icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            AmountText(
                amountMinor = transaction.amountMinor,
                currencyCode = currencyCode,
                mode = if (isExpense) AmountDisplayMode.EXPENSE_ALWAYS else AmountDisplayMode.INCOME_ALWAYS,
                showSignPrefix = true,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                hideBalances = hideBalances
            )

            if (onDelete != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDelete)
                        .padding(6.dp)
                        .testTag("delete_entry_${transaction.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = colors.textMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
