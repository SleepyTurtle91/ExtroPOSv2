package com.extrotarget.extroposv2.feature.reporting.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.reporting.*
import com.extrotarget.extroposv2.core.domain.model.reporting.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportingRepository @Inject constructor(
    private val reportingDao: ReportingDao,
    private val productRepository: com.extrotarget.extroposv2.core.data.repository.ProductRepository
) {
    fun getSalesSummary(start: Long, end: Long): Flow<SalesSummary> =
        reportingDao.getSalesInRange(start, end).map { sales ->
            val count = sales.size
            val totalSales = sales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.totalAmount) }
            val totalTax = sales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.taxAmount) }
            val totalDiscount = sales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.discountAmount) }
            val totalServiceCharge = sales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.serviceChargeAmount) }
            val netSales = totalSales.subtract(totalTax).subtract(totalServiceCharge)
            val avg = if (count > 0) totalSales.divide(BigDecimal(count), 2, RoundingMode.HALF_EVEN) else BigDecimal.ZERO
            
            SalesSummary(
                transactionCount = count,
                totalSales = totalSales,
                averageTicketSize = avg,
                totalTax = totalTax,
                totalDiscount = totalDiscount,
                totalServiceCharge = totalServiceCharge,
                netSales = netSales
            )
        }

    fun getProductPerformance(start: Long, end: Long): Flow<List<ProductPerformance>> =
        reportingDao.getRawProductPerformance(start, end).map { raw ->
            raw.groupBy { it.productId }.map { (id, items) ->
                ProductPerformance(
                    productId = id,
                    productName = items.first().productName,
                    totalQuantity = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalQuantity) },
                    totalRevenue = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalRevenue) },
                    categoryName = items.first().categoryName ?: "Uncategorized"
                )
            }.sortedByDescending { it.totalQuantity }
        }

    fun getPaymentMethodBreakdown(start: Long, end: Long): Flow<List<PaymentBreakdown>> =
        reportingDao.getRawPaymentBreakdown(start, end).map { raw ->
            raw.groupBy { it.paymentMethod }.map { (method, items) ->
                PaymentBreakdown(
                    paymentMethod = method,
                    totalAmount = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalAmount) }
                )
            }
        }

    fun getTaxComplianceBreakdown(start: Long, end: Long): Flow<List<TaxBreakdownItem>> =
        reportingDao.getRawTaxComplianceBreakdown(start, end).map { raw ->
            raw.groupBy { it.taxRate }.map { (rate, items) ->
                TaxBreakdownItem(
                    taxRate = rate,
                    netSales = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.netSales) },
                    taxAmount = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.taxAmount) }
                )
            }
        }

    fun getStaffCommissionReport(start: Long, end: Long): Flow<List<StaffCommission>> =
        reportingDao.getRawStaffCommissionReport(start, end).map { raw ->
            raw.groupBy { it.staffId }.map { (staffId, items) ->
                val staffName = items.first().staffName ?: "Unknown"
                val totalSales = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalSales) }
                val totalCommission = items.fold(BigDecimal.ZERO) { acc, i -> 
                    val product = productRepository.getProductById(i.productId)
                    val rate = product?.commissionRate ?: BigDecimal.ZERO
                    acc.add(i.totalSales.multiply(rate).divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN))
                }
                StaffCommission(
                    staffId = staffId ?: "unknown",
                    staffName = staffName,
                    totalSales = totalSales,
                    totalCommission = totalCommission
                )
            }
        }

    fun getAddonPerformance(start: Long, end: Long): Flow<List<AddonPerformance>> =
        reportingDao.getRawAddonPerformance(start, end).map { raw ->
            raw.groupBy { it.addonName }.map { (name, items) ->
                AddonPerformance(
                    addonName = name,
                    totalQuantity = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalQuantity) },
                    totalRevenue = items.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalRevenue) }
                )
            }
        }

    fun getOccupancyTrends(start: Long, end: Long): Flow<List<OccupancyTrend>> =
        reportingDao.getSalesInRange(start, end).map { sales ->
            sales.groupBy { s ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = s.timestamp
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.map { (date, dailySales) ->
                OccupancyTrend(
                    date = date,
                    occupancyRate = 0f, // Placeholder, requires room count
                    revenue = dailySales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.totalAmount) }
                )
            }.sortedBy { it.date }
        }
}
