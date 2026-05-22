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
