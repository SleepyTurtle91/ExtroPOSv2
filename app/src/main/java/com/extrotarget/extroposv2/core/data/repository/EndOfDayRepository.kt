package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.EndOfDayDao
import com.extrotarget.extroposv2.core.data.local.dao.ShiftDao
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.model.EndOfDay
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EndOfDayRepository @Inject constructor(
    private val endOfDayDao: EndOfDayDao,
    private val shiftDao: ShiftDao,
    private val saleDao: SaleDao
) {
    fun getAllEndOfDays(): Flow<List<EndOfDay>> = endOfDayDao.getAllEndOfDays()

    suspend fun generateEndOfDay(staffId: String, staffName: String): EndOfDay? {
        // Find the range of the current business day (from last EOD or start of today)
        // For simplicity, we'll take all closed shifts that haven't been part of an EOD yet
        // In a real scenario, you might want to specify a date.
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val businessDate = calendar.timeInMillis

        // Check if EOD already exists for today
        if (endOfDayDao.getEndOfDayByDate(businessDate) != null) {
            return null 
        }

        // Get all closed shifts for today (or since last EOD)
        // For now, let's just get all closed shifts of the current day
        val startTime = businessDate
        val endTime = System.currentTimeMillis()
        
        val shifts = endOfDayDao.getClosedShiftsInRange(startTime, endTime)
        if (shifts.isEmpty()) return null

        var totalCash = BigDecimal.ZERO
        var totalOther = BigDecimal.ZERO
        var totalTax = BigDecimal.ZERO
        var totalRounding = BigDecimal.ZERO
        
        shifts.forEach {
            totalCash += it.totalCashSales
            totalOther += it.totalOtherSales
            totalTax += it.totalTax
            totalRounding += it.totalRounding
        }

        // We can also fetch more granular data from SaleDao if needed
        // For example, service charge and discount might not be in Shift model yet
        // Let's assume we want to be accurate and query sales directly for the range
        
        // This is a placeholder for actual service charge/discount aggregation from Sales
        val totalSalesInRange = saleDao.getSalesInRangeNow(startTime, endTime)
        
        var totalSC = BigDecimal.ZERO
        var totalDisc = BigDecimal.ZERO
        var grossSales = BigDecimal.ZERO
        var netSales = BigDecimal.ZERO

        totalSalesInRange.forEach { sale ->
            if (sale.status == "COMPLETED") {
                totalSC += sale.serviceChargeAmount
                totalDisc += sale.discountAmount
                grossSales += sale.totalAmount
                netSales += (sale.totalAmount - sale.taxAmount - sale.serviceChargeAmount)
            }
        }

        val eod = EndOfDay(
            businessDate = businessDate,
            startTime = shifts.minOf { it.startTime },
            endTime = shifts.maxOf { it.endTime ?: endTime },
            totalCashSales = totalCash,
            totalOtherSales = totalOther,
            totalTax = totalTax,
            totalServiceCharge = totalSC,
            totalRounding = totalRounding,
            totalDiscount = totalDisc,
            netSales = netSales,
            grossSales = grossSales,
            shiftCount = shifts.size,
            staffId = staffId,
            staffName = staffName
        )

        val id = endOfDayDao.insertEndOfDay(eod)
        return eod.copy(id = id)
    }
}
