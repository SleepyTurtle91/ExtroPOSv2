package com.extrotarget.extroposv2.core.util.importer

import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.model.Product
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductImportManager @Inject constructor(
    private val productDao: ProductDao
) {
    suspend fun importFromCsv(inputStream: InputStream): Result<Int> {
        return try {
            val products = mutableListOf<Product>()
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

                        val product = Product(
                            id = existingProduct?.id ?: UUID.randomUUID().toString(),
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
                    }
                }
                line = reader.readLine()
            }

            if (products.isNotEmpty()) {
                productDao.upsertProducts(products)
                Result.success(products.size)
            } else {
                Result.failure(Exception("No valid products found in CSV"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}