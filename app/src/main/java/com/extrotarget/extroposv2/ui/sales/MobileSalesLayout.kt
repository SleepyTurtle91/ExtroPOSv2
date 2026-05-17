package com.extrotarget.extroposv2.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.extrotarget.extroposv2.ui.sales.components.PosContentGrid
import com.extrotarget.extroposv2.ui.sales.components.SaleHeader
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import java.util.*

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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryChip(
                            name = "All",
                            isSelected = uiState.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) }
                        )
                    }
                    items(uiState.categories) { category ->
                        CategoryChip(
                            name = category.name,
                            isSelected = uiState.selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            MobileBottomSummary(
                uiState = uiState,
                onClick = { /* Could show cart modal or navigate to cart screen */ }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            PosContentGrid(
                uiState = uiState,
                onProductClick = { viewModel.addToCart(it) },
                onSelectCategory = { viewModel.selectCategory(it) }
            )
        }
    }

    if (uiState.showCameraScanner) {
        Dialog(
            onDismissRequest = { viewModel.toggleCameraScanner(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                BarcodeScannerView(
                    onBarcodeDetected = { barcode ->
                        viewModel.onBarcodeScanned(barcode)
                        viewModel.toggleCameraScanner(false)
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Close button overlay
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = { viewModel.toggleCameraScanner(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color(0xFF475569),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MobileBottomSummary(
    uiState: SalesUiState,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() },
        color = Color(0xFF0F172A),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                Text(
                    "${uiState.cartItems.size} ITEMS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
            
            Text(
                CurrencyUtils.format(uiState.totalAmount),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}
