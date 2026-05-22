package com.extrotarget.extroposv2.core.domain.model.reporting

import java.math.BigDecimal

data class ProductPerformance(
    val productId: String,
    val productName: String,
    val totalQuantity: BigDecimal,
    val totalRevenue: BigDecimal,
    val categoryName: String? = null
)
