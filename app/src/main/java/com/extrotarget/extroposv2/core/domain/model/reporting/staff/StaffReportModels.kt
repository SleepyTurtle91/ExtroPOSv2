package com.extrotarget.extroposv2.core.domain.model.reporting.staff

import java.math.BigDecimal

data class StaffCommission(
    val staffId: String,
    val staffName: String,
    val totalSales: BigDecimal,
    val totalCommission: BigDecimal
)
