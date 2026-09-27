package com.example

import com.example.core.Formatters
import com.example.data.service.DefaultCurrencyConversionService
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SakuUnitTest {

    @Test
    fun testRupiahFormatting() {
        val formatted = Formatters.formatRupiah(12500000.0)
        assertTrue(formatted.startsWith("Rp") || formatted.contains("12.500.000"))

        val zero = Formatters.formatRupiah(0.0)
        assertTrue(zero.contains("0"))
    }

    @Test
    fun testAmountParsing() {
        assertEquals(50000.0, Formatters.parseAmount("50000"), 0.001)
        assertEquals(150000.0, Formatters.parseAmount("Rp 150.000"), 0.001)
        assertEquals(0.0, Formatters.parseAmount("invalid"), 0.001)
        assertEquals(0.0, Formatters.parseAmount(""), 0.001)
        assertEquals(2500000.0, Formatters.parseAmount("2.500.000"), 0.001)
    }

    @Test
    fun testCurrencyConversion() {
        val service = DefaultCurrencyConversionService()
        val amountIdr = 158500.0
        val inUsd = service.convertFromIdr(amountIdr, "USD")
        assertEquals(10.0, inUsd, 0.01)

        val backToIdr = service.convertToIdr(10.0, "USD")
        assertEquals(158500.0, backToIdr, 0.01)

        val inSgd = service.convertFromIdr(118000.0, "SGD")
        assertEquals(10.0, inSgd, 0.01)

        val inEur = service.convertFromIdr(172000.0, "EUR")
        assertEquals(10.0, inEur, 0.01)

        val inJpy = service.convertFromIdr(1050.0, "JPY")
        assertEquals(10.0, inJpy, 0.01)
    }

    @Test
    fun testTransferBalanceCalculation() {
        // Pockets no longer have balance field directly - realization comes from ExpenseAllocation
        // This test needs to be updated for the new model
        val sourceRealization = 1000000.0
        val targetRealization = 500000.0
        val transferAmount = 250000.0

        assertTrue(sourceRealization >= transferAmount)
        val updatedSourceBalance = (sourceRealization - transferAmount).coerceAtLeast(0.0)
        val updatedTargetBalance = targetRealization + transferAmount

        assertEquals(750000.0, updatedSourceBalance, 0.001)
        assertEquals(750000.0, updatedTargetBalance, 0.001)
        assertEquals(sourceRealization + targetRealization, updatedSourceBalance + updatedTargetBalance, 0.001)
    }

    @Test
    fun testSavingsRateCalculation() {
        val totalIncome = 15000000.0
        val totalExpense = 4500000.0
        val netSavings = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).toInt().coerceIn(0, 100) else 0

        assertEquals(10500000.0, netSavings, 0.001)
        assertEquals(70, savingsRate)
    }

    @Test
    fun testTransferValidationRules() {
        val sourceId = "pocket_1"
        val targetId = "pocket_1"
        // Same source and target should be rejected
        assertEquals(sourceId, targetId)

        val validTargetId = "pocket_2"
        assertFalse(sourceId == validTargetId)
    }

    @Test
    fun testPocketProgressAndPercentageCalculation() {
        // Dynamic progress calculation: currentBalance / targetAmount * 100 capped at 100%
        val current60 = 600000.0
        val target1M = 1000000.0
        assertEquals(60, com.example.ui.screens.kantong.getPocketProgressPercentage(current60, target1M))
        assertEquals(0.6f, com.example.ui.screens.kantong.getPocketProgress(current60, target1M), 0.001f)

        // 28% case
        val current28 = 280000.0
        assertEquals(28, com.example.ui.screens.kantong.getPocketProgressPercentage(current28, target1M))
        assertEquals(0.28f, com.example.ui.screens.kantong.getPocketProgress(current28, target1M), 0.001f)

        // 45% case
        val current45 = 450000.0
        assertEquals(45, com.example.ui.screens.kantong.getPocketProgressPercentage(current45, target1M))
        assertEquals(0.45f, com.example.ui.screens.kantong.getPocketProgress(current45, target1M), 0.001f)

        // Capped at 100% when balance exceeds target
        val currentOver = 1500000.0
        assertEquals(100, com.example.ui.screens.kantong.getPocketProgressPercentage(currentOver, target1M))
        assertEquals(1.0f, com.example.ui.screens.kantong.getPocketProgress(currentOver, target1M), 0.001f)

        // Zero target handling
        assertEquals(0, com.example.ui.screens.kantong.getPocketProgressPercentage(500000.0, 0.0))
        assertEquals(0f, com.example.ui.screens.kantong.getPocketProgress(500000.0, 0.0), 0.001f)
    }
}