package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.settings.AutoCountConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoCountDao {
    @Query("SELECT * FROM autocount_config WHERE id = 1")
    fun getConfig(): Flow<AutoCountConfig?>

    @Query("SELECT * FROM autocount_config WHERE id = 1")
    suspend fun getConfigSync(): AutoCountConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AutoCountConfig)
}
