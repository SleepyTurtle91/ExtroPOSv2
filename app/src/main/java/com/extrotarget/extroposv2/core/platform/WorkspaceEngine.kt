package com.extrotarget.extroposv2.core.platform

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.local.dao.platform.WorkspaceDao
import com.extrotarget.extroposv2.core.data.model.platform.Capability
import com.extrotarget.extroposv2.core.data.model.platform.WorkspaceEntity
import com.extrotarget.extroposv2.core.platform.capability.CapabilityResolver
import com.extrotarget.extroposv2.core.platform.models.*
import com.extrotarget.extroposv2.core.security.Permission
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceEngine @Inject constructor(
    private val workspaceDao: WorkspaceDao,
    private val printerDao: PrinterDao,
    private val sessionManager: SessionManager,
    private val capabilityResolver: CapabilityResolver
) {
    val workspaceConfig: StateFlow<WorkspaceEntity?> = workspaceDao.getWorkspaceConfig()
        .stateIn(kotlinx.coroutines.GlobalScope, SharingStarted.Eagerly, null)

    fun hasPermission(permission: Permission): Boolean {
        val user = sessionManager.getCurrentStaff() ?: return false
        val role = user.role.uppercase()
        
        // Admin has all permissions
        if (role == "ADMIN") return true
        
        return when (permission) {
            Permission.VIEW_DASHBOARD -> true
            Permission.VIEW_SALES -> true
            Permission.CREATE_SALE -> true
            
            Permission.VOID_SALE, Permission.REFUND_SALE, Permission.APPLY_DISCOUNT -> {
                role == "SUPERVISOR" || role == "MANAGER"
            }
            
            Permission.VIEW_INVENTORY, Permission.EDIT_PRODUCT, Permission.ADJUST_STOCK -> {
                role == "MANAGER" || role == "INVENTORY"
            }
            
            Permission.VIEW_ANALYTICS, Permission.VIEW_REPORTS -> {
                role == "MANAGER" || role == "OWNER"
            }
            
            Permission.VIEW_STAFF -> role == "MANAGER" || role == "OWNER"
            Permission.VIEW_SETTINGS -> role == "MANAGER" || role == "OWNER"
            
            Permission.ACCESS_MAINTENANCE -> role == "MANAGER" || role == "OWNER"
            Permission.MANAGE_LICENSE -> role == "MANAGER" || role == "OWNER"
            Permission.DATABASE_BACKUP -> role == "MANAGER" || role == "OWNER"
            
            else -> false
        }
    }

    suspend fun isCapabilityEnabled(capability: Capability): Boolean {
        val config = workspaceConfig.value ?: return false
        val businessMode = config.businessMode
        
        val supportedCapabilities = capabilityResolver.getCapabilitiesForMode(businessMode)
        if (!supportedCapabilities.contains(capability)) return false

        val isHardwareReady = when (capability) {
            Capability.KITCHEN_TICKET, Capability.TABLE_MANAGEMENT -> {
                printerDao.getAllPrinters().first().isNotEmpty()
            }
            else -> true
        }

        return isHardwareReady && config.enabledCapabilities.contains(capability)
    }

    fun getDashboardConfig(): DashboardConfig {
        val config = workspaceConfig.value ?: return defaultDashboard()
        
        val actions = mutableListOf<QuickAction>()
        
        when (config.businessMode) {
            BusinessMode.RETAIL -> {
                actions.add(QuickAction("retail_sale", "Quick Sale", Icons.Default.ShoppingCart, DashboardActionId.NEW_SALE))
                actions.add(QuickAction("retail_inventory", "Stock Check", Icons.Default.Inventory, DashboardActionId.INVENTORY_CHECK))
                actions.add(QuickAction("retail_product", "Add Product", Icons.Default.AddBusiness, DashboardActionId.ADD_PRODUCT))
            }
            BusinessMode.FNB -> {
                actions.add(QuickAction("fnb_order", "New Table Order", Icons.Default.Restaurant, DashboardActionId.TABLE_ORDER))
                actions.add(QuickAction("fnb_kds", "Kitchen Queue", Icons.Default.ListAlt, DashboardActionId.KITCHEN_QUEUE))
                actions.add(QuickAction("fnb_reports", "Sales Reports", Icons.Default.Assessment, DashboardActionId.REPORTS))
            }
            BusinessMode.CARWASH -> {
                actions.add(QuickAction("cw_new", "New Job", Icons.Default.DirectionsCar, DashboardActionId.NEW_CARWASH_JOB))
                actions.add(QuickAction("cw_queue", "Active Queue", Icons.Default.Queue, DashboardActionId.CARWASH_QUEUE))
                actions.add(QuickAction("cw_staff", "Staff Earnings", Icons.Default.Payments, DashboardActionId.STAFF_EARNINGS))
            }
            BusinessMode.LAUNDRY -> {
                actions.add(QuickAction("dry_new", "New Order", Icons.Default.LocalLaundryService, DashboardActionId.RECEIVE_LAUNDRY))
                actions.add(QuickAction("dry_pickup", "Ready for Pickup", Icons.Default.CheckCircle, DashboardActionId.LAUNDRY_PICKUP))
                actions.add(QuickAction("dry_status", "Status Check", Icons.Default.Search, DashboardActionId.INVENTORY_CHECK))
            }
            BusinessMode.HOTEL, BusinessMode.HOMESTAY -> {
                actions.add(QuickAction("hotel_book", "New Booking", Icons.Default.BookOnline, DashboardActionId.ROOM_BOOKING))
                actions.add(QuickAction("hotel_rooms", "Room Status", Icons.Default.Hotel, DashboardActionId.INVENTORY_CHECK))
            }
            else -> {}
        }
        
        val widgets = listOf(
            DashboardWidget("today_sales", "Today's Sales", "RM 0.00", WidgetType.MONEY),
            DashboardWidget("today_orders", "Transactions", "0", WidgetType.COUNT)
        )
        
        return DashboardConfig(
            quickActions = actions,
            widgets = widgets,
            alerts = emptyList()
        )
    }

    private fun defaultDashboard() = DashboardConfig(
        quickActions = listOf(QuickAction("default_sale", "New Sale", Icons.Default.Add, DashboardActionId.NEW_SALE)),
        widgets = emptyList()
    )
}
