package com.extrotarget.extroposv2.domain.fnb.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fnb_kitchen_stations")
data class KitchenStation(
    @PrimaryKey val id: String,
    val name: String,
    val printerId: String? = null,
    val isEnabled: Boolean = true
)
