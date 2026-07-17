package com.extrotarget.extroposv2.core.domain.commerce

import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PricingEngine @Inject constructor() {

    fun calculateTransactionPricing(
        lines: List<TransactionLine>,
        taxRate: BigDecimal,
        serviceChargeRate: BigDecimal = BigDecimal.ZERO,
        discountAmount: BigDecimal = BigDecimal.ZERO,
        applyRounding: Boolean = true
    ): PricingResult {
        val subtotal = lines.fold(BigDecimal.ZERO) { acc, line -> 
            acc.add(line.subtotal) 
        }

        // Apply Service Charge first (usually on subtotal before tax)
        val serviceCharge = CurrencyUtils.calculateServiceCharge(subtotal, serviceChargeRate)
        
        // Tax is usually calculated on (Subtotal + Service Charge - Discounts)
        // Note: Rules vary by region, but this is common for Malaysia SST
        val taxableAmount = subtotal.add(serviceCharge).subtract(discountAmount)
        val taxAmount = CurrencyUtils.calculateTax(taxableAmount.max(BigDecimal.ZERO), taxRate)
        
        val rawTotal = taxableAmount.add(taxAmount)
        
        return if (applyRounding) {
            val roundingResult = CurrencyUtils.calculateMalaysianRounding(rawTotal)
            PricingResult(
                subtotal = subtotal,
                taxAmount = taxAmount,
                serviceCharge = serviceCharge,
                discountAmount = discountAmount,
                roundingAdjustment = roundingResult.adjustment,
                totalAmount = roundingResult.finalTotal
            )
        } else {
            PricingResult(
                subtotal = subtotal,
                taxAmount = taxAmount,
                serviceCharge = serviceCharge,
                discountAmount = discountAmount,
                totalAmount = rawTotal.setScale(2, RoundingMode.HALF_EVEN)
            )
        }
    }
}
