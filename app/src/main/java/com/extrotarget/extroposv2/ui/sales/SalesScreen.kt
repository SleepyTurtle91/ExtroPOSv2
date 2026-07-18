package com.extrotarget.extroposv2.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.ui.components.stitch.FunctionKeyData
import com.extrotarget.extroposv2.ui.components.stitch.StitchCartSidebar
import com.extrotarget.extroposv2.ui.components.stitch.StitchFunctionGrid
import com.extrotarget.extroposv2.ui.components.stitch.StitchProductCard
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchButton

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.extrotarget.extroposv2.R

import com.extrotarget.extroposv2.ui.components.stitch.common.StitchTextField

import com.extrotarget.extroposv2.ui.sales.components.*

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    sessionManager: SessionManager,
    onNavigateToShift: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSidebarCollapsed by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.focusSearchRequest) {
        if (uiState.focusSearchRequest > 0) {
            searchFocusRequester.requestFocus()
        }
    }

    val functionKeys = remember(uiState.activeMode) {
        if (uiState.activeMode == BusinessMode.FNB) {
            listOf(
                FunctionKeyData(PosAction.CUSTOMER, "Customer", Icons.Default.Person),
                FunctionKeyData(PosAction.DISCOUNT, "Discount", Icons.Default.Percent),
                FunctionKeyData(PosAction.HOLD_ORDER, "Hold Table", Icons.Default.TableBar),
                FunctionKeyData(PosAction.KITCHEN_SEND, "Kitchen", Icons.Default.Restaurant),
                FunctionKeyData(PosAction.TRANSFER_TABLE, "Transfer", Icons.Default.MoveUp),
                FunctionKeyData(PosAction.SPLIT_BILL, "Split", Icons.AutoMirrored.Filled.AltRoute),
                FunctionKeyData(PosAction.PRINT_ORDER, "Print", Icons.Default.Print),
                FunctionKeyData(PosAction.PAY, "Pay", Icons.Default.Payments, containerColor = StitchColor.Primary, contentColor = Color.White)
            )
        } else {
            listOf(
                FunctionKeyData(PosAction.CUSTOMER, "Customer", Icons.Default.Person),
                FunctionKeyData(PosAction.DISCOUNT, "Discount", Icons.Default.Percent),
                FunctionKeyData(PosAction.VOID_CART, "Void", Icons.Default.DeleteSweep, contentColor = StitchColor.Error),
                FunctionKeyData(PosAction.HOLD_ORDER, "Hold", Icons.Default.Pause),
                FunctionKeyData(PosAction.REPRINT_LAST, "Reprint", Icons.Default.History),
                FunctionKeyData(PosAction.OPEN_DRAWER, "Cash Drawer", Icons.Default.CreditCard),
                FunctionKeyData(PosAction.SHIFT, "Shift", Icons.Default.Schedule),
                FunctionKeyData(PosAction.PAY, "Pay", Icons.Default.Payments, containerColor = StitchColor.Primary, contentColor = Color.White)
            )
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val isTablet = maxWidth > 900.dp
            
            Row(modifier = Modifier.fillMaxSize()) {
                if (isTablet) {
                    // Left Column: Categories
                    Surface(
                        modifier = Modifier.width(180.dp).fillMaxHeight(),
                        color = StitchColor.SurfaceContainerLowest,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    "CATEGORIES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(8.dp),
                                    color = StitchColor.OnSurfaceVariant
                                )
                            }
                            item {
                                CategoryItem(
                                    name = "ALL ITEMS",
                                    isSelected = uiState.selectedCategoryId == null,
                                    onClick = { viewModel.selectCategory(null) }
                                )
                            }
                            items(uiState.categories) { category ->
                                CategoryItem(
                                    name = category.name,
                                    isSelected = uiState.selectedCategoryId == category.id,
                                    onClick = { viewModel.selectCategory(category.id) }
                                )
                            }
                        }
                    }
                }

                // Middle Column: Products
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // Quick Search
                    StitchTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        label = "Quick Search",
                        placeholder = "SEARCH PRODUCTS...",
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .focusRequester(searchFocusRequester),
                        trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        } else null
                    )

                    if (!isTablet) {
                        // Category Filters (Horizontal Row for mobile/small screens)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                StitchButton(
                                    text = "ALL ITEMS",
                                    onClick = { viewModel.selectCategory(null) },
                                    containerColor = if (uiState.selectedCategoryId == null) StitchColor.Primary else StitchColor.SurfaceContainerLow,
                                    contentColor = if (uiState.selectedCategoryId == null) StitchColor.OnPrimary else StitchColor.OnSurfaceVariant
                                )
                            }
                            items(
                                uiState.categories,
                                key = { it.id }
                            ) { category ->
                                val isSelected = uiState.selectedCategoryId == category.id
                                StitchButton(
                                    text = category.name,
                                    onClick = { viewModel.selectCategory(category.id) },
                                    containerColor = if (isSelected) StitchColor.Primary else StitchColor.SurfaceContainerLow,
                                    contentColor = if (isSelected) StitchColor.OnPrimary else StitchColor.OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Product Grid
                    val isCompact = this@BoxWithConstraints.maxWidth < 800.dp
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (isCompact) 140.dp else 160.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            uiState.filteredProducts,
                            key = { it.id }
                        ) { product ->
                            val cartItem = uiState.cartItems.find { it.product.id == product.id }
                            StitchProductCard(
                                product = product,
                                isSelected = cartItem != null,
                                selectedQuantity = cartItem?.quantity ?: java.math.BigDecimal.ZERO,
                                compact = isCompact,
                                onClick = { viewModel.addToCart(product) }
                            )
                        }
                    }

                    // Function Keys Grid at bottom
                    Spacer(Modifier.height(16.dp))
                    StitchFunctionGrid(
                        actions = functionKeys,
                        onActionClick = { viewModel.onPosAction(it) }
                    )
                }
            }
        }

        // Checkout Sidebar
        StitchCartSidebar(
            cartItems = uiState.cartItems,
            subtotal = uiState.subtotal,
            taxAmount = uiState.totalTax,
            rounding = uiState.roundingAdjustment,
            total = uiState.totalAmount,
            isCollapsed = isSidebarCollapsed,
            onToggleCollapse = { isSidebarCollapsed = !isSidebarCollapsed },
            onUpdateQuantity = { item, qty -> viewModel.updateQuantity(item, qty) },
            onRemoveItem = { item -> viewModel.removeFromCart(item) },
            onPay = { viewModel.completeSale("OPEN_DIALOG") },
            onHold = { viewModel.saveOrder() },
            onVoid = { viewModel.clearCartWithConfirm() },
            onReprint = { viewModel.onPosAction(PosAction.REPRINT_LAST) },
            onDiscount = { viewModel.onPosAction(PosAction.DISCOUNT) }
        )
    }

    // Dialogs
    if (uiState.showConfirmClearCart) {
        ConfirmClearCartDialog(
            itemCount = uiState.cartItems.size,
            onConfirm = { viewModel.executeClearCart() },
            onDismiss = { viewModel.cancelClearCart() }
        )
    }

    if (uiState.showDiscountDialog) {
        DiscountDialog(
            initialDiscount = uiState.itemAwaitingDiscount?.discount ?: uiState.cartDiscount,
            onApply = { viewModel.applyDiscount(it) },
            onDismiss = { viewModel.dismissDiscountDialog() }
        )
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

    if (uiState.showPaymentSuccess) {
        OrderSuccessDialog(
            uiState = uiState,
            onDismiss = { viewModel.dismissPaymentSuccess() },
            onReprint = { viewModel.reprintLastReceipt() }
        )
    }

    if (uiState.showTerminalProgress) {
        TerminalProgressDialog(
            status = uiState.terminalStatus,
            totalAmount = uiState.totalAmount
        )
    }

    if (uiState.itemAwaitingModifiers != null) {
        ModifierDialog(
            item = uiState.itemAwaitingModifiers!!,
            uiState = uiState,
            onToggleModifier = { viewModel.toggleModifier(it) },
            onToggleFnbModifier = { viewModel.toggleFnbModifier(it) },
            onDismiss = { viewModel.dismissModifierSelection() }
        )
    }

    if (uiState.showMemberSelection) {
        MemberSelectionDialog(
            onDismiss = { viewModel.setShowMemberSelection(false) },
            onMemberSelected = { member ->
                viewModel.selectMember(member)
                viewModel.setShowMemberSelection(false)
            }
        )
    }

    if (uiState.showAdminAuthDialog) {
        PinAuthorizationDialog(
            permission = when (uiState.adminAuthAction) {
                is AdminAuthAction.RemoveItem -> com.extrotarget.extroposv2.core.security.Permission.VOID_SALE
                is AdminAuthAction.ApplyDiscount -> com.extrotarget.extroposv2.core.security.Permission.APPLY_DISCOUNT
                is AdminAuthAction.OpenDrawer -> com.extrotarget.extroposv2.core.security.Permission.CASH_DRAWER_OPEN
                else -> com.extrotarget.extroposv2.core.security.Permission.ACCESS_MAINTENANCE
            },
            errorMessage = uiState.adminAuthError,
            onConfirm = { viewModel.authenticateAdmin(it) },
            onDismiss = { viewModel.dismissAdminAuth() }
        )
    }
}

@Composable
fun CategoryItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) StitchColor.Primary else Color.Transparent,
        contentColor = if (isSelected) StitchColor.OnPrimary else StitchColor.OnSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}
