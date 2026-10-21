package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LedgerTheme
import com.example.ui.theme.MonoFontFamily
import com.example.util.MoneyUtils

enum class AmountDisplayMode {
    NEUTRAL,
    SIGN_COLORED, // Red if expense, green if income
    EXPENSE_ALWAYS,
    INCOME_ALWAYS
}

@Composable
fun AmountText(
    amountMinor: Long,
    currencyCode: String = "PHP",
    modifier: Modifier = Modifier,
    mode: AmountDisplayMode = AmountDisplayMode.NEUTRAL,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    showSignPrefix: Boolean = false,
    overrideColor: Color? = null,
    hideBalances: Boolean = false,
    testTag: String = "amount_text"
) {
    val colors = LedgerTheme.colors

    val textColor = when {
        overrideColor != null -> overrideColor
        mode == AmountDisplayMode.EXPENSE_ALWAYS -> colors.expense
        mode == AmountDisplayMode.INCOME_ALWAYS -> colors.income
        mode == AmountDisplayMode.SIGN_COLORED -> {
            if (amountMinor < 0) colors.expense else colors.income
        }
        else -> colors.text
    }

    val formatted = if (hideBalances) {
        "••••"
    } else {
        MoneyUtils.formatMoney(
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            includeSymbol = true,
            showPositiveSign = showSignPrefix && amountMinor > 0
        )
    }

    Text(
        text = formatted,
        modifier = modifier.testTag(testTag),
        style = TextStyle(
            fontFamily = MonoFontFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = textColor
        )
    )
}
