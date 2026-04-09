package com.extrotarget.extroposv2.ui.fnb.kds.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KdsUiState(
    val orders: List<KdsOrder> = emptyList(),
    val selectedTag: String = "KITCHEN",
    val availableTags: List<String> = listOf("KITCHEN", "BAR", "GENERAL")
)

data class KdsOrder(
    val sale: Sale,
    val items: List<SaleItem>
)

@HiltViewModel
class KdsViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _selectedTag = MutableStateFlow("KITCHEN")
    
    val uiState: StateFlow<KdsUiState> = combine(
        saleRepository.getAllSalesWithItems(),
        _selectedTag
    ) { salesWithItems, tag ->
        val filteredOrders = salesWithItems.mapNotNull { saleWithItems ->
            val sale = saleWithItems.sale
            val items = saleWithItems.items
            // Filter items that match the current station tag and are not yet ready
            val stationItems = items.filter { 
                it.printerTag.equals(tag, ignoreCase = true) && it.status != "READY" 
            }
            
            if (stationItems.isNotEmpty() && (sale.status == "COMPLETED" || sale.status == "PENDING")) {
                KdsOrder(sale, stationItems)
            } else null
        }.sortedBy { it.sale.timestamp }

        KdsUiState(
            orders = filteredOrders,
            selectedTag = tag
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KdsUiState())

    fun selectTag(tag: String) {
        _selectedTag.value = tag
    }

    fun markOrderDone(orderId: String) {
        viewModelScope.launch {
            saleRepository.updateItemsStatusByTag(orderId, _selectedTag.value, "READY")
        }
    }
}
