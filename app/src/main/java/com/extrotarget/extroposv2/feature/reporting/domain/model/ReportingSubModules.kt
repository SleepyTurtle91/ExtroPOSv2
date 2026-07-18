package com.extrotarget.extroposv2.feature.reporting.domain.model

import java.math.BigDecimal

data class SalesReport(
    val totalRevenue: BigDecimal,
    val transactionCount: Int,
    val averageTicketSize: BigDecimal,
    val topSellingProducts: List<Pair<String, Int>>
)

data class InventoryReport(
    val totalStockValue: BigDecimal,
    val lowStockCount: Int,
    val movementCount: Int
)

data class FinanceReport(
    val netProfitEstimate: BigDecimal,
    val taxLiability: BigDecimal,
    val totalDiscounts: BigDecimal
)
