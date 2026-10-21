package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LedgerTheme

@Composable
fun CategoryChip(
    name: String,
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "category_chip"
) {
    val colors = LedgerTheme.colors
    val dotColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        colors.accent
    }

    val chipShape = RoundedCornerShape(8.dp)

    val background = if (isSelected) {
        colors.accentSoft
    } else {
        colors.surface
    }

    val borderColor = if (isSelected) {
        colors.accent
    } else {
        colors.border
    }

    Row(
        modifier = modifier
            .testTag(testTag)
            .height(36.dp)
            .clip(chipShape)
            .background(background)
            .border(width = if (isSelected) 1.5.dp else 1.dp, color = borderColor, shape = chipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) colors.accent else colors.text
        )
    }
}
