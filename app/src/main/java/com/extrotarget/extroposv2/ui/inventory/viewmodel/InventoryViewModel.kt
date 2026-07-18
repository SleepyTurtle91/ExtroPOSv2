package com.extrotarget.extroposv2.ui.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Modifier
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.fnb.ModifierRepository
import com.extrotarget.extroposv2.core.util.audit.AuditManager
import com.extrotarget.extroposv2.ui.inventory.InventoryUiState
import com.extrotarget.extroposv2.ui.inventory.PendingAdjustment
import com.extrotarget.extroposv2.core.security.Permission
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import com.extrotarget.extroposv2.core.domain.commerce.StockMovement
import com.extrotarget.extroposv2.core.domain.commerce.StockMovementType
import com.extrotarget.extroposv2.core.auth.SessionManager
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
    private val modifierRepository: ModifierRepository,
    private val sessionManager: SessionManager,
    private val auditManager: AuditManager,
    private val staffRepository: StaffRepository
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
    private val _showAdminAuthDialog = MutableStateFlow(false)
    private val _adminAuthError = MutableStateFlow<String?>(null)
    private val _pendingAdjustment = MutableStateFlow<PendingAdjustment?>(null)

    val uiState: StateFlow<InventoryUiState> = combine(
        products,
        categories,
        _searchQuery,
        _selectedCategoryId,
        _selectedProduct,
        _stockMovements,
        _isLoading,
        _showAdminAuthDialog,
        _adminAuthError,
        _pendingAdjustment
    ) { params: Array<Any?> ->
        InventoryUiState(
            products = params[0] as List<Product>,
            categories = params[1] as List<Category>,
            searchQuery = params[2] as String,
            selectedCategoryId = params[3] as String?,
            selectedProduct = params[4] as Product?,
            stockMovements = params[5] as List<StockMovement>,
            isLoading = params[6] as Boolean,
            showAdminAuthDialog = params[7] as Boolean,
            adminAuthError = params[8] as String?,
            pendingAdjustment = params[9] as PendingAdjustment?
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

    fun adjustStock(quantity: BigDecimal, type: StockMovementType, reason: String) {
        val product = _selectedProduct.value ?: return
        
        // Threshold check for Manager approval
        if (quantity.abs() > BigDecimal("50") && !staffRepository.isCurrentUserAdmin()) {
            _isLoading.value = false // Ensure we're not stuck in loading if we were
            _selectedProduct.value = product // Re-select to keep dialog context if needed
            // Instead of immediate execution, we trigger auth
            updateUiStateWithPendingAdjustment(quantity, type, reason)
            return
        }
        
        executeAdjustStock(product, quantity, type, reason)
    }

    private fun updateUiStateWithPendingAdjustment(quantity: BigDecimal, type: StockMovementType, reason: String) {
        _pendingAdjustment.value = PendingAdjustment(quantity, type, reason)
        _showAdminAuthDialog.value = true
    }

    fun authenticateAdmin(pin: String) {
        viewModelScope.launch {
            val admin = staffRepository.getStaffByPin(pin)
            if (admin != null && (admin.role == "ADMIN" || admin.role == "SUPERVISOR")) {
                val pending = _pendingAdjustment.value
                val product = _selectedProduct.value
                if (pending != null && product != null) {
                    executeAdjustStock(product, pending.quantity, pending.type, pending.reason)
                }
                _showAdminAuthDialog.value = false
                _pendingAdjustment.value = null
                _adminAuthError.value = null
            } else {
                _adminAuthError.value = "Invalid Admin PIN"
            }
        }
    }

    fun dismissAdminAuth() {
        _showAdminAuthDialog.value = false
        _pendingAdjustment.value = null
        _adminAuthError.value = null
    }

    private fun executeAdjustStock(product: Product, quantity: BigDecimal, type: StockMovementType, reason: String) {
        val staffId = sessionManager.getCurrentStaff()?.id ?: "SYSTEM"
        viewModelScope.launch {
            productRepository.adjustStock(product.id, quantity, type, reason, staffId)
            
            auditManager.logAction(
                action = "STOCK_ADJUSTMENT",
                details = "Adjusted ${product.name} by $quantity ($type)",
                module = "INVENTORY",
                oldValue = product.stockQuantity.toString(),
                newValue = product.stockQuantity.add(quantity).toString(),
                entityType = "PRODUCT",
                entityId = product.id
            )
            _selectedProduct.value = null // Close dialog
        }
    }

    fun setStock(quantity: BigDecimal, note: String?) {
        val product = _selectedProduct.value ?: return
        val staffId = sessionManager.getCurrentStaff()?.id ?: "SYSTEM"
        viewModelScope.launch {
            productRepository.setStock(product.id, quantity, note, staffId)
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
