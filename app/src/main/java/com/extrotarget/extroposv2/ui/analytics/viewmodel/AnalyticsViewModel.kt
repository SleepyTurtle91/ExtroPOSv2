package com.extrotarget.extroposv2.ui.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.util.exporter.SstReportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.io.OutputStream
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TaxReportItem(
    val categoryName: String,
    val netSales: BigDecimal,
    val taxAmount: BigDecimal,
    val taxRate: BigDecimal
)

data class ChartDataPoint(
    val label: String,
    val value: Float
)

data class AnalyticsUiState(
    val totalSales: BigDecimal = BigDecimal.ZERO,
    val totalTax: BigDecimal = BigDecimal.ZERO,
    val totalDiscount: BigDecimal = BigDecimal.ZERO,
    val totalRounding: BigDecimal = BigDecimal.ZERO,
    val salesCount: Int = 0,
    val taxReports: List<TaxReportItem> = emptyList(),
    val salesTrend: List<ChartDataPoint> = emptyList(),
    val categorySplit: List<ChartDataPoint> = emptyList(),
    val startDate: Long = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // Last 24h
    val endDate: Long = System.currentTimeMillis()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val sstReportManager: SstReportManager
) : ViewModel() {

    private val _dateRange = MutableStateFlow(Pair(
        Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis,
        Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis
    ))

    val uiState: StateFlow<AnalyticsUiState> = _dateRange.flatMapLatest { range ->
        saleRepository.getAllSalesWithItems().map { allSalesWithItems ->
            val filtered = allSalesWithItems.filter { it.sale.timestamp in range.first..range.second }
            val sales = filtered.map { it.sale }
            val items = filtered.flatMap { it.items }

            val taxReport = items.groupBy { it.taxRate }
                .map { (rate, itemsAtRate) ->
                    TaxReportItem(
                        categoryName = "SST @ ${rate.stripTrailingZeros().toPlainString()}%",
                        netSales = itemsAtRate.sumOfItems { it.totalAmount.subtract(it.taxAmount) },
                        taxAmount = itemsAtRate.sumOfItems { it.taxAmount },
                        taxRate = rate
                    )
                }

            // Calculate Sales Trend (Hourly or Daily depending on range)
            val trendFormat = SimpleDateFormat("HH:00", Locale.getDefault())
            val salesTrend = filtered.groupBy { trendFormat.format(Date(it.sale.timestamp)) }
                .map { (label, salesInGroup) ->
                    ChartDataPoint(label, salesInGroup.map { it.sale }.sumOfSales { it.totalAmount }.toFloat())
                }.sortedBy { it.label }

            // Calculate Category Split
            val categorySplit = items.groupBy { it.productName } // Should use category if available in SaleItem
                .map { (name, itemsInGroup) ->
                    ChartDataPoint(name, itemsInGroup.sumOfItems { it.totalAmount }.toFloat())
                }.sortedByDescending { it.value }.take(5)

            AnalyticsUiState(
                totalSales = sales.sumOfSales { it.totalAmount },
                totalTax = sales.sumOfSales { it.taxAmount },
                totalDiscount = sales.sumOfSales { it.discountAmount },
                totalRounding = sales.sumOfSales { it.roundingAdjustment },
                salesCount = sales.size,
                taxReports = taxReport,
                salesTrend = salesTrend,
                categorySplit = categorySplit,
                startDate = range.first,
                endDate = range.second
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    fun setDateRange(start: Long, end: Long) {
        _dateRange.value = Pair(start, end)
    }

    suspend fun exportSstReport(outputStream: OutputStream): Result<Int> {
        return sstReportManager.generateSstCsvReport(
            _dateRange.value.first,
            _dateRange.value.second,
            outputStream
        )
    }

    private fun Iterable<Sale>.sumOfSales(selector: (Sale) -> BigDecimal): BigDecimal {
        var sum = BigDecimal.ZERO
        for (element in this) {
            sum = sum.add(selector(element))
        }
        return sum
    }

    private fun Iterable<SaleItem>.sumOfItems(selector: (SaleItem) -> BigDecimal): BigDecimal {
        var sum = BigDecimal.ZERO
        for (element in this) {
            sum = sum.add(selector(element))
        }
        return sum
    }
}
