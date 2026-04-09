package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.extrotarget.extroposv2.ui.sales.SalesScreen
import com.extrotarget.extroposv2.ui.inventory.InventoryScreen
import com.extrotarget.extroposv2.ui.carwash.staff.StaffManagementScreen
import com.extrotarget.extroposv2.ui.dobi.LaundryOrderScreen
import com.extrotarget.extroposv2.ui.fnb.TableFloorPlanScreen
import com.extrotarget.extroposv2.ui.analytics.AnalyticsScreen
import com.extrotarget.extroposv2.ui.settings.backup.BackupRestoreScreen
import com.extrotarget.extroposv2.ui.settings.SettingsScreen
import com.extrotarget.extroposv2.ui.settings.tax.TaxSettingsScreen
import com.extrotarget.extroposv2.ui.settings.printer.PrinterSettingsScreen
import com.extrotarget.extroposv2.ui.settings.payment.PaymentMethodSettingsScreen
import com.extrotarget.extroposv2.ui.settings.receipt.ReceiptSettingsScreen
import com.extrotarget.extroposv2.ui.inventory.viewmodel.InventoryViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Sales.route,
        modifier = modifier
    ) {
        composable(Screen.Sales.route) {
            SalesScreen(viewModel = hiltViewModel())
        }
        
        composable(Screen.Inventory.route) {
            InventoryScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Staff.route) {
            StaffManagementScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Tables.route) {
            TableFloorPlanScreen(
                viewModel = hiltViewModel(),
                onTableClick = { table ->
                    // Logic to open sales screen with this table selected
                    navController.navigate(Screen.Sales.route)
                }
            )
        }

        composable(Screen.Laundry.route) {
            LaundryOrderScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Backup.route) {
            BackupRestoreScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }

        composable("printer_settings") {
            PrinterSettingsScreen(viewModel = hiltViewModel())
        }

        composable("payment_settings") {
            PaymentMethodSettingsScreen(viewModel = hiltViewModel())
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
    }
}