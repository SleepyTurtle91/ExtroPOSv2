package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsNow(): List<Product>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode OR sku = :barcode")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("UPDATE products SET stockQuantity = stockQuantity + :adjustment WHERE id = :productId")
    suspend fun updateStockQuantity(productId: String, adjustment: java.math.BigDecimal)

    @Upsert
    suspend fun upsertProducts(products: List<Product>)

    @Query("SELECT * FROM products WHERE stockQuantity <= minStockLevel AND minStockLevel > 0")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("UPDATE products SET stockQuantity = :quantity WHERE id = :productId")
    suspend fun setStockQuantity(productId: String, quantity: java.math.BigDecimal)
}
