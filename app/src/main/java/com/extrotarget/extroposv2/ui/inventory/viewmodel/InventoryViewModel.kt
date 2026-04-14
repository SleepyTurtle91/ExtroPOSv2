package com.extrotarget.extroposv2.ui.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.inventory.InventoryRepository
import com.extrotarget.extroposv2.core.util.exporter.ProductExportManager
import com.extrotarget.extroposv2.ui.inventory.InventoryUiState
import java.io.OutputStream
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val inventoryRepository: InventoryRepository,
    private val exportManager: ProductExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    val lowStockProducts: StateFlow<List<Product>> = productRepository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                productRepository.getAllProducts(),
                categoryRepository.getAllCategories()
            ) { products, categories ->
                _uiState.update { 
                    it.copy(
                        products = products,
                        categories = categories
                    )
                }
            }.collect()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelect(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun upsertProduct(product: Product) {
        viewModelScope.launch {
            productRepository.insertProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    fun selectProduct(product: Product?) {
        _uiState.update { it.copy(selectedProduct = product) }
        if (product != null) {
            viewModelScope.launch {
                inventoryRepository.getMovementsForProduct(product.id).collect { movements ->
                    _uiState.update { it.copy(stockMovements = movements) }
                }
            }
        }
    }

    fun adjustStock(quantity: BigDecimal, type: String, note: String? = null) {
        val product = uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            inventoryRepository.adjustStock(product.id, quantity, type, note)
        }
    }

    fun setStock(quantity: BigDecimal, type: String, note: String? = null) {
        val product = uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            inventoryRepository.setStock(product.id, quantity, type, note)
        }
    }

    suspend fun exportProducts(outputStream: OutputStream): Result<Int> {
        return exportManager.exportToCsv(outputStream)
    }
}