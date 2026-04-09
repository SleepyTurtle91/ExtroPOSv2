package com.extrotarget.extroposv2.core.data.model.carwash

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String?,
    val role: String, // e.g., "WASHER", "SUPERVISOR"
    val isActive: Boolean = true
)