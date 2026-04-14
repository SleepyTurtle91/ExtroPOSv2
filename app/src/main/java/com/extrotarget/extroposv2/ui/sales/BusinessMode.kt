package com.extrotarget.extroposv2.ui.sales

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class BusinessMode(
    val id: String,
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val hasTables: Boolean = false,
    val hasStaffAssignment: Boolean = false,
    val hasWeightSupport: Boolean = false
) {
    RETAIL(
        id = "retail",
        displayName = "General Retail",
        description = "Inventory-focused POS for shops and marts.",
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFF3B82F6) // Blue 500
    ),
    FNB(
        id = "fnb",
        displayName = "F&B (Cafe/Resto)",
        description = "Table management, kitchen display, and modifiers.",
        icon = Icons.Default.Restaurant,
        color = Color(0xFFF97316), // Orange 500
        hasTables = true
    ),
    CARWASH(
        id = "carwash",
        displayName = "Car Wash",
        description = "Service tracking and staff commission engine.",
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFF10B981), // Emerald 500
        hasStaffAssignment = true
    ),
    LAUNDRY(
        id = "laundry",
        displayName = "Dobi (Laundry)",
        description = "Weight-based pricing and order lifecycle.",
        icon = Icons.Default.LocalLaundryService,
        color = Color(0xFF6366F1), // Indigo 500
        hasWeightSupport = true
    )
}
