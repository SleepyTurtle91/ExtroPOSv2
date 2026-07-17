package com.extrotarget.extroposv2.core.domain.commerce

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Shared interface for anything that can be sold (Retail Product, F&B Menu Item, Service).
 */
interface CommerceItem {
    val id: String
    val name: String
    val basePrice: BigDecimal
}

/**
 * The heart of the platform. Represents any transaction regardless of industry.
 */
data class CommerceTransaction(
    val id: String,
    val type: TransactionType,
    val items: List<TransactionLine>,
    val pricingResult: PricingResult,
    val paymentStatus: PaymentStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val customerId: String? = null,
    val tableId: String? = null, // F&B specific, but core-safe as optional metadata
    val staffId: String? = null
)

data class TransactionLine(
    val id: String,
    val commerceItem: CommerceItem,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val modifiers: List<ModifierAdjustment> = emptyList(),
    val notes: String? = null
)

data class ModifierAdjustment(
    val id: String,
    val name: String,
    val priceAdjustment: BigDecimal
)

enum class TransactionType {
    RETAIL_SALE, FNB_ORDER, SERVICE_JOB, BOOKING
}

data class PricingResult(
    val subtotal: BigDecimal,
    val taxAmount: BigDecimal,
    val serviceCharge: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val roundingAdjustment: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal
)

enum class PaymentStatus {
    PENDING, PARTIAL, PAID, VOID
}
