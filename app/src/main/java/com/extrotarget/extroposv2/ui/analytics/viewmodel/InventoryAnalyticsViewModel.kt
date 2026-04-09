package com.extrotarget.extroposv2.ui.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val lowStockProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class InventoryAnalyticsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    val uiState: StateFlow<InventoryUiState> = productRepository.getAllProducts()
        .map { products ->
            InventoryUiState(
                lowStockProducts = products.filter { it.stockQuantity <= it.minStockLevel }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState(isLoading = true))
}
