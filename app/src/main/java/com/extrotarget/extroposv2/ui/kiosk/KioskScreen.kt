package com.extrotarget.extroposv2.ui.kiosk

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.ui.kiosk.viewmodel.KioskViewModel
import com.extrotarget.extroposv2.ui.kiosk.viewmodel.KioskUiState
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal

@Composable
fun KioskScreen(
    viewModel: KioskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (uiState.isAttractMode) {
            AttractMode(onStart = viewModel::startOrdering)
        } else if (uiState.orderSuccess != null) {
            OrderSuccessScreen(orderId = uiState.orderSuccess!!)
        } else {
            KioskMainLayout(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
fun AttractMode(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onStart)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "Welcome to ExtroPOS",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "TAP ANYWHERE TO START",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KioskMainLayout(uiState: KioskUiState, viewModel: KioskViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Self-Ordering",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = viewModel::cancelOrder) {
                    Text("CANCEL", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Categories Sidebar (Left)
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFF8FAFC))
            ) {
                uiState.categories.forEach { category ->
                    val isSelected = uiState.selectedCategoryId == category.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { viewModel.selectCategory(category.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF3B82F6) else Color.Gray
                            )
                            Text(
                                category.name,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF3B82F6) else Color.Gray
                            )
                        }
                    }
                }
            }

            // Products Grid
            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                val filteredProducts = uiState.products.filter { 
                    uiState.selectedCategoryId == null || it.categoryId == uiState.selectedCategoryId 
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductKioskCard(product = product, onClick = { viewModel.addToCart(product) })
                    }
                }
            }

            // Cart Summary (Right)
            Surface(
                modifier = Modifier.width(320.dp).fillMaxHeight(),
                color = Color(0xFFF1F5F9),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Your Order", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        uiState.cartItems.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.product.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.updateQuantity(item, BigDecimal("-1")) }) {
                                        Icon(Icons.Default.Remove, contentDescription = null)
                                    }
                                    Text("${item.quantity.toInt()}", fontWeight = FontWeight.Black)
                                    IconButton(onClick = { viewModel.updateQuantity(item, BigDecimal("1")) }) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                    Row {
                        Text("Total", fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Text(CurrencyUtils.format(uiState.subtotal), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B82F6))
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = viewModel::checkout,
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        enabled = uiState.cartItems.isNotEmpty() && !uiState.isProcessing
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text("ORDER NOW", fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductKioskCard(product: com.extrotarget.extroposv2.core.data.model.Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFFE2E8F0))) {
                // Image placeholder
                Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(64.dp), tint = Color.LightGray)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
                Text(CurrencyUtils.format(product.price), color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OrderSuccessScreen(orderId: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10B981)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(32.dp))
            Text("THANK YOU!", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            Text("Your Order Number is", color = Color.White.copy(alpha = 0.8f), fontSize = 20.sp)
            Text("#$orderId", color = Color.White, fontSize = 80.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(32.dp))
            Text("Please proceed to the counter for payment.", color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
        }
    }
}
