package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.extrotarget.extroposv2.ui.sales.SalesScreen
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import com.extrotarget.extroposv2.ui.inventory.InventoryScreen
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.ui.carwash.staff.StaffManagementScreen
import com.extrotarget.extroposv2.ui.dobi.LaundryOrderScreen
import com.extrotarget.extroposv2.ui.carwash.CarWashJobQueueScreen
import com.extrotarget.extroposv2.ui.fnb.TableFloorPlanScreen
import com.extrotarget.extroposv2.ui.analytics.AnalyticsScreen
import com.extrotarget.extroposv2.ui.analytics.AnalyticsScreen
import com.extrotarget.extroposv2.ui.analytics.InventoryAnalyticsScreen
import com.extrotarget.extroposv2.ui.analytics.StaffEarningsReportScreen
import com.extrotarget.extroposv2.ui.analytics.viewmodel.AnalyticsViewModel
import com.extrotarget.extroposv2.ui.settings.backup.BackupScreen
import com.extrotarget.extroposv2.ui.settings.SettingsScreen
import com.extrotarget.extroposv2.ui.settings.tax.TaxSettingsScreen
import com.extrotarget.extroposv2.ui.settings.printer.PrinterSettingsScreen
import com.extrotarget.extroposv2.ui.settings.payment.PaymentMethodSettingsScreen
import com.extrotarget.extroposv2.ui.settings.payment.DuitNowSettingsScreen
import com.extrotarget.extroposv2.ui.settings.receipt.ReceiptSettingsScreen
import com.extrotarget.extroposv2.ui.settings.lhdn.LhdnSettingsScreen
import com.extrotarget.extroposv2.ui.settings.autocount.AutoCountSettingsScreen
import com.extrotarget.extroposv2.ui.settings.audit.AuditScreen
import com.extrotarget.extroposv2.ui.settings.sync.SyncScreen
import com.extrotarget.extroposv2.ui.loyalty.MemberManagementScreen
import com.extrotarget.extroposv2.ui.loyalty.LoyaltySettingsScreen
import com.extrotarget.extroposv2.ui.inventory.viewmodel.InventoryViewModel
import com.extrotarget.extroposv2.ui.lhdn.history.LhdnHistoryScreen
import com.extrotarget.extroposv2.ui.lhdn.history.LhdnHistoryViewModel
import com.extrotarget.extroposv2.ui.report.ZReportScreen
import com.extrotarget.extroposv2.ui.report.ShiftManagementScreen
import com.extrotarget.extroposv2.ui.report.history.ShiftHistoryScreen

import com.extrotarget.extroposv2.ui.inventory.transfer.StockTransferScreen
import com.extrotarget.extroposv2.ui.settings.branch.BranchSettingsScreen
import com.extrotarget.extroposv2.ui.inventory.InventoryManagementScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Sales.route,
        modifier = modifier
    ) {
        composable(Screen.Sales.route) {
            SalesScreen(
                viewModel = hiltViewModel(),
                sessionManager = sessionManager,
                onNavigateToShift = { navController.navigate(Screen.ShiftManagement.route) }
            )
        }
        
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateToStockTransfer = { navController.navigate(Screen.StockTransfer.route) },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.StockTransfer.route) {
            StockTransferScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.ProductManagement.route) {
            InventoryManagementScreen(viewModel = hiltViewModel())
        }

        composable(Screen.BranchSettings.route) {
            BranchSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Staff.route) {
            StaffManagementScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Tables.route) {
            val salesViewModel: SalesViewModel = hiltViewModel()
            TableFloorPlanScreen(
                viewModel = hiltViewModel(),
                onTableClick = { table ->
                    salesViewModel.selectTable(table)
                    navController.navigate(Screen.Sales.route)
                }
            )
        }

        composable(Screen.Laundry.route) {
            LaundryOrderScreen(viewModel = hiltViewModel())
        }

        composable(Screen.CarWash.route) {
            CarWashJobQueueScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                viewModel = hiltViewModel(),
                onNavigateToLowStock = { navController.navigate(Screen.InventoryAnalytics.route) },
                onNavigateToStaffEarnings = { navController.navigate(Screen.StaffEarnings.route) }
            )
        }

        composable(Screen.InventoryAnalytics.route) {
            InventoryAnalyticsScreen(viewModel = hiltViewModel())
        }

        composable(Screen.StaffEarnings.route) {
            StaffEarningsReportScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Backup.route) {
            BackupScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateTo = { route -> navController.navigate(route) },
                viewModel = hiltViewModel()
            )
        }

        composable("printer_settings") {
            PrinterSettingsScreen(viewModel = hiltViewModel())
        }

        composable("payment_settings") {
            PaymentMethodSettingsScreen(
                onNavigateToDuitNow = { navController.navigate("duitnow_settings") }
            )
        }

        composable("duitnow_settings") {
            DuitNowSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("tax_settings") {
            TaxSettingsScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ReceiptSettings.route) {
            ReceiptSettingsScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("lhdn_settings") {
            LhdnSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Screen.LhdnHistory.route) },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.LhdnHistory.route) {
            LhdnHistoryScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SecurityAudit.route) {
            AuditScreen(viewModel = hiltViewModel())
        }

        composable(Screen.TerminalSync.route) {
            SyncScreen(viewModel = hiltViewModel())
        }

        composable(Screen.AutoCountSettings.route) {
            AutoCountSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.MemberManagement.route) {
            MemberManagementScreen(viewModel = hiltViewModel())
        }

        composable(Screen.LoyaltySettings.route) {
            LoyaltySettingsScreen(viewModel = hiltViewModel())
        }

        composable(Screen.ShiftManagement.route) {
            ShiftManagementScreen(
                onShiftOpened = { navController.navigate(Screen.Sales.route) { popUpTo(Screen.ShiftManagement.route) { inclusive = true } } },
                onViewActiveShift = { navController.navigate(Screen.ZReport.route) }
            )
        }

        composable(Screen.ZReport.route) {
            ZReportScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ShiftHistory.route) {
            ShiftHistoryScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}