package com.extrotarget.extroposv2.core.data.local.dao.settings

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface DuitNowDao {
    @Query("SELECT * FROM duitnow_configs WHERE id = 1")
    fun getConfig(): Flow<DuitNowConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: DuitNowConfig)

    @Query("SELECT COUNT(*) FROM duitnow_configs")
    suspend fun hasConfig(): Int
}
