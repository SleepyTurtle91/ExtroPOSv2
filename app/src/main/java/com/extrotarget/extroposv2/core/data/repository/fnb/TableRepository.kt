package com.extrotarget.extroposv2.core.data.repository.fnb

import com.extrotarget.extroposv2.core.data.local.dao.fnb.TableDao
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableRepository @Inject constructor(
    private val tableDao: TableDao
) {
    val allTables: Flow<List<Table>> = tableDao.getAllTables()

    suspend fun getTable(id: String): Table? = tableDao.getTableById(id)

    suspend fun addTable(table: Table) = tableDao.insertTable(table)

    suspend fun updateTable(table: Table) = tableDao.updateTable(table)

    suspend fun occupyTable(tableId: String, saleId: String) {
        tableDao.updateTableStatus(tableId, TableStatus.OCCUPIED, saleId)
    }

    suspend fun releaseTable(tableId: String) {
        tableDao.updateTableStatus(tableId, TableStatus.AVAILABLE, null)
    }
    
    suspend fun markTableDirty(tableId: String) {
        tableDao.updateTableStatus(tableId, TableStatus.DIRTY, null)
    }
}