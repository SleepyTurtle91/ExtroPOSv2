package com.extrotarget.extroposv2.core.data.local.dao.fnb

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM fnb_tables")
    fun getAllTables(): Flow<List<Table>>

    @Query("SELECT * FROM fnb_tables WHERE id = :tableId")
    suspend fun getTableById(tableId: String): Table?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: Table)

    @Update
    suspend fun updateTable(table: Table)

    @Query("UPDATE fnb_tables SET status = :status, currentSaleId = :saleId WHERE id = :tableId")
    suspend fun updateTableStatus(tableId: String, status: TableStatus, saleId: String?)

    @Delete
    suspend fun deleteTable(table: Table)
}