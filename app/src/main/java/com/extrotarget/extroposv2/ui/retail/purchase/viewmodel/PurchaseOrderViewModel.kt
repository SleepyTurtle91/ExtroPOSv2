package com.extrotarget.extroposv2.ui.retail.purchase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.repository.retail.PurchaseRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.domain.retail.model.PurchaseOrder
import com.extrotarget.extroposv2.domain.retail.model.PurchaseOrderItem
import com.extrotarget.extroposv2.domain.retail.model.Supplier
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import com.extrotarget.extroposv2.core.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

data class PurchaseOrderUiState(
    val orders: List<PurchaseOrder> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class PurchaseOrderViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val productRepository: ProductRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: StateFlow<PurchaseOrderUiState> = combine(
        purchaseRepository.getAllPurchaseOrders(),
        purchaseRepository.getAllSuppliers(),
        productRepository.getAllProducts()
    ) { orders, suppliers, products ->
        PurchaseOrderUiState(
            orders = orders,
            suppliers = suppliers,
            allProducts = products
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PurchaseOrderUiState())

    fun createOrder(supplierId: String, poNumber: String, items: List<PurchaseOrderItem>) {
        val staffId = sessionManager.getCurrentStaff()?.id ?: "SYSTEM"
        viewModelScope.launch {
            val po = PurchaseOrder(
                id = UUID.randomUUID().toString(),
                poNumber = poNumber,
                supplierId = supplierId,
                createdBy = staffId,
                totalAmount = items.sumOf { it.unitCost.multiply(it.quantity) }
            )
            purchaseRepository.createPurchaseOrder(po, items)
        }
    }

    fun submitOrder(poId: String) {
        viewModelScope.launch {
            purchaseRepository.updateStatus(poId, WorkflowStatus.SUBMITTED)
        }
    }

    fun approveOrder(poId: String) {
        viewModelScope.launch {
            purchaseRepository.updateStatus(poId, WorkflowStatus.APPROVED)
        }
    }

    fun receiveOrder(poId: String, receivedProducts: List<Pair<String, BigDecimal>>) {
        val staffId = sessionManager.getCurrentStaff()?.id ?: "SYSTEM"
        viewModelScope.launch {
            purchaseRepository.receivePurchaseOrder(poId, receivedProducts, staffId)
        }
    }
}
