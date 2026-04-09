package com.extrotarget.extroposv2.core.data.model.carwash

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Implements the Staff Commission Formula:
 * E_staff = Σ (Pi * Ci) + Fi
 *
 * Where:
 * Pi: Price of service i
 * Ci: Commission percentage for that service
 * Fi: Fixed allowance per job
 */
object CommissionCalculator {

    fun calculate(
        servicePrice: BigDecimal,
        commissionRatePercent: BigDecimal,
        fixedAllowance: BigDecimal
    ): BigDecimal {
        val percentageCommission = servicePrice
            .multiply(commissionRatePercent)
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN)
            
        return percentageCommission.add(fixedAllowance)
            .setScale(2, RoundingMode.HALF_EVEN)
    }

    fun calculateTotalEarnings(records: List<CommissionRecord>): BigDecimal {
        return records.fold(BigDecimal.ZERO) { acc, record ->
            acc.add(record.calculatedCommission)
        }.setScale(2, RoundingMode.HALF_EVEN)
    }
}