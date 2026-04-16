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
    private val staffRepository: com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository,
    private val taxRepository: com.extrotarget.extroposv2.core.data.repository.settings.TaxRepository
) {
    suspend fun seedForMode(
        mode: BusinessMode,
        adminName: String? = null,
        adminUsername: String? = null,
        adminPin: String? = null
    ) {
        seedEssentialData(adminName, adminUsername, adminPin)
        seedModeSpecificData(mode)
    }

    private suspend fun seedEssentialData(
        adminName: String? = null,
        adminUsername: String? = null,
        adminPin: String? = null
    ) {
        val activeStaff = staffRepository.getAllActiveStaff().first()
        if (activeStaff.isEmpty()) {
            val admin = com.extrotarget.extroposv2.core.data.model.carwash.Staff(
                id = "admin-fixed-id",
                name = adminName ?: "Administrator",
                phone = adminUsername ?: "admin", // Using username as phone for now if needed, or update model
                role = "ADMIN",
                pin = adminPin ?: "0000",
                isActive = true
            )
            staffRepository.saveStaff(admin)
        } else if (adminPin != null) {
            // If staff exists (e.g. from partial previous seed), update the admin PIN
            val admin = staffRepository.getStaffById("admin-fixed-id")
            if (admin != null) {
                staffRepository.saveStaff(admin.copy(
                    name = adminName ?: admin.name,
                    phone = adminUsername ?: admin.phone,
                    pin = adminPin
                ))
            }
        }

        val taxConfig = taxRepository.getTaxConfig().first()
        if (taxConfig == null) {
            taxRepository.updateTaxConfig(
                com.extrotarget.extroposv2.core.data.model.settings.TaxConfig(
                    id = "default_tax",
                    defaultTaxRate = BigDecimal("0.00"),
                    isTaxEnabled = false,
                    taxName = "Tax"
                )
            )
        }
    }

    private suspend fun seedModeSpecificData(mode: BusinessMode) {
        val categories = categoryRepository.getAllCategories().first()
        // If already has categories, assume seeded or user-configured
        if (categories.isNotEmpty()) return

        val modeCats = when (mode) {
            BusinessMode.RETAIL -> listOf("Drinks", "Bakery", "Tech")
            BusinessMode.FNB -> listOf("Main Dishes", "Drinks", "Desserts")
            BusinessMode.CARWASH -> listOf("Wash Services", "Add-ons", "Premium Detail")
            BusinessMode.LAUNDRY -> listOf("Weight Based", "Dry Clean")
        }

        val catMap = mutableMapOf<String, Category>()
        modeCats.forEach { name ->
            val cat = Category(UUID.randomUUID().toString(), name, "Default $name")
            categoryRepository.insertCategory(cat)
            catMap[name] = cat
        }

        val products = when (mode) {
            BusinessMode.RETAIL -> listOf(
                createSeedProduct("Mineral Water 500ml", "1.50", catMap["Drinks"], BusinessMode.RETAIL),
                createSeedProduct("Gardenia Bread", "3.50", catMap["Bakery"], BusinessMode.RETAIL),
                createSeedProduct("USB Cable", "15.00", catMap["Tech"], BusinessMode.RETAIL)
            )
            BusinessMode.FNB -> listOf(
                createSeedProduct("Nasi Lemak Ayam", "12.90", catMap["Main Dishes"], BusinessMode.FNB),
                createSeedProduct("Teh Tarik", "2.80", catMap["Drinks"], BusinessMode.FNB),
                createSeedProduct("Chocolate Cake", "8.50", catMap["Desserts"], BusinessMode.FNB)
            )
            BusinessMode.CARWASH -> listOf(
                createSeedProduct("Basic Wash", "15.00", catMap["Wash Services"], BusinessMode.CARWASH),
                createSeedProduct("Vacuum", "5.00", catMap["Add-ons"], BusinessMode.CARWASH),
                createSeedProduct("Nano Coating", "150.00", catMap["Premium Detail"], BusinessMode.CARWASH)
            )
            BusinessMode.LAUNDRY -> listOf(
                createSeedProduct("Wash & Fold", "4.50", catMap["Weight Based"], BusinessMode.LAUNDRY, isWeight = true),
                createSeedProduct("Suit Dry Clean", "25.00", catMap["Dry Clean"], BusinessMode.LAUNDRY)
            )
        }

        products.forEach { product ->
            productRepository.insertProduct(product)
            inventoryRepository.adjustStock(product.id, BigDecimal("100"), "IN", "Initial template seeding")
        }
    }

    suspend fun seedIfNeeded() {
        // Essential only
        seedEssentialData()
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
            taxRate = BigDecimal("0.00"),
            stockQuantity = BigDecimal("100"),
            categoryId = category?.id,
            businessMode = mode.id,
            isWeightBased = isWeight
        )
    }
}
