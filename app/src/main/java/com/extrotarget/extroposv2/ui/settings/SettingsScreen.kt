package com.extrotarget.extroposv2.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.ui.navigation.Screen
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import com.extrotarget.extroposv2.core.util.LocaleHelper
import com.extrotarget.extroposv2.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeMode = uiState.activeMode
    
    val isTrainingMode by viewModel.settingsRepository.isTrainingModeEnabled.collectAsState(initial = false)
    val currentLanguageCode by viewModel.settingsRepository.languageCode.collectAsState(initial = "en")
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                SettingsCategoryHeader(stringResource(R.string.onboarding_select_business)) // Or dynamic category if needed
                SettingsItem(
                    title = "User Manual & Help",
                    subtitle = "Learn how to use ExtroPOS v2",
                    icon = Icons.Default.Info,
                    onClick = { onNavigateTo(Screen.Help.route) }
                )
                SettingsItem(
                    title = "Switch Business Mode",
                    subtitle = "Current: ${activeMode.displayName}",
                    icon = Icons.Default.SwapHoriz,
                    onClick = { onNavigateTo(Screen.ModeSelection.route) }
                )

                ListItem(
                    headlineContent = { Text("Training Mode") },
                    supportingContent = { Text("Practice without affecting actual data. Data clears on exit.") },
                    leadingContent = { 
                        Icon(
                            Icons.Default.School, 
                            contentDescription = null, 
                            tint = if (isTrainingMode) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                        ) 
                    },
                    trailingContent = {
                        Switch(
                            checked = isTrainingMode,
                            onCheckedChange = { viewModel.setTrainingMode(it) }
                        )
                    }
                )
            }

            item {
                SettingsCategoryHeader("Regional & Language")
                SettingsItem(
                    title = stringResource(R.string.settings_language),
                    subtitle = LocaleHelper.getDisplayName(currentLanguageCode),
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }

            item {
                SettingsCategoryHeader(stringResource(R.string.settings_printer))
                SettingsItem(
                    title = "Printer Configuration",
                    subtitle = "Setup Bluetooth, USB, or Network printers",
                    icon = Icons.Default.Print,
                    onClick = { onNavigateTo("printer_settings") }
                )
                SettingsItem(
                    title = "Receipt Layout",
                    subtitle = "Customize header, footer, and tax display",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    onClick = { onNavigateTo(Screen.ReceiptSettings.route) }
                )
            }

            item {
                SettingsCategoryHeader("Business Configuration")
                SettingsItem(
                    title = "Products & Catalog",
                    subtitle = "Manage Categories, Products and Modifiers",
                    icon = Icons.Default.Inventory,
                    onClick = { onNavigateTo(Screen.ProductManagement.route) }
                )
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
                SettingsItem(
                    title = "Branch Management",
                    subtitle = "Setup HQ/Branch Sync and Inter-Branch Stock",
                    icon = Icons.Default.Sync,
                    onClick = { onNavigateTo(Screen.BranchSettings.route) }
                )

                if (activeMode.hasRoomManagement || activeMode.hasBookings) {
                    SettingsItem(
                        title = "Room Configuration",
                        subtitle = "Add, edit, or remove rooms and properties",
                        icon = Icons.Default.MeetingRoom,
                        onClick = { onNavigateTo(Screen.RoomManagement.route) }
                    )
                }
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
                if (activeMode.hasBookings) {
                    SettingsItem(
                        title = "Hospitality Management",
                        subtitle = if (activeMode.hasRoomManagement) "Manage rooms, bookings, and guests" else "Manage homestay bookings and guests",
                        icon = activeMode.icon,
                        onClick = { onNavigateTo(Screen.HotelDashboard.route) }
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

            item {
                SettingsCategoryHeader("Reports & Audits")
                SettingsItem(
                    title = "Shift History",
                    subtitle = "View and print past Z-Reports",
                    icon = Icons.Default.History,
                    onClick = { onNavigateTo(Screen.ShiftHistory.route) }
                )
                SettingsItem(
                    title = "LHDN Compliance History",
                    subtitle = "View submitted e-Invoices to LHDN",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    onClick = { onNavigateTo(Screen.LhdnHistory.route) }
                )
            }

            item {
                SettingsCategoryHeader("About & Support")
                SettingsItem(
                    title = "About ExtroPOS",
                    subtitle = "Version ${BuildConfig.VERSION_NAME}",
                    icon = Icons.Default.Info,
                    onClick = { onNavigateTo(Screen.About.route) }
                )
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLanguageCode,
            onDismiss = { showLanguageDialog = false },
            onSelect = { code ->
                viewModel.setLanguage(code)
                showLanguageDialog = false
            }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_select_language)) },
        text = {
            Column {
                val languages = listOf(
                    "en" to stringResource(R.string.lang_english),
                    "ms" to stringResource(R.string.lang_malay),
                    "zh" to stringResource(R.string.lang_chinese)
                )
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == currentLanguageCode,
                            onClick = { onSelect(code) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
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
