package com.example.ui.screens.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.GoalEntity
import com.example.ui.components.AmountText
import com.example.ui.components.Cooper
import com.example.ui.components.CooperPose
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.Localization
import com.example.util.MoneyUtils
import java.util.UUID

@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    currencyCode: String,
    hideBalances: Boolean = false,
    showCooper: Boolean = true,
    lang: String = "en",
    onAddFunds: (goalId: String, amountMinor: Long) -> Unit,
    onAddGoal: (goal: GoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgerTheme.colors

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<GoalEntity?>(null) }
    var milestoneCelebrationText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .testTag("goals_screen")
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
                        text = Localization.t("goals_title", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Text(
                        text = "Build emergency funds and long-term targets",
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
                    Text(text = Localization.t("action_add_goal", lang), fontSize = 13.sp)
                }
            }
        }

        // Milestone celebration banner if triggered
        if (milestoneCelebrationText != null && showCooper) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.accentSoft),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Cooper(pose = CooperPose.RUN, size = 52.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = Localization.t("goals_milestone_celebration", lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                                Text(
                                    text = milestoneCelebrationText ?: "",
                                    fontSize = 12.sp,
                                    color = colors.text
                                )
                            }
                        }

                        TextButton(onClick = { milestoneCelebrationText = null }) {
                            Text("Dismiss", fontSize = 11.sp, color = colors.accent)
                        }
                    }
                }
            }
        }

        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showCooper) {
                            Cooper(pose = CooperPose.SIT, size = 120.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(
                            text = "No savings goals created yet.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.text
                        )
                        Text(
                            text = "Start small: an emergency fund or travel dream.",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("Create First Goal")
                        }
                    }
                }
            }
        } else {
            items(goals) { goal ->
                GoalCard(
                    goal = goal,
                    currencyCode = currencyCode,
                    hideBalances = hideBalances,
                    lang = lang,
                    onDeposit = { selectedGoalForDeposit = goal }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            currencyCode = currencyCode,
            onDismiss = { showAddDialog = false },
            onConfirm = { goal ->
                onAddGoal(goal)
                showAddDialog = false
            }
        )
    }

    selectedGoalForDeposit?.let { goal ->
        AddMoneyToGoalDialog(
            goal = goal,
            currencyCode = currencyCode,
            onDismiss = { selectedGoalForDeposit = null },
            onConfirm = { amountMinor ->
                onAddFunds(goal.id, amountMinor)
                val newTotal = goal.currentAmountMinor + amountMinor
                val ratio = if (goal.targetAmountMinor > 0) newTotal.toFloat() / goal.targetAmountMinor.toFloat() else 1f
                val pct = (ratio * 100).toInt()
                if (pct in listOf(25, 50, 75, 100) || (pct >= 100 && goal.currentAmountMinor < goal.targetAmountMinor)) {
                    milestoneCelebrationText = "You reached $pct% of ${goal.name}!"
                }
                selectedGoalForDeposit = null
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    currencyCode: String,
    hideBalances: Boolean,
    lang: String,
    onDeposit: () -> Unit
) {
    val colors = LedgerTheme.colors

    val progress = if (goal.targetAmountMinor > 0) {
        (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val pct = (progress * 100).toInt()

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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = goal.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.accentSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$pct%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.accent,
                trackColor = colors.border
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    AmountText(
                        amountMinor = goal.currentAmountMinor,
                        currencyCode = currencyCode,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        overrideColor = colors.text,
                        hideBalances = hideBalances
                    )
                    Text(
                        text = "Target: ${if (hideBalances) "••••" else MoneyUtils.formatMoney(goal.targetAmountMinor, currencyCode)}",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }

                OutlinedButton(
                    onClick = onDeposit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Localization.t("goals_add_funds", lang), fontSize = 12.sp, color = colors.accent)
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (GoalEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Savings Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Travel, Emergency)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val minor = MoneyUtils.toMinor(targetStr, currencyCode)
                        onConfirm(
                            GoalEntity(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                targetAmountMinor = minor
                            )
                        )
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddMoneyToGoalDialog(
    goal: GoalEntity,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (amountMinor: Long) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Money to ${goal.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Every peso saved moves you closer.", fontSize = 12.sp)
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount to Add") },
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
                        onConfirm(minor)
                    }
                }
            ) {
                Text("Add Funds")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
