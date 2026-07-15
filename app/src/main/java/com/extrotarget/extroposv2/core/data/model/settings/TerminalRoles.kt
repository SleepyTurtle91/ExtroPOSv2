package com.extrotarget.extroposv2.core.data.model.settings

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector
import com.extrotarget.extroposv2.R

enum class TerminalRole(@StringRes val displayName: Int) {
    MASTER(R.string.role_master),
    SLAVE(R.string.role_slave)
}

enum class OperationMode(
    val id: String, 
    @StringRes val displayName: Int, 
    val icon: ImageVector,
    @StringRes val description: Int
) {
    POS_ONLY(
        id = "pos_only",
        displayName = R.string.op_mode_pos_name,
        icon = Icons.Default.PointOfSale,
        description = R.string.op_mode_pos_desc
    ),
    BACKEND_ONLY(
        id = "backend_only",
        displayName = R.string.op_mode_backend_name,
        icon = Icons.Default.Computer,
        description = R.string.op_mode_backend_desc
    ),
    HYBRID(
        id = "hybrid",
        displayName = R.string.op_mode_hybrid_name,
        icon = Icons.AutoMirrored.Filled.Dvr,
        description = R.string.op_mode_hybrid_desc
    )
}
