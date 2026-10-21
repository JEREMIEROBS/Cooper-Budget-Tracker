package com.example

import com.example.util.MoneyUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyUtilsTest {

    @Test
    fun testToMinor_standardUSD() {
        assertEquals(1250L, MoneyUtils.toMinor("12.50", "USD"))
        assertEquals(1200L, MoneyUtils.toMinor("12", "USD"))
        assertEquals(5L, MoneyUtils.toMinor("0.05", "USD"))
        assertEquals(50L, MoneyUtils.toMinor("0.5", "USD"))
    }

    @Test
    fun testToMinor_zero() {
        assertEquals(0L, MoneyUtils.toMinor("0", "USD"))
        assertEquals(0L, MoneyUtils.toMinor("0.00", "USD"))
        assertEquals(0L, MoneyUtils.toMinor("", "USD"))
    }

    @Test
    fun testToMinor_negative() {
        assertEquals(-1250L, MoneyUtils.toMinor("-12.50", "USD"))
        assertEquals(-500L, MoneyUtils.toMinor("-5", "USD"))
    }

    @Test
    fun testToMinor_largeValues() {
        assertEquals(100000000L, MoneyUtils.toMinor("1000000.00", "USD"))
    }

    @Test
    fun testToMinor_zeroDecimalCurrency_JPY() {
        assertEquals(1500L, MoneyUtils.toMinor("1500", "JPY"))
        assertEquals(-2500L, MoneyUtils.toMinor("-2500", "JPY"))
    }

    @Test
    fun testFormatMoney_standardUSD() {
        assertEquals("$12.50", MoneyUtils.formatMoney(1250L, "USD"))
        assertEquals("$0.00", MoneyUtils.formatMoney(0L, "USD"))
        assertEquals("-$12.50", MoneyUtils.formatMoney(-1250L, "USD"))
    }

    @Test
    fun testFormatMoney_large() {
        assertEquals("$1,000,000.00", MoneyUtils.formatMoney(100000000L, "USD"))
    }

    @Test
    fun testFormatMoney_JPY() {
        val jpyFormatted = MoneyUtils.formatMoney(1500L, "JPY")
        // Should not have decimals
        assert(!jpyFormatted.contains("."))
    }
}
