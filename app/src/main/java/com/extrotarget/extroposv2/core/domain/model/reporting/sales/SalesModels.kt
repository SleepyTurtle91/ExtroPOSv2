package com.extrotarget.extroposv2.core.domain.model.reporting.sales

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

data class PaymentBreakdown(
    val paymentMethod: String,
    val totalAmount: BigDecimal
)

data class OccupancyTrend(
    val date: Long,
    val occupancyRate: Float,
    val revenue: BigDecimal
)
