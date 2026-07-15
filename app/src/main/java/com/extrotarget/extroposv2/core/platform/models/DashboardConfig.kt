package com.extrotarget.extroposv2.core.platform.models

import androidx.compose.ui.graphics.vector.ImageVector

data class DashboardConfig(
    val quickActions: List<QuickAction>,
    val widgets: List<DashboardWidget>,
    val alerts: List<DashboardAlert> = emptyList()
)

data class QuickAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val actionId: DashboardActionId
)

enum class DashboardActionId {
    NEW_SALE,
    TABLE_ORDER,
    KITCHEN_QUEUE,
    INVENTORY_CHECK,
    ADD_PRODUCT,
    RECEIVE_LAUNDRY,
    LAUNDRY_PICKUP,
    NEW_CARWASH_JOB,
    CARWASH_QUEUE,
    STAFF_EARNINGS,
    ROOM_BOOKING,
    REPORTS
}

data class DashboardWidget(
    val id: String,
    val label: String,
    val value: String,
    val type: WidgetType
)

enum class WidgetType {
    MONEY,
    COUNT,
    PERCENTAGE
}

data class DashboardAlert(
    val id: String,
    val message: String,
    val severity: AlertSeverity
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}
