package com.extrotarget.extroposv2.feature.reporting.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.feature.reporting.domain.model.SalesReport
import com.extrotarget.extroposv2.feature.reporting.domain.model.InventoryReport
import com.extrotarget.extroposv2.feature.reporting.domain.model.FinanceReport
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportingRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) {
    suspend fun getSalesReport(start: Long, end: Long): SalesReport {
        val sales = saleDao.getSalesInRangeNow(start, end)
        val totalRevenue = sales.sumOf { it.totalAmount }
        val transactionCount = sales.size
        val avgTicket = if (transactionCount > 0) totalRevenue.divide(BigDecimal(transactionCount), 2, RoundingMode.HALF_EVEN) else BigDecimal.ZERO

        return SalesReport(
            totalRevenue = totalRevenue,
            transactionCount = transactionCount,
            averageTicketSize = avgTicket,
            topSellingProducts = emptyList() 
        )
    }

    suspend fun getInventoryReport(): InventoryReport {
        val products = productDao.getAllProducts().first()
        val totalValue = products.sumOf { it.price.multiply(it.stockQuantity) }
        val lowStock = products.count { it.stockQuantity <= it.minStockLevel && it.minStockLevel > BigDecimal.ZERO }

        return InventoryReport(
            totalStockValue = totalValue,
            lowStockCount = lowStock,
            movementCount = 0 
        )
    }

    suspend fun getFinanceReport(start: Long, end: Long): FinanceReport {
        val sales = saleDao.getSalesInRangeNow(start, end)
        val tax = sales.sumOf { it.taxAmount }
        val discounts = sales.sumOf { it.discountAmount }
        
        return FinanceReport(
            netProfitEstimate = sales.sumOf { it.subtotal }.subtract(discounts),
            taxLiability = tax,
            totalDiscounts = discounts
        )
    }
}
