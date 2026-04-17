package com.extrotarget.extroposv2.ui.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Modifier
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.fnb.ModifierRepository
import com.extrotarget.extroposv2.ui.inventory.InventoryUiState
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val modifierRepository: ModifierRepository
) : ViewModel() {

    val categories = categoryRepository.getAllCategories()
    val products = productRepository.getAllProducts()
    val modifiers = modifierRepository.allModifiers

    val lowStockProducts: StateFlow<List<Product>> = products
        .map { list -> list.filter { it.stockQuantity <= it.minStockLevel && it.minStockLevel > BigDecimal.ZERO } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    private val _stockMovements = MutableStateFlow<List<StockMovement>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<InventoryUiState> = combine(
        products,
        categories,
        _searchQuery,
        _selectedCategoryId,
        _selectedProduct,
        _stockMovements,
        _isLoading
    ) { params: Array<Any?> ->
        InventoryUiState(
            products = params[0] as List<Product>,
            categories = params[1] as List<Category>,
            searchQuery = params[2] as String,
            selectedCategoryId = params[3] as String?,
            selectedProduct = params[4] as Product?,
            stockMovements = params[5] as List<StockMovement>,
            isLoading = params[6] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
        if (product != null) {
            viewModelScope.launch {
                productRepository.getStockMovements(product.id).collect {
                    _stockMovements.value = it
                }
            }
        } else {
            _stockMovements.value = emptyList()
        }
    }

    fun upsertProduct(product: Product) {
        viewModelScope.launch {
            productRepository.insertProduct(product)
        }
    }

    fun adjustStock(quantity: BigDecimal, type: String, note: String?) {
        val product = _selectedProduct.value ?: return
        viewModelScope.launch {
            productRepository.adjustStock(product.id, quantity, type, note)
        }
    }

    fun setStock(quantity: BigDecimal, type: String, note: String?) {
        val product = _selectedProduct.value ?: return
        viewModelScope.launch {
            productRepository.setStock(product.id, quantity, type, note)
        }
    }

    suspend fun exportProducts(outputStream: java.io.OutputStream): Result<Int> {
        return try {
            val allProducts = productRepository.getAllProducts().first()
            outputStream.bufferedWriter().use { writer ->
                writer.write("id,name,sku,barcode,price,stockQuantity,minStockLevel,categoryId,printerTag\n")
                allProducts.forEach { p ->
                    writer.write("${p.id},\"${p.name}\",${p.sku},${p.barcode ?: ""},${p.price},${p.stockQuantity},${p.minStockLevel},${p.categoryId ?: ""},${p.printerTag ?: ""}\n")
                }
            }
            Result.success(allProducts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addCategory(name: String, description: String? = null) {
        viewModelScope.launch {
            val category = Category(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description
            )
            categoryRepository.insertCategory(category)
        }
    }

    fun addProduct(
        name: String,
        price: BigDecimal,
        categoryId: String?,
        sku: String = "",
        barcode: String? = null,
        printerTag: String = "KITCHEN",
        businessMode: String = "fnb"
    ) {
        viewModelScope.launch {
            val product = Product(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                sku = sku,
                barcode = barcode,
                price = price,
                taxRate = BigDecimal.ZERO, // Default
                stockQuantity = BigDecimal.ZERO,
                categoryId = categoryId,
                printerTag = printerTag,
                businessMode = businessMode
            )
            productRepository.insertProduct(product)
        }
    }

    fun toggleProductAvailability(product: com.extrotarget.extroposv2.core.data.model.Product) {
        viewModelScope.launch {
            productRepository.updateProduct(product.copy(isAvailable = !product.isAvailable))
        }
    }

    fun addModifier(name: String, price: BigDecimal = BigDecimal.ZERO) {
        viewModelScope.launch {
            modifierRepository.addModifier(name, price)
        }
    }

    fun toggleModifierAvailability(modifier: com.extrotarget.extroposv2.core.data.model.Modifier) {
        viewModelScope.launch {
            modifierRepository.updateModifierAvailability(modifier.id, !modifier.isAvailable)
        }
    }

    fun linkModifiersToCategory(categoryId: String, modifierIds: List<String>) {
        viewModelScope.launch {
            modifierRepository.updateCategoryModifiers(categoryId, modifierIds)
        }
    }

    fun linkModifiersToProduct(productId: String, modifierIds: List<String>) {
        viewModelScope.launch {
            modifierRepository.updateProductModifiers(productId, modifierIds)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.insertCategory(category)
        }
    }

    suspend fun getSelectedModifierIds(targetId: String, targetType: com.extrotarget.extroposv2.core.data.model.ModifierTargetType): List<String> {
        return modifierRepository.getModifierIdsForTarget(targetId, targetType)
    }
}
