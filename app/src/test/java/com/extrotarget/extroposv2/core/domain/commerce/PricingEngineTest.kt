package com.extrotarget.extroposv2.core.domain.commerce

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class PricingEngineTest {

    private val pricingEngine = PricingEngine()

    @Test
    fun testRetailPricingWithSst() {
        val item = object : CommerceItem {
            override val id = "1"
            override val name = "Test Item"
            override val basePrice = BigDecimal("100.00")
        }
        val lines = listOf(
            TransactionLine("l1", item, BigDecimal.ONE, BigDecimal("100.00"), BigDecimal("100.00"))
        )
        
        val result = pricingEngine.calculateTransactionPricing(
            lines = lines,
            taxRate = BigDecimal("8.00"),
            applyRounding = false
        )
        
        assertEquals(BigDecimal("100.00"), result.subtotal)
        assertEquals(BigDecimal("8.00"), result.taxAmount)
        assertEquals(BigDecimal("108.00"), result.totalAmount)
    }

    @Test
    fun testFnbPricingWithServiceChargeAndSst() {
        val item = object : CommerceItem {
            override val id = "1"
            override val name = "Burger"
            override val basePrice = BigDecimal("20.00")
        }
        val lines = listOf(
            TransactionLine("l1", item, BigDecimal.ONE, BigDecimal("20.00"), BigDecimal("20.00"))
        )
        
        // Malaysia F&B logic: 10% Service Charge, 6% SST (calculated on subtotal + sc)
        val result = pricingEngine.calculateTransactionPricing(
            lines = lines,
            taxRate = BigDecimal("6.00"),
            serviceChargeRate = BigDecimal("10.00"),
            applyRounding = false
        )
        
        assertEquals(BigDecimal("20.00"), result.subtotal)
        assertEquals(BigDecimal("2.00"), result.serviceCharge)
        // Taxable = 20 + 2 = 22. Tax = 22 * 0.06 = 1.32
        assertEquals(BigDecimal("1.32"), result.taxAmount)
        assertEquals(BigDecimal("23.32"), result.totalAmount)
    }

    @Test
    fun testBnmRounding() {
        val item = object : CommerceItem {
            override val id = "1"
            override val name = "Rounding Test"
            override val basePrice = BigDecimal("1.01")
        }
        val lines = listOf(
            TransactionLine("l1", item, BigDecimal.ONE, BigDecimal("1.01"), BigDecimal("1.01"))
        )
        
        val result = pricingEngine.calculateTransactionPricing(
            lines = lines,
            taxRate = BigDecimal.ZERO,
            applyRounding = true
        )
        
        // 1.01 should round down to 1.00
        assertEquals(BigDecimal("1.00"), result.totalAmount)
        assertEquals(BigDecimal("-0.01"), result.roundingAdjustment)
    }

    @Test
    fun testDiscountBeforeTax() {
        val item = object : CommerceItem {
            override val id = "1"
            override val name = "Discount Test"
            override val basePrice = BigDecimal("100.00")
        }
        val lines = listOf(
            TransactionLine("l1", item, BigDecimal.ONE, BigDecimal("100.00"), BigDecimal("100.00"))
        )
        
        val result = pricingEngine.calculateTransactionPricing(
            lines = lines,
            taxRate = BigDecimal("8.00"),
            discountAmount = BigDecimal("10.00"),
            applyRounding = false
        )
        
        // Subtotal = 100
        // Taxable = 100 - 10 = 90
        // Tax = 90 * 0.08 = 7.20
        // Total = 90 + 7.20 = 97.20
        assertEquals(BigDecimal("7.20"), result.taxAmount)
        assertEquals(BigDecimal("97.20"), result.totalAmount)
    }
}
