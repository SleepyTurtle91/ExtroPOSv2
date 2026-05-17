package com.extrotarget.extroposv2.core.data.local.dao.reporting

import androidx.room.Dao
import androidx.room.Query
import com.extrotarget.extroposv2.core.data.model.Sale
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface ReportingDao {

    @Query("SELECT * FROM sales WHERE timestamp BETWEEN :start AND :end")
    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>>

    @Query("""
        SELECT 
            si.productId, 
            si.productName, 
            SUM(si.quantity) as totalQuantity, 
            SUM(si.totalAmount) as totalRevenue,
            p.categoryId as categoryName
        FROM sale_items si
        JOIN sales s ON si.saleId = s.id
        LEFT JOIN products p ON si.productId = p.id
        WHERE s.timestamp BETWEEN :start AND :end
        GROUP BY si.productId
    """)
    fun getRawProductPerformance(start: Long, end: Long): Flow<List<RawProductPerformance>>

    @Query("""
        SELECT paymentMethod, SUM(totalAmount) as totalAmount
        FROM sales
        WHERE timestamp BETWEEN :start AND :end
        GROUP BY paymentMethod
    """)
    fun getRawPaymentBreakdown(start: Long, end: Long): Flow<List<RawPaymentBreakdown>>

    @Query("""
        SELECT 
            si.taxRate, 
            SUM(si.totalAmount - si.taxAmount) as netSales, 
            SUM(si.taxAmount) as taxAmount
        FROM sale_items si
        JOIN sales s ON si.saleId = s.id
        WHERE s.timestamp BETWEEN :start AND :end
        GROUP BY si.taxRate
    """)
    fun getRawTaxComplianceBreakdown(start: Long, end: Long): Flow<List<RawTaxComplianceBreakdown>>

    @Query("""
        SELECT 
            s.staffId, 
            st.name as staffName, 
            si.productId,
            SUM(si.totalAmount) as totalSales
        FROM sale_items si
        JOIN sales s ON si.saleId = s.id
        LEFT JOIN staff st ON s.staffId = st.id
        WHERE s.timestamp BETWEEN :start AND :end
        GROUP BY s.staffId, si.productId
    """)
    fun getRawStaffCommissionReport(start: Long, end: Long): Flow<List<RawStaffCommission>>

    @Query("""
        SELECT si.productName as addonName, SUM(si.quantity) as totalQuantity, SUM(si.totalAmount) as totalRevenue
        FROM sale_items si
        JOIN sales s ON si.saleId = s.id
        WHERE s.timestamp BETWEEN :start AND :end AND si.productId LIKE 'ADDON_%'
        GROUP BY si.productName
    """)
    fun getRawAddonPerformance(start: Long, end: Long): Flow<List<RawAddonPerformance>>
}

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

data class RawTaxComplianceBreakdown(
    val taxRate: BigDecimal,
    val netSales: BigDecimal,
    val taxAmount: BigDecimal
)

data class RawStaffCommission(
    val staffId: String?,
    val staffName: String?,
    val productId: String,
    val totalSales: BigDecimal
)

data class RawAddonPerformance(
    val addonName: String,
    val totalQuantity: BigDecimal,
    val totalRevenue: BigDecimal
)
