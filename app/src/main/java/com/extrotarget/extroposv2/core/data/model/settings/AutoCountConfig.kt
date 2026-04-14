package com.extrotarget.extroposv2.core.data.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autocount_config")
data class AutoCountConfig(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = false,
    val apiUrl: String = "http://localhost:8080/",
    val username: String = "",
    val password: String = "",
    val cashAccountCode: String = "300-000",
    val cardAccountCode: String = "300-001",
    val defaultTaxCode: String = "SR-S",
    val syncToken: String? = null,
    val tokenExpiry: Long = 0
)
