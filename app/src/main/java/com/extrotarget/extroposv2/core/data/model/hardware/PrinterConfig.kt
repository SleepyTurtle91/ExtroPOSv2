package com.extrotarget.extroposv2.core.data.model.hardware

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "printer_configs")
data class PrinterConfig(
    @PrimaryKey val id: String = "default_printer",
    val name: String,
    val connectionType: String, // BLUETOOTH, USB, NETWORK
    val address: String, // MAC address, IP, or USB device ID
    val port: Int = 9100, // For Network
    val isDefault: Boolean = true,
    val printerTag: String? = null // e.g., "KITCHEN", "BAR", "RECEIPT"
)