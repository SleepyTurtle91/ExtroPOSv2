package com.extrotarget.extroposv2.core.data.model.platform

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.data.model.settings.OperationMode
import com.extrotarget.extroposv2.ui.sales.BusinessMode

@Entity(tableName = "workspace_config")
data class WorkspaceEntity(
    @PrimaryKey val id: Long = 1, // Single-row config
    val businessName: String,
    val businessMode: BusinessMode,
    val operationMode: OperationMode,
    val enabledCapabilities: Set<Capability>,
    val schemaVersion: Int = 1
)
