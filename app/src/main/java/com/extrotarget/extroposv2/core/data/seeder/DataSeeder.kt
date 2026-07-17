package com.extrotarget.extroposv2.core.data.seeder

import androidx.room.withTransaction
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.inventory.InventoryRepository
import com.extrotarget.extroposv2.core.domain.commerce.StockMovementType
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val database: AppDatabase,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val inventoryRepository: InventoryRepository,
    private val staffRepository: com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository,
    private val taxRepository: com.extrotarget.extroposv2.core.data.repository.settings.TaxRepository
) {
    suspend fun seedEssentialData(
        adminName: String? = null,
        adminUsername: String? = null,
        adminPin: String? = null
    ) {
        val activeStaff = staffRepository.getAllActiveStaff().first()
        if (activeStaff.isEmpty()) {
            val admin = com.extrotarget.extroposv2.core.data.model.carwash.Staff(
                id = "admin-fixed-id",
                name = adminName ?: "Administrator",
                phone = adminUsername ?: "admin",
                role = "ADMIN",
                pin = adminPin ?: "0000",
                isActive = true
            )
            staffRepository.saveStaff(admin)
        }

        val taxConfig = taxRepository.getTaxConfig().firstOrNull()
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
        
        // Seed default receipt if missing
        val receiptConfig = database.receiptDao().getReceiptConfig().firstOrNull()
        if (receiptConfig == null) {
            database.receiptDao().saveReceiptConfig(
                com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig(
                    id = "default_receipt",
                    storeName = "ExtroPOS v2",
                    headerMessage = "Welcome",
                    footerMessage = "Thank you for your business!",
                    showTaxSummary = true
                )
            )
        }
    }

    suspend fun seedDemoData(mode: BusinessMode) {
        val catMap = mutableMapOf<String, Category>()
        
        // 1. Seed Categories
        val modeCats = when (mode) {
            BusinessMode.RETAIL -> listOf("Drinks", "Bakery", "Tech")
            BusinessMode.FNB -> listOf("Main Dishes", "Drinks", "Desserts")
            BusinessMode.CARWASH -> listOf("Wash Services", "Add-ons", "Premium Detail")
            BusinessMode.LAUNDRY -> listOf("Weight Based", "Dry Clean")
            BusinessMode.HOTEL, BusinessMode.HOMESTAY -> listOf("Room Addons", "Transport", "Meal Vouchers")
            else -> emptyList()
        }

        modeCats.forEach { name ->
            val cat = Category(UUID.randomUUID().toString(), name, "Default $name")
            categoryRepository.insertCategory(cat)
            catMap[name] = cat
        }

        // 2. Seed Products
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
            BusinessMode.HOTEL, BusinessMode.HOMESTAY -> listOf(
                createSeedProduct("Extra Bed", "50.00", catMap["Room Addons"], mode),
                createSeedProduct("Airport Transfer", "80.00", catMap["Transport"], mode),
                createSeedProduct("Breakfast Voucher", "25.00", catMap["Meal Vouchers"], mode)
            )
            else -> emptyList()
        }

        products.forEach { product ->
            productRepository.insertProduct(product)
            inventoryRepository.adjustStock(product.id, BigDecimal("100"), StockMovementType.RESTOCK, "Initial template seeding")
        }

        // 3. Seed Tables (FNB only)
        if (mode == BusinessMode.FNB) {
            listOf("T1", "T2", "T3", "T4", "T5").forEachIndexed { index, name ->
                database.tableDao().insertTable(
                    Table(
                        id = UUID.randomUUID().toString(),
                        code = name,
                        name = "Table ${index + 1}",
                        capacity = 4,
                        status = TableStatus.AVAILABLE,
                        zone = "Indoor",
                        displayOrder = index
                    )
                )
            }
        }

        // 4. Seed Members
        listOf(
            Member(UUID.randomUUID().toString(), "Ahmad Ali", "0123456789", "ahmad@example.com"),
            Member(UUID.randomUUID().toString(), "Tan Ah Kao", "0112233445", "tan@example.com"),
            Member(UUID.randomUUID().toString(), "Muthu", "0177889900", "muthu@example.com")
        ).forEach { database.loyaltyDao().insertMember(it) }
    }

    suspend fun clearBusinessData() {
        database.productDao().deleteAll()
        database.categoryDao().deleteAll()
        database.modifierDao().clearAllModifiers()
        database.tableDao().deleteAll()
        database.saleDao().clearAllBusinessData()
        database.loyaltyDao().clearLoyaltyData()
        database.carWashDao().deleteAll()
        database.laundryDao().deleteAll()
        database.hotelDao().clearAllHotelData()
    }

    suspend fun restoreDemoDatabase(mode: BusinessMode) {
        database.withTransaction {
            clearBusinessData()
            seedEssentialData()
            seedDemoData(mode)
        }
    }

    suspend fun seedIfNeeded() {
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
