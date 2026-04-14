package com.extrotarget.extroposv2.core.data.seeder

import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.inventory.InventoryRepository
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val inventoryRepository: InventoryRepository,
    private val staffRepository: com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
) {
    suspend fun seedIfNeeded() {
        val activeStaff = staffRepository.getAllActiveStaff().first()
        if (activeStaff.isEmpty()) {
            val staffMembers = listOf(
                Pair("Admin", "123456"),
                Pair("Ali", "111111"),
                Pair("Abu", "222222"),
                Pair("Chong", "333333"),
                Pair("Muthu", "444444"),
                Pair("Siti", "555555")
            )

            staffMembers.forEach { (name, pin) ->
                staffRepository.saveStaff(
                    com.extrotarget.extroposv2.core.data.model.carwash.Staff(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        phone = "012345678${staffMembers.indexOf(Pair(name, pin))}",
                        role = if (name == "Admin") "ADMIN" else "CASHIER",
                        pin = pin,
                        isActive = true
                    )
                )
            }
        }

        val categories = categoryRepository.getAllCategories().first()
        if (categories.isEmpty()) {
            val retailCats = listOf("Drinks", "Bakery", "Tech")
            val fnbCats = listOf("Main", "Drinks")
            val carwashCats = listOf("Wash", "Service", "Premium")
            val laundryCats = listOf("Weight Based", "Unit Based")

            val allCats = (retailCats + fnbCats + carwashCats + laundryCats).distinct()
            val catMap = mutableMapOf<String, Category>()

            allCats.forEach { name ->
                val cat = Category(UUID.randomUUID().toString(), name, "Category for $name")
                categoryRepository.insertCategory(cat)
                catMap[name] = cat
            }

            // Products Seeding from Template
            val seedProducts = mutableListOf<Product>()

            // Retail
            seedProducts.add(createSeedProduct("Mineral Water 500ml", "1.50", catMap["Drinks"], BusinessMode.RETAIL))
            seedProducts.add(createSeedProduct("Gardenia White Bread", "3.20", catMap["Bakery"], BusinessMode.RETAIL))
            seedProducts.add(createSeedProduct("Powerbank 10k mAh", "89.00", catMap["Tech"], BusinessMode.RETAIL))
            seedProducts.add(createSeedProduct("USB-C Cable 1m", "15.90", catMap["Tech"], BusinessMode.RETAIL))

            // F&B
            seedProducts.add(createSeedProduct("Nasi Lemak Ayam Goreng", "12.90", catMap["Main"], BusinessMode.FNB))
            seedProducts.add(createSeedProduct("Teh Tarik (Kaw)", "2.80", catMap["Drinks"], BusinessMode.FNB))
            seedProducts.add(createSeedProduct("Roti Canai Plain", "1.50", catMap["Main"], BusinessMode.FNB))
            seedProducts.add(createSeedProduct("Kopi O Ais", "2.50", catMap["Drinks"], BusinessMode.FNB))

            // Car Wash
            seedProducts.add(createSeedProduct("Basic Wash (Sedan)", "15.00", catMap["Wash"], BusinessMode.CARWASH))
            seedProducts.add(createSeedProduct("Interior Vacuum", "10.00", catMap["Service"], BusinessMode.CARWASH))
            seedProducts.add(createSeedProduct("Nano Mist Treatment", "45.00", catMap["Premium"], BusinessMode.CARWASH))

            // Laundry
            seedProducts.add(createSeedProduct("Wash & Fold (per kg)", "4.50", catMap["Weight Based"], BusinessMode.LAUNDRY, isWeight = true))
            seedProducts.add(createSeedProduct("Comforter / Blanket", "15.00", catMap["Unit Based"], BusinessMode.LAUNDRY))

            seedProducts.forEach { product ->
                productRepository.insertProduct(product)
                inventoryRepository.adjustStock(product.id, product.stockQuantity, "IN", "Initial template seeding")
            }
        }
    }

    private fun createSeedProduct(
        name: String, 
        price: String, 
        category: Category?, 
        mode: BusinessMode,
        isWeight: Boolean = false
    ): Product {
        return Product(
            id = UUID.randomUUID().toString(),
            name = name,
            sku = "SKU-${name.take(5).uppercase()}-${UUID.randomUUID().toString().take(4)}",
            barcode = "8000${UUID.randomUUID().toString().filter { it.isDigit() }.take(8)}",
            price = BigDecimal(price),
            taxRate = BigDecimal("0.06"),
            stockQuantity = BigDecimal("100"),
            categoryId = category?.id,
            businessMode = mode.id,
            isWeightBased = isWeight
        )
    }
}
