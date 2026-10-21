package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

/**
 * Money utilities enforcing exact integer minor units (e.g. cents).
 * Float arithmetic is strictly avoided.
 */
object MoneyUtils {

    // Currencies with 0 decimal minor units
    private val ZERO_DECIMAL_CURRENCIES = setOf("JPY", "KRW", "VND", "CLP")

    fun getMinorUnitFactor(currencyCode: String): Long {
        return if (ZERO_DECIMAL_CURRENCIES.contains(currencyCode.uppercase())) 1L else 100L
    }

    fun getDecimals(currencyCode: String): Int {
        return if (ZERO_DECIMAL_CURRENCIES.contains(currencyCode.uppercase())) 0 else 2
    }

    fun getCurrencySymbol(currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode.uppercase())
            currency.getSymbol(Locale.getDefault())
        } catch (_: Exception) {
            when (currencyCode.uppercase()) {
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                "PHP" -> "₱"
                "CAD" -> "CA$"
                "AUD" -> "A$"
                "INR" -> "₹"
                else -> currencyCode
            }
        }
    }

    /**
     * Converts a user entered string representation (e.g. "12.50" or "12") to integer minor units (1250).
     * Avoids float inaccuracies by string parsing.
     */
    fun toMinor(input: String, currencyCode: String = "USD"): Long {
        val clean = input.trim().replace(",", "").replace("$", "").replace("€", "").replace("£", "")
        if (clean.isBlank()) return 0L

        val isNegative = clean.startsWith("-")
        val unsigned = if (isNegative) clean.substring(1) else clean

        val decimals = getDecimals(currencyCode)
        if (decimals == 0) {
            val intPart = unsigned.takeWhile { it.isDigit() }
            val amount = intPart.toLongOrNull() ?: 0L
            return if (isNegative) -amount else amount
        }

        val parts = unsigned.split(".")
        val intPart = parts.getOrNull(0)?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
        val fracString = parts.getOrNull(1)?.filter { it.isDigit() } ?: ""
        val normalizedFrac = (fracString + "00").take(2).toLongOrNull() ?: 0L

        val totalMinor = (intPart * 100L) + normalizedFrac
        return if (isNegative) -totalMinor else totalMinor
    }

    /**
     * Formats integer minor units to a clean readable string with currency symbol.
     * e.g. 1250 minor units -> "$12.50"
     */
    fun formatMoney(
        amountMinor: Long,
        currencyCode: String = "USD",
        includeSymbol: Boolean = true,
        showPositiveSign: Boolean = false
    ): String {
        val isNegative = amountMinor < 0
        val absMinor = kotlin.math.abs(amountMinor)
        val decimals = getDecimals(currencyCode)
        val symbol = if (includeSymbol) getCurrencySymbol(currencyCode) else ""

        val formattedNumber = if (decimals == 0) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("#,##0", symbols)
            formatter.format(absMinor)
        } else {
            val major = absMinor / 100L
            val minor = absMinor % 100L
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("#,##0", symbols)
            val majorStr = formatter.format(major)
            val minorStr = String.format(Locale.US, "%02d", minor)
            "$majorStr.$minorStr"
        }

        return when {
            isNegative -> "-$symbol$formattedNumber"
            showPositiveSign && amountMinor > 0 -> "+$symbol$formattedNumber"
            else -> "$symbol$formattedNumber"
        }
    }

    /**
     * Formats minor units without currency symbol (for inputs or custom badge views).
     */
    fun formatPlain(amountMinor: Long, currencyCode: String = "USD"): String {
        return formatMoney(amountMinor, currencyCode, includeSymbol = false)
    }
}
