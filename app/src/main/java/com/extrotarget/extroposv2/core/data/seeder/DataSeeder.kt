package com.extrotarget.extroposv2.core.data.seeder

import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.inventory.InventoryRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val inventoryRepository: InventoryRepository
) {
    suspend fun seedIfNeeded() {
        val categories = categoryRepository.getAllCategories().first()
        if (categories.isEmpty()) {
            val cat1 = Category(UUID.randomUUID().toString(), "Electronics", "Devices and gadgets")
            val cat2 = Category(UUID.randomUUID().toString(), "Groceries", "Food and essentials")
            val cat3 = Category(UUID.randomUUID().toString(), "Apparel", "Clothing and shoes")

            categoryRepository.insertCategory(cat1)
            categoryRepository.insertCategory(cat2)
            categoryRepository.insertCategory(cat3)

            val products = listOf(
                Product(UUID.randomUUID().toString(), "iPhone 15 Pro", "SKU-IPH15", "800000000001", BigDecimal("1299.99"), BigDecimal("0.16"), BigDecimal("10"), categoryId = cat1.id),
                Product(UUID.randomUUID().toString(), "MacBook Air M3", "SKU-MBA3", "800000000002", BigDecimal("1099.00"), BigDecimal("0.16"), BigDecimal("5"), categoryId = cat1.id),
                Product(UUID.randomUUID().toString(), "Milk 1L", "SKU-MILK", "800000000003", BigDecimal("2.50"), BigDecimal("0.0"), BigDecimal("50"), categoryId = cat2.id),
                Product(UUID.randomUUID().toString(), "Bread", "SKU-BREAD", "800000000004", BigDecimal("1.20"), BigDecimal("0.0"), BigDecimal("30"), categoryId = cat2.id),
                Product(UUID.randomUUID().toString(), "T-Shirt", "SKU-TSHIRT", "800000000005", BigDecimal("15.00"), BigDecimal("0.08"), BigDecimal("100"), categoryId = cat3.id),
                Product(UUID.randomUUID().toString(), "Jeans", "SKU-JEANS", "800000000006", BigDecimal("45.00"), BigDecimal("0.08"), BigDecimal("40"), categoryId = cat3.id)
            )

            products.forEach { product ->
                productRepository.insertProduct(product)
                // Add initial stock movement record
                inventoryRepository.adjustStock(product.id, product.stockQuantity, "IN", "Initial stock seeding")
            }
        }
    }
}