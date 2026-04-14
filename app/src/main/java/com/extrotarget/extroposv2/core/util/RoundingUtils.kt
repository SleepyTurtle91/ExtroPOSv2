package com.extrotarget.extroposv2.core.util

import java.math.BigDecimal
import java.math.RoundingMode

object RoundingUtils {
    val SST_RATE = BigDecimal("0.06")

    /**
     * Malaysian BNM Rounding (5-sen)
     * Cash transactions only.
     * 1, 2 -> 0
     * 3, 4 -> 5
     * 6, 7 -> 5
     * 8, 9 -> 10
     */
    fun calculateBNMRounding(total: BigDecimal): RoundingResult {
        // Get the last digit (cents)
        val cents = total.multiply(BigDecimal("100"))
            .remainder(BigDecimal("10"))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()

        val adjustment = when (cents) {
            1 -> BigDecimal("-0.01")
            2 -> BigDecimal("-0.02")
            3 -> BigDecimal("0.02")
            4 -> BigDecimal("0.01")
            6 -> BigDecimal("-0.01")
            7 -> BigDecimal("-0.02")
            8 -> BigDecimal("0.02")
            9 -> BigDecimal("0.01")
            else -> BigDecimal.ZERO
        }

        return RoundingResult(
            adjustment = adjustment,
            finalTotal = total.add(adjustment).setScale(2, RoundingMode.HALF_UP)
        )
    }

    data class RoundingResult(
        val adjustment: BigDecimal,
        val finalTotal: BigDecimal
    )
}
