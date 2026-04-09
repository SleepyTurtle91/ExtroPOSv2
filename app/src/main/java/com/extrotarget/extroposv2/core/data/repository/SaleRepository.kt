package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao
) {
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>> =
        saleDao.getAllSalesWithItems()

    suspend fun completeSale(sale: Sale, items: List<SaleItem>) = 
        saleDao.completeSale(sale, items)

    suspend fun getSaleById(saleId: String): Sale? = saleDao.getSaleById(saleId)

    suspend fun getItemsBySaleId(saleId: String): List<SaleItem> = 
        saleDao.getItemsBySaleId(saleId)

    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>> =
        saleDao.getSalesInRange(start, end)
}