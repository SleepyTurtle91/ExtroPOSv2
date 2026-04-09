package com.extrotarget.extroposv2.core.data.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "tax_configs")
data class TaxConfig(
    @PrimaryKey val id: String = "default_tax",
    val defaultTaxRate: BigDecimal = BigDecimal("6.00"), // Default 6% SST
    val isTaxEnabled: Boolean = true,
    val taxName: String = "SST",
    val isServiceChargeEnabled: Boolean = false,
    val serviceChargeRate: BigDecimal = BigDecimal("10.00")
)
