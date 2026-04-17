package com.extrotarget.extroposv2.core.data.repository.fnb

import com.extrotarget.extroposv2.core.data.local.dao.ModifierDao
import com.extrotarget.extroposv2.core.data.model.Modifier
import com.extrotarget.extroposv2.core.data.model.ModifierTargetType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModifierRepository @Inject constructor(
    private val modifierDao: ModifierDao
) {
    val allModifiers: Flow<List<Modifier>> = modifierDao.getAllModifiers()

    suspend fun addModifier(name: String, price: java.math.BigDecimal = java.math.BigDecimal.ZERO) {
        val modifier = Modifier(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            priceAdjustment = price
        )
        modifierDao.insertModifier(modifier)
    }

    /**
     * Logic for Modifier Bypass:
     * Check PRODUCT first, if empty, check CATEGORY.
     */
    suspend fun getModifiersForProduct(productId: String, categoryId: String?): List<Modifier> {
        // 1. Try to get modifiers linked directly to the product
        val productModifiers = modifierDao.getModifiersForTarget(productId, ModifierTargetType.PRODUCT)
        if (productModifiers.isNotEmpty()) return productModifiers

        // 2. If no product-level modifiers, fallback to category-level
        if (categoryId != null) {
            return modifierDao.getModifiersForTarget(categoryId, ModifierTargetType.CATEGORY)
        }

        return emptyList()
    }

    suspend fun updateProductModifiers(productId: String, modifierIds: List<String>) {
        modifierDao.updateModifiersForTarget(productId, ModifierTargetType.PRODUCT, modifierIds)
    }

    suspend fun updateCategoryModifiers(categoryId: String, modifierIds: List<String>) {
        modifierDao.updateModifiersForTarget(categoryId, ModifierTargetType.CATEGORY, modifierIds)
    }

    suspend fun updateModifierAvailability(modifierId: String, isAvailable: Boolean) {
        modifierDao.updateModifierAvailability(modifierId, isAvailable)
    }

    suspend fun getModifierIdsForTarget(targetId: String, targetType: ModifierTargetType): List<String> {
        return modifierDao.getModifiersForTarget(targetId, targetType).map { it.id }
    }
}
