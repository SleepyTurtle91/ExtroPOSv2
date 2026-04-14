package com.extrotarget.extroposv2.ui.sales

import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.util.RoundingUtils
import java.math.BigDecimal

import java.util.UUID

data class SalesUiState(
    val activeMode: BusinessMode = BusinessMode.RETAIL,
    val products: List<Product> = emptyList(),
    val categories: List<com.extrotarget.extroposv2.core.data.model.Category> = emptyList(),
    val staffList: List<com.extrotarget.extroposv2.core.data.model.carwash.Staff> = emptyList(),
    val selectedCategoryId: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val searchQuery: String = "",
    val isCheckingOut: Boolean = false,
    val showStaffSelection: Boolean = false,
    val itemAwaitingStaff: CartItem? = null,
    val showWeightInput: Boolean = false,
    val productAwaitingWeight: Product? = null,
    val showPaymentSuccess: Boolean = false,
    val lastSaleQrContent: String? = null,
    val lastSaleId: String? = null,
    val selectedTable: com.extrotarget.extroposv2.core.data.model.fnb.Table? = null,
    val itemAwaitingModifiers: CartItem? = null,
    val availableModifiers: List<String> = listOf("Bungkus", "Ikat Tepi", "Kurang Manis", "Tambah Pedas", "No Veggie"),
    val cartDiscount: Discount? = null,
    val showDiscountDialog: Boolean = false,
    val itemAwaitingDiscount: CartItem? = null,
    val showTerminalProgress: Boolean = false,
    val terminalStatus: String? = null,
    val showAdminAuthDialog: Boolean = false,
    val adminAuthAction: AdminAuthAction? = null,
    val adminAuthError: String? = null,
    val showSettingsModal: Boolean = false,
    val showConfirmClearCart: Boolean = false,
    val isLocked: Boolean = true,
    val activeTab: String = "pos",
    val tables: List<com.extrotarget.extroposv2.core.data.model.fnb.Table> = emptyList(),
    val syncStatus: com.extrotarget.extroposv2.core.network.SyncStatus = com.extrotarget.extroposv2.core.network.SyncStatus.IDLE
) {
    val filteredProducts: List<Product> = products.filter { product ->
        (product.businessMode == null || product.businessMode == activeMode.id) &&
        (searchQuery.isEmpty() || product.name.contains(searchQuery, ignoreCase = true)) &&
        (selectedCategoryId == null || product.categoryId == selectedCategoryId)
    }

    val subtotal: BigDecimal = cartItems.fold(BigDecimal.ZERO) { acc, item ->
        acc.add(item.totalBeforeDiscount)
    }

    val itemDiscounts: BigDecimal = cartItems.fold(BigDecimal.ZERO) { acc, item ->
        acc.add(item.discountAmount)
    }

    val cartDiscountAmount: BigDecimal = cartDiscount?.calculateDiscount(subtotal.subtract(itemDiscounts)) ?: BigDecimal.ZERO

    val totalDiscount: BigDecimal = itemDiscounts.add(cartDiscountAmount)
    
    val totalTax: BigDecimal = cartItems.fold(BigDecimal.ZERO) { acc, item ->
        acc.add(item.taxAmount)
    }

    val amountBeforeRounding: BigDecimal = subtotal.subtract(totalDiscount).add(totalTax)
    
    private val roundingResult = RoundingUtils.calculateBNMRounding(amountBeforeRounding)
    
    val roundingAdjustment: BigDecimal = roundingResult.adjustment
    val totalAmount: BigDecimal = amountBeforeRounding
    val totalAmountCash: BigDecimal = roundingResult.finalTotal
}

data class Discount(
    val type: DiscountType,
    val value: BigDecimal,
    val label: String? = null
) {
    fun calculateDiscount(baseAmount: BigDecimal): BigDecimal {
        return when (type) {
            DiscountType.FIXED -> value
            DiscountType.PERCENTAGE -> baseAmount.multiply(value)
                .divide(BigDecimal("100"), 2, java.math.RoundingMode.HALF_EVEN)
        }
    }
}

enum class DiscountType {
    FIXED, PERCENTAGE
}

sealed class AdminAuthAction {
    data class RemoveItem(val item: CartItem) : AdminAuthAction()
    data class ApplyDiscount(val item: CartItem?, val discount: Discount?) : AdminAuthAction()
}

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val product: Product,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal,
    val assignedStaffId: String? = null,
    val assignedStaffName: String? = null,
    val modifiers: List<String> = emptyList(),
    val discount: Discount? = null,
    val isSentToKitchen: Boolean = false
) {
    val totalBeforeDiscount: BigDecimal = unitPrice.multiply(quantity)
    
    val discountAmount: BigDecimal = discount?.calculateDiscount(totalBeforeDiscount) ?: BigDecimal.ZERO

    val totalPrice: BigDecimal = totalBeforeDiscount.subtract(discountAmount)

    val taxAmount: BigDecimal = totalPrice.multiply(taxRate)
        .divide(BigDecimal("100"), 2, java.math.RoundingMode.HALF_EVEN)
}
