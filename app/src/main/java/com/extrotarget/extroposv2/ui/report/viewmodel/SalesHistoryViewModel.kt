package com.extrotarget.extroposv2.ui.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.domain.usecase.PrintReceiptUseCase
import com.extrotarget.extroposv2.core.domain.usecase.VoidSaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SalesHistoryUiState(
    val sales: List<SaleWithItems> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val startDate: Long = 0,
    val endDate: Long = 0,
    val error: String? = null
)

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val voidSaleUseCase: VoidSaleUseCase,
    private val printReceiptUseCase: PrintReceiptUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow(getDefaultDateRange())

    init {
        // Initialize with today's range
        val range = getDefaultDateRange()
        _dateRange.value = range
    }

    private fun getDefaultDateRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return start to end
    }

    val uiState: StateFlow<SalesHistoryUiState> = combine(
        saleRepository.getAllSalesWithItems(),
        _searchQuery,
        _dateRange
    ) { sales, query, range ->
        val filtered = sales.filter { item ->
            val matchesQuery = query.isEmpty() || 
                item.sale.id.contains(query, ignoreCase = true) ||
                item.items.any { it.productName.contains(query, ignoreCase = true) }
            
            val matchesDate = item.sale.timestamp >= range.first && item.sale.timestamp <= range.second
            
            matchesQuery && matchesDate
        }

        SalesHistoryUiState(
            sales = filtered,
            searchQuery = query,
            startDate = range.first,
            endDate = range.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalesHistoryUiState(isLoading = true))

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateRange(start: Long, end: Long) {
        _dateRange.value = start to end
    }

    fun voidSale(saleId: String, reason: String) {
        viewModelScope.launch {
            try {
                voidSaleUseCase(saleId, reason)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun reprintReceipt(saleWithItems: SaleWithItems) {
        viewModelScope.launch {
            printReceiptUseCase(saleWithItems.sale, saleWithItems.items)
        }
    }
}
