package com.extrotarget.extroposv2.core.data.local.dao.reporting

import androidx.room.Dao
import androidx.room.Query
import com.extrotarget.extroposv2.core.domain.model.reporting.sales.*
import com.extrotarget.extroposv2.core.domain.model.reporting.inventory.*
import com.extrotarget.extroposv2.core.domain.model.reporting.finance.*
import com.extrotarget.extroposv2.core.domain.model.reporting.staff.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface ReportingDao {

    @Query("SELECT * FROM sales WHERE timestamp BETWEEN :start AND :end AND status = 'COMPLETED'")
    fun getSalesInRange(start: Long, end: Long): Flow<List<com.extrotarget.extroposv2.core.data.model.Sale>>

    @Query("""
        SELECT 
            si.productId, 
            si.productName, 
            si.quantity as totalQuantity, 
            si.totalAmount as totalRevenue,
            c.name as categoryName
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        LEFT JOIN products p ON si.productId = p.id
        LEFT JOIN categories c ON p.categoryId = c.id
        WHERE s.timestamp BETWEEN :start AND :end AND s.status = 'COMPLETED'
    """)
    fun getRawProductPerformance(start: Long, end: Long): Flow<List<RawProductPerformance>>

    @Query("""
        SELECT paymentMethod, totalAmount
        FROM sales
        WHERE timestamp BETWEEN :start AND :end AND status = 'COMPLETED'
    """)
    fun getRawPaymentBreakdown(start: Long, end: Long): Flow<List<RawPaymentBreakdown>>

    @Query("""
        SELECT 
            taxRate, 
            (si.totalAmount - si.taxAmount) as netSales,
            si.taxAmount
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE s.timestamp BETWEEN :start AND :end AND s.status = 'COMPLETED'
    """)
    fun getRawTaxComplianceBreakdown(start: Long, end: Long): Flow<List<RawTaxBreakdownItem>>

    @Query("""
        SELECT 
            assignedStaffId as staffId, 
            assignedStaffName as staffName, 
            si.totalAmount as totalSales,
            productId
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE s.timestamp BETWEEN :start AND :end AND s.status = 'COMPLETED'
    """)
    fun getRawStaffCommissionReport(start: Long, end: Long): Flow<List<RawStaffCommission>>

    @Query("""
        SELECT 
            productName as addonName, 
            quantity as totalQuantity, 
            si.totalAmount as totalRevenue
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE s.timestamp BETWEEN :start AND :end AND s.status = 'COMPLETED' AND printerTag = 'ADDON'
    """)
    fun getRawAddonPerformance(start: Long, end: Long): Flow<List<RawAddonPerformance>>
}

data class RawStaffCommission(
    val staffId: String?,
    val staffName: String?,
    val totalSales: BigDecimal,
    val productId: String
)

data class RawAddonPerformance(
    val addonName: String,
    val totalQuantity: BigDecimal,
    val totalRevenue: BigDecimal
)

data class RawProductPerformance(
    val productId: String,
    val productName: String,
    val totalQuantity: BigDecimal,
    val totalRevenue: BigDecimal,
    val categoryName: String?
)

data class RawPaymentBreakdown(
    val paymentMethod: String,
    val totalAmount: BigDecimal
)

data class RawTaxBreakdownItem(
    val taxRate: BigDecimal,
    val netSales: BigDecimal,
    val taxAmount: BigDecimal
)
