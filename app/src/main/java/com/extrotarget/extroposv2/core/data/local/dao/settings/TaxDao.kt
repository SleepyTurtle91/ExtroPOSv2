package com.extrotarget.extroposv2.core.data.local.dao.settings

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.settings.TaxConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxDao {
    @Query("SELECT * FROM tax_configs WHERE id = 'default_tax' LIMIT 1")
    fun getTaxConfig(): Flow<TaxConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxConfig(config: TaxConfig)
}
