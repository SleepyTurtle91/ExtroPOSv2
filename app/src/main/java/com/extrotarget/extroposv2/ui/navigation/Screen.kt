package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Sales : Screen("sales", "Sales", Icons.Default.PointOfSale)
    object Inventory : Screen("inventory", "Inventory", Icons.Default.Inventory)
    object Staff : Screen("staff", "Staff", Icons.Default.Badge)
    object Tables : Screen("tables", "Tables", Icons.Default.TableBar)
    object Laundry : Screen("laundry", "Laundry", Icons.Default.LocalLaundryService)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Backup : Screen("backup", "Backup & Restore", Icons.Default.Backup)
    object ReceiptSettings : Screen("receipt_settings", "Receipt Layout", Icons.Default.ReceiptLong)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
