package com.extrotarget.extroposv2.core.data.model.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dvr
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector

enum class TerminalRole(val displayName: String) {
    MASTER("Master (Main Station)"),
    SLAVE("Slave (Counter Extension)")
}

enum class OperationMode(
    val id: String, 
    val displayName: String, 
    val icon: ImageVector,
    val description: String
) {
    POS_ONLY(
        id = "pos_only",
        displayName = "Counter Mode",
        icon = Icons.Default.PointOfSale,
        description = "Optimized for sales. Connects to Backend for data."
    ),
    BACKEND_ONLY(
        id = "backend_only",
        displayName = "Backend Mode",
        icon = Icons.Default.Computer,
        description = "HQ management station for Products, Stocks, and Reports."
    ),
    HYBRID(
        id = "hybrid",
        displayName = "Hybrid Mode",
        icon = Icons.Default.Dvr,
        description = "Stand-alone operation. Both Sales and Management in one app."
    )
}
