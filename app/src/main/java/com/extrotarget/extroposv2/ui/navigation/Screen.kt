package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Sales : Screen("sales", "Sales", Icons.Default.PointOfSale)
    object Inventory : Screen("inventory", "Inventory", Icons.Default.Inventory)
    object Staff : Screen("staff", "Staff", Icons.Default.Badge)
    object Tables : Screen("tables", "Tables", Icons.Default.TableBar)
    object Laundry : Screen("laundry", "Laundry", Icons.Default.LocalLaundryService)
    object CarWash : Screen("carwash", "Car Wash", Icons.Default.LocalLaundryService) // Reusing icon or find a better one
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Backup : Screen("backup", "Backup & Restore", Icons.Default.Backup)
    object ReceiptSettings : Screen("receipt_settings", "Receipt Layout", Icons.AutoMirrored.Filled.ReceiptLong)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object SecurityAudit : Screen("security_audit", "Security Logs", Icons.AutoMirrored.Filled.ReceiptLong) // Reusing icon for now
    object TerminalSync : Screen("terminal_sync", "Multi-Terminal Sync", Icons.Default.Sync)
    object AutoCountSettings : Screen("autocount_settings", "AutoCount Sync", Icons.Default.Sync)
    object LoyaltySettings : Screen("loyalty_settings", "Loyalty Program", Icons.Default.CardMembership)
    object MemberManagement : Screen("member_management", "Members", Icons.Default.CardMembership)
    object LhdnHistory : Screen("lhdn_history", "LHDN History", Icons.AutoMirrored.Filled.ReceiptLong)
    object ZReport : Screen("z_report", "Shift Closeout", Icons.AutoMirrored.Filled.ReceiptLong)
    object ShiftManagement : Screen("shift_management", "Shift", Icons.Default.PointOfSale)
    object ShiftHistory : Screen("shift_history", "Shift History", Icons.AutoMirrored.Filled.ReceiptLong)
    object BranchSettings : Screen("branch_settings", "Branch Management", Icons.Default.Sync)
    object StockTransfer : Screen("stock_transfer", "Stock Transfer", Icons.Default.Inventory)
    object ProductManagement : Screen("product_management", "Products & Modifiers", Icons.Default.EditNote)
    
    // Analytics Sub-screens
    object InventoryAnalytics : Screen("inventory_analytics", "Low Stock", Icons.Default.Inventory)
    object StaffEarnings : Screen("staff_earnings", "Staff Earnings", Icons.Default.Badge)
}
