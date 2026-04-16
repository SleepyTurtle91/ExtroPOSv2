@file:OptIn(ExperimentalMaterial3Api::class)

package com.extrotarget.extroposv2.ui.sales

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.extrotarget.extroposv2.ui.components.qr.QrCodeView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.components.ProductCard
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import com.extrotarget.extroposv2.ui.sales.components.*
import com.extrotarget.extroposv2.ui.fnb.TableFloorPlanScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.ui.loyalty.MemberManagementScreen

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTime by remember { mutableStateOf(java.util.Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = java.util.Date()
            kotlinx.coroutines.delay(1000)
        }
    }

    if (uiState.isLocked) {
        LockScreen(
            onUnlock = { viewModel.unlock(it) },
            errorMessage = uiState.adminAuthError
        )
        return
    }

    val activeMode = uiState.activeMode

    Row(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            SaleHeader(
                activeMode = activeMode,
                uiState = uiState,
                currentTime = currentTime,
                syncStatus = uiState.syncStatus
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState.activeTab) {
                    "pos" -> {
                        PosContent(
                            uiState = uiState,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onProductClick = { viewModel.addToCart(it) },
                            onUpdateQuantity = { item, qty -> viewModel.updateQuantity(item, qty) },
                            onShowModifiers = { viewModel.showModifierSelection(it) },
                            onRemoveFromCart = { viewModel.removeFromCart(it) },
                            onClearCart = { viewModel.clearCartWithConfirm() },
                            onSendToKitchen = { viewModel.sendToKitchen() },
                            onCompleteSale = { viewModel.completeSale(it) },
                            onAddCustomer = { viewModel.setShowMemberSelection(true) },
                            onRedeemPoints = { viewModel.setRedeemedPoints(it) }
                        )
                    }
                    "tables" -> {
                        TableFloorPlanScreen(
                            viewModel = hiltViewModel<com.extrotarget.extroposv2.ui.fnb.viewmodel.TableViewModel>(),
                            onTableClick = { table ->
                                viewModel.selectTable(table)
                                viewModel.setActiveTab("pos")
                            }
                        )
                    }
                    "staff" -> {
                        val staffEarningsViewModel: com.extrotarget.extroposv2.ui.analytics.viewmodel.StaffEarningsViewModel = hiltViewModel()
                        val staffEarningsState by staffEarningsViewModel.uiState.collectAsState()
                        StaffEarnings(
                            staffEarnings = staffEarningsState.staffEarnings
                        )
                    }
                }
            }
        }
    }

    if (uiState.showPaymentMethodDialog) {
        PaymentMethodDialog(
            totalAmount = uiState.totalAmount,
            onSelectMethod = { viewModel.completeSale(it) },
            onDismiss = { viewModel.completeSale("CLOSE_DIALOG") }
        )
    }

    if (uiState.showCashReceivedDialog) {
        CashReceivedDialog(
            totalAmount = uiState.totalAmountCash,
            onConfirm = { viewModel.confirmCashReceived(it) },
            onDismiss = { viewModel.dismissCashReceived() }
        )
    }

    if (uiState.showConfirmClearCart) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelClearCart() },
            title = { Text("CLEAR CART", fontWeight = FontWeight.Black) },
            text = { Text("Are you sure you want to clear the current cart items?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.executeClearCart() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("CLEAR ALL", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelClearCart() }) {
                    Text("CANCEL", fontWeight = FontWeight.Black, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (uiState.showSettingsModal) {
        SettingsModal(
            currentMode = uiState.activeMode,
            onSelectMode = { viewModel.setBusinessMode(it) },
            onClose = { viewModel.toggleSettingsModal(false) },
            onSignOut = { /* Handle Sign Out */ }
        )
    }

    if (uiState.showDiscountDialog) {
        DiscountDialog(
            initialDiscount = if (uiState.itemAwaitingDiscount != null) uiState.itemAwaitingDiscount!!.discount else uiState.cartDiscount,
            onDismiss = { viewModel.dismissDiscountDialog() },
            onApply = { viewModel.applyDiscount(it) }
        )
    }

    if (uiState.showPaymentSuccess) {
        OrderSuccessDialog(
            uiState = uiState,
            onDismiss = { viewModel.dismissPaymentSuccess() },
            onReprint = { viewModel.reprintLastReceipt() }
        )
    }

    if (uiState.itemAwaitingModifiers != null) {
        ModifierDialog(
            item = uiState.itemAwaitingModifiers!!,
            availableModifiers = uiState.availableModifiers,
            onToggleModifier = { viewModel.toggleModifier(it) },
            onDismiss = { viewModel.dismissModifierSelection() }
        )
    }

    if (uiState.showStaffSelection && uiState.itemAwaitingStaff != null) {
        StaffSelectionDialog(
            staffList = uiState.staffList,
            onSelect = { viewModel.assignStaffToItem(it) },
            onDismiss = { viewModel.cancelStaffSelection() }
        )
    }

    if (uiState.showWeightInput && uiState.productAwaitingWeight != null) {
        WeightInputDialog(
            product = uiState.productAwaitingWeight!!,
            onConfirm = { weight -> viewModel.addWeightBasedItem(weight) },
            onDismiss = { viewModel.cancelWeightInput() }
        )
    }

    if (uiState.showTerminalProgress) {
        TerminalProgressDialog(
            status = uiState.terminalStatus,
            totalAmount = uiState.totalAmount
        )
    }

    if (uiState.terminalStatus != null && !uiState.showTerminalProgress) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTerminalError() },
            title = { Text("Terminal Status") },
            text = { Text(uiState.terminalStatus!!) },
            confirmButton = {
                Button(onClick = { viewModel.dismissTerminalError() }) {
                    Text("OK")
                }
            }
        )
    }

    if (uiState.showAdminAuthDialog) {
        AdminAuthDialog(
            onDismiss = { viewModel.dismissAdminAuth() },
            onConfirm = { pin -> viewModel.authenticateAdmin(pin) },
            errorMessage = uiState.adminAuthError
        )
    }

    if (uiState.showMemberSelection) {
        Dialog(
            onDismissRequest = { viewModel.setShowMemberSelection(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Member", style = MaterialTheme.typography.headlineMedium)
                        IconButton(onClick = { viewModel.setShowMemberSelection(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    MemberManagementScreen(
                        onMemberSelected = { viewModel.selectMember(it) }
                    )
                }
            }
        }
    }
}

