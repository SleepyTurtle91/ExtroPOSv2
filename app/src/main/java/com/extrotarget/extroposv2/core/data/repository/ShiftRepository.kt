package com.extrotarget.extroposv2.core.data.repository

import androidx.room.withTransaction
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.ShiftDao
import com.extrotarget.extroposv2.core.data.model.AdjustmentType
import com.extrotarget.extroposv2.core.data.model.Shift
import com.extrotarget.extroposv2.core.data.model.ShiftAdjustment
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftRepository @Inject constructor(
    private val shiftDao: ShiftDao,
    private val database: AppDatabase
) {
    fun getActiveShift(): Flow<Shift?> = shiftDao.getActiveShift()
    
    suspend fun getActiveShiftNow(): Shift? = shiftDao.getActiveShiftNow()

    fun getAllShifts(): Flow<List<Shift>> = shiftDao.getAllShifts()

    suspend fun openShift(shift: Shift): Long = shiftDao.insertShift(shift)

    suspend fun closeShift(shift: Shift) = shiftDao.updateShift(shift)
    
    suspend fun updateShift(shift: Shift) = shiftDao.updateShift(shift)

    fun getAdjustmentsForShift(shiftId: Long) = shiftDao.getAdjustmentsForShift(shiftId)

    suspend fun addAdjustment(adjustment: ShiftAdjustment) {
        database.withTransaction {
            shiftDao.insertAdjustment(adjustment)
            if (adjustment.type == AdjustmentType.CASH_IN) {
                shiftDao.updateCashIn(adjustment.shiftId, adjustment.amount)
            } else {
                shiftDao.updateCashOut(adjustment.shiftId, adjustment.amount)
            }
        }
    }

    suspend fun recordSale(shiftId: Long, cashAmount: BigDecimal, otherAmount: BigDecimal, taxAmount: BigDecimal, rounding: BigDecimal) {
        database.withTransaction {
            if (cashAmount != BigDecimal.ZERO) shiftDao.updateCashSales(shiftId, cashAmount)
            if (otherAmount != BigDecimal.ZERO) shiftDao.updateOtherSales(shiftId, otherAmount)
            if (taxAmount != BigDecimal.ZERO) shiftDao.updateTax(shiftId, taxAmount)
            if (rounding != BigDecimal.ZERO) shiftDao.updateRounding(shiftId, rounding)
        }
    }
}
