package com.extrotarget.extroposv2.core.domain.model.reporting.finance

import java.math.BigDecimal

data class TaxBreakdownItem(
    val taxRate: BigDecimal,
    val netSales: BigDecimal,
    val taxAmount: BigDecimal
)
