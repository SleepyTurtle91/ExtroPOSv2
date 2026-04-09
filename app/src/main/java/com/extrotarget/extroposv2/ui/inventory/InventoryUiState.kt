package com.extrotarget.extroposv2.ui.inventory

import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import java.math.BigDecimal

data class InventoryUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedProduct: Product? = null,
    val stockMovements: List<StockMovement> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val isAdjustingStock: Boolean = false
) {
    val filteredProducts: List<Product> = products.filter { product ->
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                product.sku.contains(searchQuery, ignoreCase = true) ||
                (product.barcode?.contains(searchQuery, ignoreCase = true) ?: false)
        val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
        matchesSearch && matchesCategory
    }
}