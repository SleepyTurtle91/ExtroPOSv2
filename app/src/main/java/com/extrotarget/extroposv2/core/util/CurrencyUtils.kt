package com.extrotarget.extroposv2.core.util

import com.extrotarget.extroposv2.core.config.AppConfig
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private var currencyLocale = AppConfig.Locales.DEFAULT_CURRENCY
    private var currencyFormat = NumberFormat.getCurrencyInstance(currencyLocale)

    fun updateLocale(locale: Locale) {
        currencyLocale = locale
        currencyFormat = NumberFormat.getCurrencyInstance(locale)
    }

    fun getCurrencySymbol(): String {
        return currencyFormat.currency?.symbol ?: "$"
    }

    fun format(amount: BigDecimal): String {
        return currencyFormat.format(amount)
    }

    /**
     * Calculates tax based on the given rate.
     */
    fun calculateTax(amount: BigDecimal, taxRate: BigDecimal): BigDecimal {
        return amount.multiply(taxRate)
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN)
    }

    fun calculateServiceCharge(amount: BigDecimal, serviceChargeRate: BigDecimal): BigDecimal {
        return amount.multiply(serviceChargeRate)
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
     * Logic:
     * 1, 2 -> 0 (Round down)
     * 3, 4 -> 5 (Round up)
     * 6, 7 -> 5 (Round down)
     * 8, 9 -> 10 (Round up)
     */
    fun applyMalaysianRounding(amount: BigDecimal): BigDecimal {
        val amountInSen = amount.multiply(BigDecimal("100")).setScale(0, RoundingMode.HALF_UP)
        val lastDigit = amountInSen.remainder(BigDecimal.TEN).toInt()
        
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
        
        return amount.add(BigDecimal(adjustmentInSen).divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN))
            .setScale(2, RoundingMode.HALF_EVEN)
    }

    /**
     * Returns the rounding adjustment amount (positive or negative).
     */
    fun calculateRoundingAdjustment(totalAmount: BigDecimal): BigDecimal {
        val amountInSen = totalAmount.multiply(BigDecimal("100")).setScale(0, RoundingMode.HALF_UP)
        val lastDigit = amountInSen.remainder(BigDecimal.TEN).toInt()
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