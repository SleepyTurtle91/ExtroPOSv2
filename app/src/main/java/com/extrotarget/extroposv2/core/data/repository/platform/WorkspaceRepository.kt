package com.extrotarget.extroposv2.core.data.repository.platform

import com.extrotarget.extroposv2.core.data.local.dao.platform.WorkspaceDao
import com.extrotarget.extroposv2.core.data.model.platform.WorkspaceEntity
import com.extrotarget.extroposv2.core.data.model.settings.OperationMode
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceRepository @Inject constructor(
    private val workspaceDao: WorkspaceDao
) {
    val workspaceConfig: Flow<WorkspaceEntity?> = workspaceDao.getWorkspaceConfig()

    suspend fun initializeWorkspace(
        businessName: String,
        mode: BusinessMode,
        opMode: OperationMode = OperationMode.HYBRID
    ) {
        val entity = WorkspaceEntity(
            businessName = businessName,
            businessMode = mode,
            operationMode = opMode,
            enabledCapabilities = mode.defaultCapabilities
        )
        workspaceDao.updateWorkspaceConfig(entity)
    }

    suspend fun updateOperationMode(mode: OperationMode) {
        val current = workspaceDao.getWorkspaceConfig().firstOrNull() ?: return
        workspaceDao.updateWorkspaceConfig(current.copy(operationMode = mode))
    }
}

// Helper extension if needed
suspend fun <T> Flow<T>.firstOrNull(): T? {
    return try {
        kotlinx.coroutines.flow.first()
    } catch (e: Exception) {
        null
    }
}
