package com.extrotarget.extroposv2.core.util

import java.math.BigDecimal
import java.math.RoundingMode

object CommissionCalculator {
    /**
     * Calculates commission based on the formula:
     * E = Σ (Price × Rate%) + Fixed Fee
     * 
     * @param price The unit price of the service
     * @param ratePercent The commission rate percentage (e.g., 10.00 for 10%)
     * @param fixedAllowance The fixed allowance per job
     * @param quantity The quantity of services performed
     * @return The total calculated commission as BigDecimal
     */
    fun calculate(
        price: BigDecimal,
        ratePercent: BigDecimal,
        fixedAllowance: BigDecimal,
        quantity: BigDecimal = BigDecimal.ONE
    ): BigDecimal {
        val percentageCommission = price.multiply(ratePercent)
            .divide(BigDecimal("100"), 4, RoundingMode.HALF_EVEN)
            
        return percentageCommission.add(fixedAllowance)
            .multiply(quantity)
            .setScale(2, RoundingMode.HALF_EVEN)
    }
}
