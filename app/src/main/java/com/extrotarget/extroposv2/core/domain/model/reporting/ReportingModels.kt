package com.extrotarget.extroposv2.core.domain.model.reporting

import java.math.BigDecimal

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
