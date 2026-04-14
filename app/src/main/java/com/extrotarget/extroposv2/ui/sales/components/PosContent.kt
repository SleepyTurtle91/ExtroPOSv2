package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.ui.components.ProductCard
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.SalesUiState

@Composable
fun PosContent(
    uiState: SalesUiState,
    onSearchQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onUpdateQuantity: (com.extrotarget.extroposv2.ui.sales.CartItem, java.math.BigDecimal) -> Unit,
    onShowModifiers: (com.extrotarget.extroposv2.ui.sales.CartItem) -> Unit,
    onRemoveFromCart: (com.extrotarget.extroposv2.ui.sales.CartItem) -> Unit,
    onClearCart: () -> Unit,
    onSendToKitchen: () -> Unit,
    onCompleteSale: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMode = uiState.activeMode

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                placeholder = {
                    Text(
                        "SEARCH ${activeMode.displayName.uppercase()}...",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp)) },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF3B82F6)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { onProductClick(product) }
                    )
                }
            }
        }

        CartSidebar(
            uiState = uiState,
            onUpdateQuantity = onUpdateQuantity,
            onShowModifiers = onShowModifiers,
            onRemoveFromCart = onRemoveFromCart,
            onClearCart = onClearCart,
            onSendToKitchen = onSendToKitchen,
            onCompleteSale = onCompleteSale
        )
    }
}
