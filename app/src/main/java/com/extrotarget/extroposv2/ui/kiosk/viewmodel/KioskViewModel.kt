package com.extrotarget.extroposv2.ui.kiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.network.SyncClient
import com.extrotarget.extroposv2.core.network.SyncMessageType
import com.extrotarget.extroposv2.ui.sales.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

data class KioskUiState(
    val isAttractMode: Boolean = true,
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedCategoryId: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val isProcessing: Boolean = false,
    val orderSuccess: String? = null // Holds the order number/ID
) {
    val subtotal: BigDecimal = cartItems.fold(BigDecimal.ZERO) { acc, item ->
        acc.add(item.totalPrice)
    }
}

@HiltViewModel
class KioskViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val syncClient: SyncClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(KioskUiState())
    val uiState: StateFlow<KioskUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories.filter { c -> c.isAvailable }) }
            }
        }
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _uiState.update { it.copy(products = products.filter { p -> p.isAvailable }) }
            }
        }
    }

    fun startOrdering() {
        _uiState.update { it.copy(isAttractMode = false, cartItems = emptyList(), orderSuccess = null) }
    }

    fun selectCategory(id: String?) {
        _uiState.update { it.copy(selectedCategoryId = id) }
    }

    fun addToCart(product: Product) {
        _uiState.update { state ->
            val existing = state.cartItems.find { it.product.id == product.id }
            val updated = if (existing != null) {
                state.cartItems.map { 
                    if (it.id == existing.id) it.copy(quantity = it.quantity.add(BigDecimal.ONE)) else it 
                }
            } else {
                state.cartItems + CartItem(product = product, quantity = BigDecimal.ONE, unitPrice = product.price, taxRate = product.taxRate)
            }
            state.copy(cartItems = updated)
        }
    }

    fun updateQuantity(item: CartItem, delta: BigDecimal) {
        _uiState.update { state ->
            val updated = state.cartItems.mapNotNull {
                if (it.id == item.id) {
                    val newQty = it.quantity.add(delta)
                    if (newQty <= BigDecimal.ZERO) null else it.copy(quantity = newQty)
                } else it
            }
            state.copy(cartItems = updated)
        }
    }

    fun checkout() {
        if (_uiState.value.cartItems.isEmpty()) return
        
        _uiState.update { it.copy(isProcessing = true) }
        
        viewModelScope.launch {
            try {
                val orderId = UUID.randomUUID().toString().takeLast(4).uppercase()
                // In a real implementation, we'd send a full Sale object, 
                // but for the P2P prototype, we broadcast a specialized kiosk message.
                syncClient.sendRealtimeMessage(
                    SyncMessageType.KIOSK_NEW_ORDER,
                    mapOf(
                        "orderId" to orderId,
                        "items" to _uiState.value.cartItems,
                        "total" to _uiState.value.subtotal
                    )
                )
                
                _uiState.update { it.copy(isProcessing = false, orderSuccess = orderId) }
                
                // Return to attract mode after 10 seconds
                kotlinx.coroutines.delay(10000)
                _uiState.update { it.copy(isAttractMode = true, orderSuccess = null, cartItems = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }
    
    fun cancelOrder() {
        _uiState.update { it.copy(isAttractMode = true, cartItems = emptyList()) }
    }
}
