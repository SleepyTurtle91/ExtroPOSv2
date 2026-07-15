package com.extrotarget.extroposv2.core.data.local.dao.platform

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.platform.WorkspaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspace_config WHERE id = 1")
    fun getWorkspaceConfig(): Flow<WorkspaceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWorkspaceConfig(config: WorkspaceEntity)
}
