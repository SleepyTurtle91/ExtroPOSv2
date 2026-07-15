package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.sales.SalesUiState
import com.extrotarget.extroposv2.ui.theme.*
import java.math.BigDecimal

@Composable
fun CartSidebar(
    uiState: SalesUiState,
    onUpdateQuantity: (CartItem, BigDecimal) -> Unit,
    onShowModifiers: (CartItem) -> Unit,
    onRemoveFromCart: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onSendToKitchen: () -> Unit,
    onCompleteSale: (String) -> Unit,
    onAddCustomer: () -> Unit = {},
    onRedeemPoints: (BigDecimal) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .width(420.dp) // Fixed sidebar width as per DESIGN.md
            .fillMaxHeight(),
        color = SurfaceLevel1,
        border = BorderStroke(1.dp, SurfaceOutline)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High-Density Order Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ORDER SUMMARY",
                        style = TitleMedium,
                        fontWeight = FontWeight.Black,
                        color = HighDensityOnSurface
                    )
                    Text(
                        "Register 01 • Table ${uiState.activeMode.name}",
                        style = BodySmall,
                        color = HighDensityOnSurface.copy(alpha = 0.6f)
                    )
                }
                
                IconButton(
                    onClick = onClearCart,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, SurfaceOutline, RoundedCornerShape(4.dp))
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = HighDensityOnSurface)
                }
            }

            HorizontalDivider(color = SurfaceOutline)

            // Cart Items List
            LazyColumn(
                modifier = Modifier.weight(1f).background(SurfaceLevel0),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                if (uiState.cartItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(64.dp), tint = SurfaceOutline)
                            Spacer(Modifier.height(16.dp))
                            Text("NO ITEMS IN CART", style = LabelCaps, color = HighDensityOutline)
                        }
                    }
                } else {
                    items(uiState.cartItems) { item ->
                        CartItemTile(
                            item = item,
                            onUpdateQty = { onUpdateQuantity(item, it) },
                            onShowModifiers = { onShowModifiers(item) }
                        )
                    }
                }
            }

            // High-Density Footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceLevel1,
                border = BorderStroke(1.dp, SurfaceOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Summary Rows
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("SUBTOTAL", CurrencyUtils.format(uiState.subtotal))
                        
                        if (uiState.totalTax > BigDecimal.ZERO) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("SST (6%)", style = BodySmall, color = HighDensityOnSurface.copy(alpha = 0.6f))
                                    Surface(color = HighDensityTertiary, shape = RoundedCornerShape(99.dp)) {
                                        Text("TAX", style = LabelCaps, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text(CurrencyUtils.format(uiState.totalTax), style = BodyBase, color = HighDensityOnSurface)
                            }
                        }

                        if (uiState.roundingAdjustment != BigDecimal.ZERO) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("BNM ROUNDING", style = BodySmall, color = HighDensityOnSurface.copy(alpha = 0.6f))
                                    Surface(color = PosCyan, shape = RoundedCornerShape(99.dp)) {
                                        Text("BNM", style = LabelCaps, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text(CurrencyUtils.format(uiState.roundingAdjustment), style = BodyBase, color = HighDensityOnSurface)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceOutline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("TOTAL", style = TitleMedium, fontWeight = FontWeight.Black)
                        Text(
                            CurrencyUtils.format(uiState.totalAmountCash),
                            style = PriceLarge,
                            color = HighDensityPrimary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action Buttons
                    Button(
                        onClick = { onCompleteSale("OPEN_DIALOG") },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        enabled = uiState.cartItems.isNotEmpty()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Payments, contentDescription = null)
                            Text("PAY ${CurrencyUtils.format(uiState.totalAmountCash)}", style = TitleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { /* Hold logic */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, SurfaceOutline)
                        ) {
                            Text("HOLD", style = LabelCaps, color = HighDensityOnSurface)
                        }
                        OutlinedButton(
                            onClick = onClearCart,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HighDensityError.copy(alpha = 0.2f))
                        ) {
                            Text("VOID", style = LabelCaps, color = HighDensityError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemTile(
    item: CartItem,
    onUpdateQty: (BigDecimal) -> Unit,
    onShowModifiers: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowModifiers() },
        color = SurfaceLevel1,
        border = BorderStroke(1.dp, SurfaceOutline),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(HighDensitySecondary, RoundedCornerShape(99.dp))
            )
            
            Spacer(Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name.uppercase(),
                    style = BodyBase,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    CurrencyUtils.format(item.unitPrice),
                    style = LabelCaps,
                    color = HighDensityOutline
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Stepper
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .border(1.dp, SurfaceOutline, RoundedCornerShape(4.dp))
                        .background(SurfaceLevel1),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (item.quantity > BigDecimal.ONE) onUpdateQty(item.quantity.subtract(BigDecimal.ONE)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        item.quantity.stripTrailingZeros().toPlainString(),
                        style = BodyBase,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onUpdateQty(item.quantity.add(BigDecimal.ONE)) },
                        modifier = Modifier.size(32.dp).border(BorderStroke(1.dp, SurfaceOutline), RoundedCornerShape(0.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
                
                Text(
                    CurrencyUtils.format(item.totalPrice),
                    style = TitleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(80.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = BodySmall, color = HighDensityOnSurface.copy(alpha = 0.6f))
        Text(value, style = BodyBase, color = HighDensityOnSurface)
    }
}
