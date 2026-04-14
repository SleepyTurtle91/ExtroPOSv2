package com.extrotarget.extroposv2.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.ui.navigation.Screen
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeMode = uiState.activeMode

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                SettingsCategoryHeader("Hardware & Printing")
                SettingsItem(
                    title = "Printer Configuration",
                    subtitle = "Setup Bluetooth, USB, or Network printers",
                    icon = Icons.Default.Print,
                    onClick = { onNavigateTo("printer_settings") }
                )
                SettingsItem(
                    title = "Receipt Layout",
                    subtitle = "Customize header, footer, and tax display",
                    icon = Icons.Default.ReceiptLong,
                    onClick = { onNavigateTo(Screen.ReceiptSettings.route) }
                )
            }

            item {
                SettingsCategoryHeader("Business Configuration")
                SettingsItem(
                    title = "Payment Methods",
                    subtitle = "Manage Cash, DuitNow, and Custom E-Wallets",
                    icon = Icons.Default.Payments,
                    onClick = { onNavigateTo("payment_settings") }
                )
                SettingsItem(
                    title = "Tax & SST Settings",
                    subtitle = "Configure 6%, 8% or 10% tax rates",
                    icon = Icons.Default.Percent,
                    onClick = { onNavigateTo("tax_settings") }
                )
                SettingsItem(
                    title = "LHDN MyInvois",
                    subtitle = "Configure e-Invoicing (TIN, BRN, MSIC)",
                    icon = Icons.Default.CloudUpload,
                    onClick = { onNavigateTo("lhdn_settings") }
                )
                SettingsItem(
                    title = "AutoCount Sync",
                    subtitle = "Automated accounting data synchronization",
                    icon = Icons.Default.Sync,
                    onClick = { onNavigateTo(Screen.AutoCountSettings.route) }
                )
                SettingsItem(
                    title = "Loyalty & Members",
                    subtitle = "Configure points and member rewards",
                    icon = Icons.Default.CardMembership,
                    onClick = { onNavigateTo(Screen.LoyaltySettings.route) }
                )
            }

            item {
                SettingsCategoryHeader("Modules & Features")
                if (activeMode == BusinessMode.FNB) {
                    SettingsItem(
                        title = "F&B Table Management",
                        subtitle = "Configure floor plan and table layouts",
                        icon = Icons.Default.TableBar,
                        onClick = { onNavigateTo(Screen.Tables.route) }
                    )
                }
                if (activeMode == BusinessMode.LAUNDRY) {
                    SettingsItem(
                        title = "Laundry Configuration",
                        subtitle = "Setup weight units and WhatsApp alerts",
                        icon = Icons.Default.LocalLaundryService,
                        onClick = { onNavigateTo(Screen.Laundry.route) }
                    )
                }
                if (activeMode == BusinessMode.CARWASH) {
                    SettingsItem(
                        title = "Car Wash Services",
                        subtitle = "Manage wash types and staff commissions",
                        icon = Icons.Default.DirectionsCar,
                        onClick = { /* onNavigateTo("carwash_settings") */ }
                    )
                }
            }

            item {
                SettingsCategoryHeader("System & Data")
                SettingsItem(
                    title = "Analytics Dashboard",
                    subtitle = "View sales reports and performance",
                    icon = Icons.Default.Analytics,
                    onClick = { onNavigateTo(Screen.Analytics.route) }
                )
                SettingsItem(
                    title = "Backup & Restore",
                    subtitle = "Export or import database backups",
                    icon = Icons.Default.Backup,
                    onClick = { onNavigateTo(Screen.Backup.route) }
                )
                SettingsItem(
                    title = "Security & Audit Logs",
                    subtitle = "Monitor sensitive actions and user changes",
                    icon = Icons.Default.Security,
                    onClick = { onNavigateTo(Screen.SecurityAudit.route) }
                )
                SettingsItem(
                    title = "Multi-Terminal Sync",
                    subtitle = "Sync data between Master and Slave devices",
                    icon = Icons.Default.CastConnected,
                    onClick = { onNavigateTo(Screen.TerminalSync.route) }
                )
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    )
}
