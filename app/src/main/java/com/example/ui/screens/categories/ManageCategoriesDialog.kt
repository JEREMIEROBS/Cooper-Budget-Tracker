package com.example.ui.screens.categories

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.CategoryEntity
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.CategoryColors
import com.example.ui.theme.LedgerTheme

@Composable
fun ManageCategoriesDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    categories: List<CategoryEntity>,
    onAddCategory: (name: String, kind: String, colorHex: String, iconName: String) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    if (!isOpen) return

    val colors = LedgerTheme.colors
    var isAddingNew by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatKind by remember { mutableStateOf("expense") }
    var newCatColorHex by remember { mutableStateOf("#1F5E4B") }
    var newCatIcon by remember { mutableStateOf("shopping_cart") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Categories",
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
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (isAddingNew) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.bg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "New Category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Expense / Income toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (newCatKind == "expense") colors.accentSoft else Color.Transparent)
                                    .clickable { newCatKind = "expense" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Expense",
                                    fontSize = 12.sp,
                                    color = if (newCatKind == "expense") colors.accent else colors.textMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (newCatKind == "income") colors.accentSoft else Color.Transparent)
                                    .clickable { newCatKind = "income" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Income",
                                    fontSize = 12.sp,
                                    color = if (newCatKind == "income") colors.accent else colors.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            placeholder = { Text("Category name", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_category_name_input"),
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.border
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color picker palette
                        Text("Color", fontSize = 11.sp, color = colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CategoryColors) { c ->
                                val hex = String.format("#%06X", (0xFFFFFF and c.hashCode()))
                                val isSelected = newCatColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = colors.text,
                                            shape = CircleShape
                                        )
                                        .clickable { newCatColorHex = hex }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Icon picker
                        Text("Icon", fontSize = 11.sp, color = colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CategoryIconHelper.availableIcons) { iconKey ->
                                val isSelected = newCatIcon == iconKey
                                val iconVector = CategoryIconHelper.getIcon(iconKey)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) colors.accentSoft else colors.surface)
                                        .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(6.dp))
                                        .clickable { newCatIcon = iconKey },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = iconKey,
                                        tint = if (isSelected) colors.accent else colors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isAddingNew = false }) {
                                Text("Cancel", color = colors.textMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newCatName.isNotBlank()) {
                                        onAddCategory(newCatName, newCatKind, newCatColorHex, newCatIcon)
                                        newCatName = ""
                                        isAddingNew = false
                                    }
                                },
                                enabled = newCatName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Text("Add")
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { isAddingNew = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentSoft,
                            contentColor = colors.accent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_new_category_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Custom Category")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of existing categories
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    items(categories) { cat ->
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            colors.accent
                        }
                        val icon = CategoryIconHelper.getIcon(cat.iconName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = cat.name,
                                    tint = catColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cat.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.text
                                )
                                Text(
                                    text = cat.kind.replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }

                            // Only allow delete if more than 3 categories exist
                            if (categories.size > 3) {
                                IconButton(
                                    onClick = { onDeleteCategory(cat.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Category",
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = colors.accent, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = colors.surface
    )
}
