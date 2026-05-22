package com.extrotarget.extroposv2.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.components.barcode.BarcodeScannerView
import com.extrotarget.extroposv2.ui.sales.components.CartSidebar
import com.extrotarget.extroposv2.ui.sales.components.PosContentGrid
import com.extrotarget.extroposv2.ui.sales.components.SaleHeader
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileSalesLayout(
    modifier: Modifier = Modifier,
    uiState: SalesUiState,
    currentTime: Date,
    activeMode: BusinessMode,
    sessionManager: SessionManager,
    viewModel: SalesViewModel,
    onNavigateToShift: () -> Unit
) {
    var showCartSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                SaleHeader(
                    activeMode = activeMode,
                    uiState = uiState,
                    currentTime = currentTime,
                    syncStatus = uiState.syncStatus,
                    sessionManager = sessionManager,
                    onOpenShift = onNavigateToShift,
                    onOpenDrawer = { viewModel.openDrawer() },
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onScanBarcode = { viewModel.toggleCameraScanner(true) }
                )
                
                // Horizontal Category Bar
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("ALL", fontWeight = FontWeight.Black, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name.uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick cart toggle / summary
                    OutlinedButton(
                        onClick = { showCartSheet = true },
                        modifier = Modifier.weight(0.4f).height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.cartItems.isNotEmpty()) {
                                    Badge { Text(uiState.cartItems.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "View Cart")
                        }
                    }

                    // Main Pay Button
                    Button(
                        onClick = { 
                            if (activeMode.hasTables) {
                                viewModel.sendToKitchen()
                            } else {
                                viewModel.completeSale("OPEN_DIALOG") 
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeMode.hasTables) Color(0xFF475569) else Color(0xFF3B82F6)
                        ),
                        enabled = uiState.cartItems.isNotEmpty()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (activeMode.hasTables) "SAVE ORDER" else "PAY",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                CurrencyUtils.format(uiState.totalAmountCash),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
        ) {
            PosContentGrid(
                uiState = uiState,
                onProductClick = { viewModel.addToCart(it) },
                onSelectCategory = { viewModel.selectCategory(it) }
            )
        }
    }

    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.8f)) {
                CartSidebar(
                    uiState = uiState,
                    onUpdateQuantity = { item, qty -> viewModel.updateQuantity(item, qty) },
                    onShowModifiers = { viewModel.showModifierSelection(it) },
                    onRemoveFromCart = { viewModel.removeFromCart(it) },
                    onClearCart = { viewModel.clearCartWithConfirm() },
                    onSendToKitchen = { 
                        viewModel.sendToKitchen()
                        showCartSheet = false
                    },
                    onCompleteSale = { 
                        viewModel.completeSale(it)
                        showCartSheet = false
                    },
                    onAddCustomer = { viewModel.setShowMemberSelection(true) },
                    onRedeemPoints = { viewModel.setRedeemedPoints(it) }
                )
            }
        }
    }

    if (uiState.showCameraScanner) {
        Dialog(
            onDismissRequest = { viewModel.toggleCameraScanner(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box {
                    BarcodeScannerView(
                        onBarcodeDetected = { barcode ->
                            viewModel.onBarcodeScanned(barcode)
                        }
                    )
                    
                    IconButton(
                        onClick = { viewModel.toggleCameraScanner(false) },
                        modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}
