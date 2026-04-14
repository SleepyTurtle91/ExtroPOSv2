package com.extrotarget.extroposv2.core.domain.usecase

import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.sales.Discount
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class CartUseCase @Inject constructor() {

    fun addItem(cartItems: List<CartItem>, product: Product, assignedStaffId: String? = null, assignedStaffName: String? = null): List<CartItem> {
        val existingItem = cartItems.find { 
            it.product.id == product.id && 
            it.assignedStaffId == assignedStaffId && 
            !it.product.isWeightBased &&
            it.modifiers.isEmpty() &&
            it.discount == null
        }

        return if (existingItem != null) {
            cartItems.map {
                if (it.id == existingItem.id) {
                    it.copy(quantity = it.quantity.add(BigDecimal.ONE))
                } else it
            }
        } else {
            cartItems + CartItem(
                product = product,
                quantity = BigDecimal.ONE,
                unitPrice = product.price,
                taxRate = product.taxRate,
                assignedStaffId = assignedStaffId,
                assignedStaffName = assignedStaffName,
                isSentToKitchen = false
            )
        }
    }

    fun addWeightBasedItem(cartItems: List<CartItem>, product: Product, weight: BigDecimal): List<CartItem> {
        return cartItems + CartItem(
            product = product,
            quantity = weight,
            unitPrice = product.price,
            taxRate = product.taxRate,
            isSentToKitchen = false
        )
    }

    fun removeItem(cartItems: List<CartItem>, itemToRemove: CartItem): List<CartItem> {
        return cartItems.filter { it.id != itemToRemove.id }
    }

    fun updateQuantity(cartItems: List<CartItem>, itemToUpdate: CartItem, newQuantity: BigDecimal): List<CartItem> {
        if (newQuantity <= BigDecimal.ZERO) return removeItem(cartItems, itemToUpdate)
        return cartItems.map {
            if (it.id == itemToUpdate.id) {
                it.copy(quantity = newQuantity)
            } else it
        }
    }

    fun toggleModifier(cartItems: List<CartItem>, itemToUpdate: CartItem, modifier: String): List<CartItem> {
        return cartItems.map {
            if (it.id == itemToUpdate.id) {
                val currentModifiers = it.modifiers.toMutableList()
                if (currentModifiers.contains(modifier)) {
                    currentModifiers.remove(modifier)
                } else {
                    currentModifiers.add(modifier)
                }
                it.copy(modifiers = currentModifiers)
            } else it
        }
    }

    fun applyItemDiscount(cartItems: List<CartItem>, itemToUpdate: CartItem, discount: Discount?): List<CartItem> {
        return cartItems.map {
            if (it.id == itemToUpdate.id) {
                it.copy(discount = discount)
            } else it
        }
    }
}
