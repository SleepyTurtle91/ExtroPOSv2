package com.extrotarget.extroposv2.core.data.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "tax_configs")
data class TaxConfig(
    @PrimaryKey val id: String = "default_tax",
    val defaultTaxRate: BigDecimal = BigDecimal("0.00"), // Default no tax
    val isTaxEnabled: Boolean = false,
    val taxName: String = "Tax",
    val isServiceChargeEnabled: Boolean = false,
    val serviceChargeRate: BigDecimal = BigDecimal("10.00")
)
