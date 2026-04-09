package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = 
        productDao.getProductsByCategory(categoryId)

    suspend fun getProductById(id: String): Product? = productDao.getProductById(id)

    suspend fun getProductByBarcode(barcode: String): Product? = 
        productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()
}