package com.example.ui.screens.importexport

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.LedgerTheme
import com.example.util.SecurityUtils
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun BackupRestoreDialog(
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onRestoreTransactions: (List<TransactionEntity>) -> Unit
) {
    val colors = LedgerTheme.colors

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Backup, 1: Restore
    var password by remember { mutableStateOf("") }
    var encryptedPayload by remember { mutableStateOf("") }
    var restoreInput by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }
    var restoreSummary by remember { mutableStateOf<String?>(null) }
    var pendingRestoredTransactions by remember { mutableStateOf<List<TransactionEntity>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "AES-256 Encrypted Backup & Restore",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = colors.surface,
                    contentColor = colors.accent,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Backup (Export)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Restore") }
                    )
                }

                if (selectedTab == 0) {
                    Text(
                        text = "Your data will be encrypted with AES-256 before leaving your phone. Passwords are never stored.",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Encryption Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (password.length >= 4) {
                                try {
                                    val json = JSONObject()
                                    json.put("schemaVersion", 2)
                                    json.put("exportedAt", System.currentTimeMillis())
                                    val txArr = JSONArray()
                                    for (tx in transactions) {
                                        val obj = JSONObject()
                                        obj.put("id", tx.id)
                                        obj.put("categoryId", tx.categoryId)
                                        obj.put("accountId", tx.accountId)
                                        obj.put("amountMinor", tx.amountMinor)
                                        obj.put("kind", tx.kind)
                                        obj.put("note", tx.note)
                                        obj.put("occurredAt", tx.occurredAt)
                                        txArr.put(obj)
                                    }
                                    json.put("transactions", txArr)

                                    val encrypted = SecurityUtils.encrypt(json.toString(), password.toCharArray())
                                    encryptedPayload = encrypted
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = "Encryption failed: ${e.message}"
                                }
                            } else {
                                errorMessage = "Password must be at least 4 characters"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Encrypted Backup")
                    }

                    if (encryptedPayload.isNotBlank()) {
                        Text(
                            text = "Backup ready (${encryptedPayload.length} bytes encrypted)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent
                        )
                        OutlinedTextField(
                            value = encryptedPayload,
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Restore tab
                    Text(
                        text = "Paste the encrypted payload and enter your password. You will see a summary preview before applying.",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )

                    OutlinedTextField(
                        value = restoreInput,
                        onValueChange = { restoreInput = it },
                        label = { Text("Encrypted Backup Payload") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = { restorePassword = it },
                        label = { Text("Decryption Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            try {
                                val decryptedJson = SecurityUtils.decrypt(restoreInput.trim(), restorePassword.toCharArray())
                                val root = JSONObject(decryptedJson)
                                val txArr = root.optJSONArray("transactions") ?: JSONArray()
                                val list = mutableListOf<TransactionEntity>()
                                for (i in 0 until txArr.length()) {
                                    val obj = txArr.getJSONObject(i)
                                    list.add(
                                        TransactionEntity(
                                            id = obj.optString("id"),
                                            categoryId = obj.optString("categoryId"),
                                            accountId = obj.optString("accountId", "acc_cash"),
                                            amountMinor = obj.optLong("amountMinor"),
                                            kind = obj.optString("kind", "expense"),
                                            note = obj.optString("note", ""),
                                            occurredAt = obj.optLong("occurredAt", System.currentTimeMillis())
                                        )
                                    )
                                }
                                pendingRestoredTransactions = list
                                restoreSummary = "Valid backup verified: ${list.size} transactions found."
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = "Incorrect password or corrupted file."
                                restoreSummary = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Validate & Preview")
                    }

                    restoreSummary?.let { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colors.accentSoft)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(summary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        pendingRestoredTransactions?.let {
                                            onRestoreTransactions(it)
                                            onDismiss()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                                ) {
                                    Text("Apply Restore")
                                }
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Text(it, fontSize = 12.sp, color = colors.expense)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
