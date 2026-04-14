package com.extrotarget.extroposv2.core.util.exporter

import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.model.Product
import kotlinx.coroutines.flow.first
import java.io.OutputStream
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductExportManager @Inject constructor(
    private val productDao: ProductDao
) {
    suspend fun exportToCsv(outputStream: OutputStream): Result<Int> {
        return try {
            val products = productDao.getAllProducts().first()
            val writer = BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))
            
            // Header
            writer.write("Name,SKU,Barcode,Price,TaxRate,StockQuantity,MinStockLevel,CategoryId,Description,PrinterTag,CommissionRate,FixedCommission,ImageUrl,IsAvailable,IsWeightBased")
            writer.newLine()
            
            products.forEach { product ->
                val line = StringBuilder()
                line.append(escapeCsv(product.name)).append(",")
                line.append(escapeCsv(product.sku)).append(",")
                line.append(escapeCsv(product.barcode ?: "")).append(",")
                line.append(product.price.toPlainString()).append(",")
                line.append(product.taxRate.toPlainString()).append(",")
                line.append(product.stockQuantity.toPlainString()).append(",")
                line.append(product.minStockLevel.toPlainString()).append(",")
                line.append(escapeCsv(product.categoryId ?: "")).append(",")
                line.append(escapeCsv(product.description ?: "")).append(",")
                line.append(escapeCsv(product.printerTag ?: "")).append(",")
                line.append(product.commissionRate.toPlainString()).append(",")
                line.append(product.fixedCommission.toPlainString()).append(",")
                line.append(escapeCsv(product.imageUrl ?: "")).append(",")
                line.append(product.isAvailable).append(",")
                line.append(product.isWeightBased)
                
                writer.write(line.toString())
                writer.newLine()
            }
            
            writer.flush()
            Result.success(products.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
