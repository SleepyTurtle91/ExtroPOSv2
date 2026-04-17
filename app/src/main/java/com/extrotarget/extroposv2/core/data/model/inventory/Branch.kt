package com.extrotarget.extroposv2.core.data.model.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branches")
data class Branch(
    @PrimaryKey val id: String,
    val name: String,
    val ipAddress: String, // Public IP or local IP of the HQ
    val isHQ: Boolean = false,
    val syncToken: String? = null,
    val lastSyncTimestamp: Long = 0L,
    val isEnabled: Boolean = true
)
