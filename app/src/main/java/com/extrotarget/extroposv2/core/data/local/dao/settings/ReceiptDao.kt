package com.extrotarget.extroposv2.core.data.local.dao.settings

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipt_configs WHERE id = 'default_receipt'")
    fun getReceiptConfig(): Flow<ReceiptConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReceiptConfig(config: ReceiptConfig)
}