package com.extrotarget.extroposv2.core.security

enum class Permission(
    val id: String,
    val displayName: String
) {
    // --- Navigation / View Permissions ---
    VIEW_DASHBOARD("view_dashboard", "View Workspace Dashboard"),
    VIEW_SALES("view_sales", "Access Sales Counter"),
    VIEW_INVENTORY("view_inventory", "Access Inventory Management"),
    VIEW_ANALYTICS("view_analytics", "View Business Analytics"),
    VIEW_REPORTS("view_reports", "View Financial Reports"),
    VIEW_STAFF("view_staff", "Manage Staff & Commissions"),
    VIEW_SETTINGS("view_settings", "Access System Settings"),
    
    // --- Sales Actions ---
    CREATE_SALE("create_sale", "Create New Transactions"),
    VOID_SALE("void_sale", "Void Completed Transactions"),
    REFUND_SALE("refund_sale", "Process Sale Refunds"),
    REFUND_AFTER_CLOSE("refund_after_close", "Refund After Shift Close"),
    DELETE_TRANSACTION("delete_transaction", "Delete Transaction Record"),
    APPLY_DISCOUNT("apply_discount", "Apply Manual Discounts"),
    CHANGE_PAYMENT_METHOD("change_payment_method", "Change Payment Method"),
    CHANGE_ITEM_PRICE("change_item_price", "Manual Price Override"),
    CHANGE_TAX_RATE("change_tax_rate", "Modify Tax Settings"),
    
    // --- Inventory Actions ---
    EDIT_PRODUCT("edit_product", "Add/Edit Products"),
    ADJUST_STOCK("adjust_stock", "Manual Stock Adjustments"),
    LARGE_STOCK_ADJUSTMENT("large_stock_adjustment", "Large Stock Adjustments"),
    STOCK_TRANSFER("stock_transfer", "Process Branch Transfers"),
    
    // --- Admin / System ---
    ACCESS_MAINTENANCE("access_maintenance", "Access Maintenance Mode"),
    MANAGE_LICENSE("manage_license", "Manage Software Licensing"),
    DATABASE_BACKUP("database_backup", "Backup & Restore Database"),
    CASH_DRAWER_OPEN("cash_drawer_open", "Manual Drawer Open"),
    SHIFT_OVERRIDE("shift_override", "Override Shift Constraints")
}
