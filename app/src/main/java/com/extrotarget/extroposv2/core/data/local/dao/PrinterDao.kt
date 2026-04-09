package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface PrinterDao {
    @Query("SELECT * FROM printer_configs WHERE id = 'default_printer' LIMIT 1")
    fun getDefaultPrinter(): Flow<PrinterConfig?>

    @Query("SELECT * FROM printer_configs WHERE printerTag = :tag")
    fun getPrintersByTag(tag: String): Flow<List<PrinterConfig>>

    @Query("SELECT * FROM printer_configs")
    fun getAllPrinters(): Flow<List<PrinterConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: PrinterConfig)

    @Query("DELETE FROM printer_configs WHERE id = :id")
    suspend fun deleteConfig(id: String)
}