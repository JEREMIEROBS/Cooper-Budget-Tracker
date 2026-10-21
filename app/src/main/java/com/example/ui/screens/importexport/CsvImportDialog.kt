package com.example.ui.screens.importexport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.CsvImportUtils
import com.example.util.CsvMapping
import com.example.util.MoneyUtils

@Composable
fun CsvImportDialog(
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onImport: (List<TransactionEntity>) -> Unit
) {
    val colors = LedgerTheme.colors

    var csvText by remember {
        mutableStateOf(
            "Date,Amount,Description\n" +
            "2026-10-02,-150.00,Starbucks Coffee\n" +
            "2026-10-05,-1240.50,SM Supermarket Groceries\n" +
            "2026-10-10,-450.00,Grab Transport Fare\n" +
            "2026-10-15,+25000.00,Client Consulting Pay"
        )
    }

    var selectedPreset by remember { mutableStateOf("standard") } // "standard", "gcash", "maya"
    var mapping by remember { mutableStateOf(CsvMapping(0, 1, 2, hasHeader = true)) }

    val defaultCategoryId = categories.firstOrNull { it.id == "cat_dining" }?.id
        ?: categories.firstOrNull()?.id ?: "cat_groceries"
    val defaultAccountId = accounts.firstOrNull()?.id ?: "acc_cash"

    val previewRows = remember(csvText, mapping) {
        CsvImportUtils.parsePreview(csvText, mapping, currencyCode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CSV Import & Column Mapping", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Paste your CSV export below. Presets configure column mapping automatically.",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )

                // Presets row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedPreset = "standard"
                            mapping = CsvMapping(0, 1, 2, hasHeader = true)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Standard", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedPreset = "gcash"
                            mapping = CsvMapping(0, 2, 1, hasHeader = true)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("GCash", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedPreset = "maya"
                            mapping = CsvMapping(0, 1, 3, hasHeader = true)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Maya", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    label = { Text("CSV Text") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "PREVIEW (${previewRows.size} rows parsed)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = colors.textMuted
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(previewRows) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${row.dateStr} · ${row.note}",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${if (row.isExpense) "-" else "+"}${MoneyUtils.formatMoney(row.amountMinor, currencyCode)}",
                                    fontSize = 11.sp,
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (row.isExpense) colors.expense else colors.income
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entities = CsvImportUtils.parseAll(
                        csvContent = csvText,
                        mapping = mapping,
                        defaultCategoryId = defaultCategoryId,
                        defaultAccountId = defaultAccountId,
                        currencyCode = currencyCode
                    )
                    if (entities.isNotEmpty()) {
                        onImport(entities)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Import ${previewRows.size} Rows")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
