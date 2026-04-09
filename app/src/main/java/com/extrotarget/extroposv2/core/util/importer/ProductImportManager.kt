package com.extrotarget.extroposv2.core.util.importer

import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import androidx.room.withTransaction
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductImportManager @Inject constructor(
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao,
    private val db: AppDatabase
) {
    suspend fun importFromCsv(inputStream: InputStream): Result<Int> {
        return try {
            val products = mutableListOf<Product>()
            val movements = mutableListOf<StockMovement>()
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header
            val header = reader.readLine()
            
            var line: String? = reader.readLine()
            while (line != null) {
                val parts = line.split(",").map { it.trim() }
                if (parts.size >= 5) { // Name, SKU, Barcode, Price, TaxRate are minimum
                    val name = parts.getOrNull(0) ?: ""
                    val sku = parts.getOrNull(1) ?: ""
                    val barcode = parts.getOrNull(2).takeIf { it?.isNotEmpty() == true }
                    val price = parts.getOrNull(3)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val taxRate = parts.getOrNull(4)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val stock = parts.getOrNull(5)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val minStock = parts.getOrNull(6)?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val categoryId = parts.getOrNull(7).takeIf { it?.isNotEmpty() == true }
                    val description = parts.getOrNull(8)
                    val printerTag = parts.getOrNull(9)

                    // Basic validation
                    if (name.isNotEmpty()) {
                        // Check if product with this SKU or Barcode already exists to preserve ID
                        val existingProduct = if (barcode != null) {
                            productDao.getProductByBarcode(barcode)
                        } else if (sku.isNotEmpty()) {
                            productDao.getProductByBarcode(sku) // Dao handles SKU or Barcode
                        } else null

                        val productId = existingProduct?.id ?: UUID.randomUUID().toString()
                        val product = Product(
                            id = productId,
                            name = name,
                            sku = sku,
                            barcode = barcode,
                            price = price,
                            taxRate = taxRate,
                            stockQuantity = stock,
                            minStockLevel = minStock,
                            categoryId = categoryId,
                            description = description,
                            printerTag = printerTag
                        )
                        products.add(product)

                        // If stock is specified in CSV, record it as an ADJUSTMENT if it changed
                        if (stock != BigDecimal.ZERO && stock != (existingProduct?.stockQuantity ?: BigDecimal.ZERO)) {
                            val diff = if (existingProduct != null) stock.subtract(existingProduct.stockQuantity) else stock
                            movements.add(
                                StockMovement(
                                    id = UUID.randomUUID().toString(),
                                    productId = productId,
                                    quantity = diff,
                                    type = "IMPORT",
                                    note = "CSV Import"
                                )
                            )
                        }
                    }
                }
                line = reader.readLine()
            }

            if (products.isNotEmpty()) {
                db.withTransaction {
                    productDao.upsertProducts(products)
                    movements.forEach { stockMovementDao.insertMovement(it) }
                }
                Result.success(products.size)
            } else {
                Result.failure(Exception("No valid products found in CSV"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}