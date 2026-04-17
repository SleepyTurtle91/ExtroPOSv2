package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffId: String,
    val staffName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val startFloat: BigDecimal,
    val endActualCash: BigDecimal? = null,
    val endExpectedCash: BigDecimal? = null,
    val totalCashSales: BigDecimal = BigDecimal.ZERO,
    val totalOtherSales: BigDecimal = BigDecimal.ZERO,
    val totalTax: BigDecimal = BigDecimal.ZERO,
    val totalRounding: BigDecimal = BigDecimal.ZERO,
    val cashIn: BigDecimal = BigDecimal.ZERO,
    val cashOut: BigDecimal = BigDecimal.ZERO,
    val isClosed: Boolean = false,
    val terminalId: String = "TERM-01"
)
