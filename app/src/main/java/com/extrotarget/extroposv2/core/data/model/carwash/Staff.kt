package com.extrotarget.extroposv2.core.data.model.carwash

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String?,
    val role: String, // e.g., "CASHIER", "SUPERVISOR", "ADMIN"
    val pin: String? = null, // 4-6 digit PIN for screen lock
    val isActive: Boolean = true
)