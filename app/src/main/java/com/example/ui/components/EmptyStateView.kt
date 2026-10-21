package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LedgerTheme

@Composable
fun EmptyStateView(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    cooperPose: CooperPose? = null,
    showCooper: Boolean = true,
    modifier: Modifier = Modifier,
    testTag: String = "empty_state"
) {
    val colors = LedgerTheme.colors

    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (cooperPose != null && showCooper) {
            Cooper(
                pose = cooperPose,
                size = 190.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        Text(
            text = message,
            fontSize = 15.sp,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}
