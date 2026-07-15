package com.extrotarget.extroposv2.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.ui.components.stitch.StitchCartSidebar
import com.extrotarget.extroposv2.ui.components.stitch.StitchProductCard
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import com.extrotarget.extroposv2.ui.theme.StitchColor
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchButton
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchOutlinedButton

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    sessionManager: SessionManager,
    onNavigateToShift: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Main Product Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Category Filters
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
                items(uiState.categories) { category ->
                    val isSelected = uiState.selectedCategoryId == category.id
                    StitchButton(
                        text = category.name,
                        onClick = { viewModel.selectCategory(category.id) },
                        containerColor = if (isSelected) StitchColor.Primary else StitchColor.SurfaceContainerLow,
                        contentColor = if (isSelected) StitchColor.OnPrimary else StitchColor.OnSurfaceVariant
                    )
                }
            }

            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredProducts) { product ->
                    val cartItem = uiState.cartItems.find { it.product.id == product.id }
                    StitchProductCard(
                        product = product,
                        isSelected = cartItem != null,
                        selectedQuantity = cartItem?.quantity ?: java.math.BigDecimal.ZERO,
                        onClick = { viewModel.addToCart(product) }
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
            onUpdateQuantity = { item, qty -> viewModel.updateQuantity(item, qty) },
            onRemoveItem = { item -> viewModel.removeFromCart(item) },
            onPay = { viewModel.completeSale("CASH") },
            onHold = { viewModel.saveOrder() },
            onVoid = { viewModel.clearCartWithConfirm() }
        )
    }
}
