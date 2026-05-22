package com.extrotarget.extroposv2.core.domain.model.reporting

import java.math.BigDecimal

data class SalesSummary(
    val transactionCount: Int,
    val totalSales: BigDecimal,
    val averageTicketSize: BigDecimal,
    val totalTax: BigDecimal,
    val totalDiscount: BigDecimal,
    val totalServiceCharge: BigDecimal,
    val netSales: BigDecimal
)

data class ProductPerformance(
    val productId: String,
    val productName: String,
    val totalQuantity: BigDecimal,
    val totalRevenue: BigDecimal,
    val categoryName: String
)

data class PaymentBreakdown(
    val paymentMethod: String,
    val totalAmount: BigDecimal
)

data class TaxBreakdownItem(
    val taxRate: BigDecimal,
    val netSales: BigDecimal,
    val taxAmount: BigDecimal
)

data class StaffCommission(
    val staffId: String,
    val staffName: String,
    val totalSales: BigDecimal,
    val totalCommission: BigDecimal
)

data class AddonPerformance(
    val addonName: String,
    val totalQuantity: BigDecimal,
    val totalRevenue: BigDecimal
)

data class OccupancyTrend(
    val date: Long,
    val occupancyRate: Float,
    val revenue: BigDecimal
)
