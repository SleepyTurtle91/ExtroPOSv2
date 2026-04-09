package com.extrotarget.extroposv2.core.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "MY"))

    fun format(amount: BigDecimal): String {
        return currencyFormat.format(amount)
    }

    /**
     * Calculates tax based on the given rate.
     * Malaysian SST is typically 5%, 6%, 8%, or 10%.
     */
    fun calculateTax(amount: BigDecimal, taxRate: BigDecimal): BigDecimal {
        return amount.multiply(taxRate)
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN)
    }

    /**
     * Calculates the total amount including tax and applying discounts.
     */
    fun calculateTotal(amount: BigDecimal, taxAmount: BigDecimal, discountAmount: BigDecimal): BigDecimal {
        return amount.add(taxAmount).subtract(discountAmount)
            .setScale(2, RoundingMode.HALF_EVEN)
    }

    /**
     * Bank Negara Malaysia (BNM) Rounding Mechanism to the nearest 5 sen.
     * Required for the final total in cash transactions.
     */
    fun applyMalaysianRounding(amount: BigDecimal): BigDecimal {
        val remainder = amount.remainder(BigDecimal("0.10"))
        val roundedRemainder = when {
            remainder < BigDecimal("0.03") -> BigDecimal("0.00")
            remainder < BigDecimal("0.08") -> BigDecimal("0.05")
            else -> BigDecimal("0.10")
        }
        return amount.subtract(remainder).add(roundedRemainder).setScale(2, RoundingMode.HALF_EVEN)
    }

    /**
     * Returns the rounding adjustment amount (positive or negative).
     */
    fun calculateRoundingAdjustment(totalAmount: BigDecimal): BigDecimal {
        val amountInSen = totalAmount.multiply(BigDecimal("100")).toInt()
        val lastDigit = amountInSen % 10
        val adjustmentInSen = when (lastDigit) {
            1 -> -1
            2 -> -2
            3 -> 2
            4 -> 1
            6 -> -1
            7 -> -2
            8 -> 2
            9 -> 1
            else -> 0
        }
        return BigDecimal(adjustmentInSen).divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN)
    }
}