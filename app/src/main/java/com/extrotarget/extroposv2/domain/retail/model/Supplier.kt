package com.extrotarget.extroposv2.domain.retail.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey val id: String,
    val name: String,
    val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val sstId: String? = null,
    val isActive: Boolean = true
)
