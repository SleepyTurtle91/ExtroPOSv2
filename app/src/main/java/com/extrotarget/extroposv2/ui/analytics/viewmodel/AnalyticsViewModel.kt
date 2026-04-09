package com.extrotarget.extroposv2.ui.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

data class AnalyticsUiState(
    val totalSales: BigDecimal = BigDecimal.ZERO,
    val totalTax: BigDecimal = BigDecimal.ZERO,
    val totalDiscount: BigDecimal = BigDecimal.ZERO,
    val totalRounding: BigDecimal = BigDecimal.ZERO,
    val salesCount: Int = 0,
    val startDate: Long = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // Last 24h
    val endDate: Long = System.currentTimeMillis()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _dateRange = MutableStateFlow(Pair(
        Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis,
        Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis
    ))

    val uiState: StateFlow<AnalyticsUiState> = _dateRange.flatMapLatest { range ->
        saleRepository.getSalesInRange(range.first, range.second).map { sales ->
            AnalyticsUiState(
                totalSales = sales.sumOf { it.totalAmount },
                totalTax = sales.sumOf { it.taxAmount },
                totalDiscount = sales.sumOf { it.discountAmount },
                totalRounding = sales.sumOf { it.roundingAdjustment },
                salesCount = sales.size,
                startDate = range.first,
                endDate = range.second
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    fun setDateRange(start: Long, end: Long) {
        _dateRange.value = Pair(start, end)
    }

    private fun Iterable<Sale>.sumOf(selector: (Sale) -> BigDecimal): BigDecimal {
        var sum = BigDecimal.ZERO
        for (element in this) {
            sum = sum.add(selector(element))
        }
        return sum
    }
}