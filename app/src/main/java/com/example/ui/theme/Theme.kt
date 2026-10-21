package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class LedgerCustomColors(
    val bg: Color,
    val surface: Color,
    val border: Color,
    val text: Color,
    val textMuted: Color,
    val accent: Color,
    val accentSoft: Color,
    val expense: Color,
    val income: Color,
    val warning: Color
)

val LocalLedgerColors = staticCompositionLocalOf {
    LedgerCustomColors(
        bg = LedgerBgLight,
        surface = LedgerSurfaceLight,
        border = LedgerBorderLight,
        text = LedgerTextLight,
        textMuted = LedgerTextMutedLight,
        accent = LedgerAccentLight,
        accentSoft = LedgerAccentSoftLight,
        expense = LedgerExpenseLight,
        income = LedgerIncomeLight,
        warning = LedgerWarningLight
    )
}

private val LightColorScheme = lightColorScheme(
    primary = LedgerAccentLight,
    onPrimary = Color.White,
    primaryContainer = LedgerAccentSoftLight,
    onPrimaryContainer = LedgerAccentLight,
    background = LedgerBgLight,
    onBackground = LedgerTextLight,
    surface = LedgerSurfaceLight,
    onSurface = LedgerTextLight,
    surfaceVariant = LedgerBgLight,
    onSurfaceVariant = LedgerTextMutedLight,
    outline = LedgerBorderLight,
    outlineVariant = LedgerBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = LedgerAccentDark,
    onPrimary = Color.Black,
    primaryContainer = LedgerAccentSoftDark,
    onPrimaryContainer = LedgerAccentDark,
    background = LedgerBgDark,
    onBackground = LedgerTextDark,
    surface = LedgerSurfaceDark,
    onSurface = LedgerTextDark,
    surfaceVariant = LedgerSurfaceDark,
    onSurfaceVariant = LedgerTextMutedDark,
    outline = LedgerBorderDark,
    outlineVariant = LedgerBorderDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional paper ledger colors rather than dynamic system override
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColors = if (darkTheme) {
        LedgerCustomColors(
            bg = LedgerBgDark,
            surface = LedgerSurfaceDark,
            border = LedgerBorderDark,
            text = LedgerTextDark,
            textMuted = LedgerTextMutedDark,
            accent = LedgerAccentDark,
            accentSoft = LedgerAccentSoftDark,
            expense = LedgerExpenseDark,
            income = LedgerIncomeDark,
            warning = LedgerWarningDark
        )
    } else {
        LedgerCustomColors(
            bg = LedgerBgLight,
            surface = LedgerSurfaceLight,
            border = LedgerBorderLight,
            text = LedgerTextLight,
            textMuted = LedgerTextMutedLight,
            accent = LedgerAccentLight,
            accentSoft = LedgerAccentSoftLight,
            expense = LedgerExpenseLight,
            income = LedgerIncomeLight,
            warning = LedgerWarningLight
        )
    }

    CompositionLocalProvider(LocalLedgerColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object LedgerTheme {
    val colors: LedgerCustomColors
        @Composable
        get() = LocalLedgerColors.current
}
