package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "end_of_days")
data class EndOfDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessDate: Long, // Start of the business day timestamp
    val startTime: Long,
    val endTime: Long,
    val totalCashSales: BigDecimal = BigDecimal.ZERO,
    val totalOtherSales: BigDecimal = BigDecimal.ZERO,
    val totalTax: BigDecimal = BigDecimal.ZERO,
    val totalServiceCharge: BigDecimal = BigDecimal.ZERO,
    val totalRounding: BigDecimal = BigDecimal.ZERO,
    val totalDiscount: BigDecimal = BigDecimal.ZERO,
    val netSales: BigDecimal = BigDecimal.ZERO,
    val grossSales: BigDecimal = BigDecimal.ZERO,
    val shiftCount: Int = 0,
    val staffId: String, // Who performed EOD
    val staffName: String,
    val terminalId: String = "TERM-01"
)
