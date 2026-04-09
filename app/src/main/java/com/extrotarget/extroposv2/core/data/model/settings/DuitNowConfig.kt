package com.extrotarget.extroposv2.core.data.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duitnow_configs")
data class DuitNowConfig(
    @PrimaryKey val id: Int = 1,
    val merchantId: String = "12345678",
    val merchantName: String = "EXTROPOS MERCHANT",
    val city: String = "KUALA LUMPUR",
    val isEnabled: Boolean = true
)
