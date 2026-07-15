package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps
import com.extrotarget.extroposv2.ui.theme.priceLarge
import java.math.BigDecimal

@Composable
fun StitchCartSidebar(
    cartItems: List<CartItem>,
    subtotal: BigDecimal,
    taxAmount: BigDecimal,
    rounding: BigDecimal,
    total: BigDecimal,
    onUpdateQuantity: (CartItem, BigDecimal) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onPay: () -> Unit,
    onHold: () -> Unit = {},
    onVoid: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(420.dp)
            .fillMaxHeight(),
        color = StitchColor.SurfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "ORDER SUMMARY",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(
                        "Register 01 • Table 12",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StitchColor.OnSurfaceVariant
                        )
                    )
                }
                
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
            }
            
            Divider(color = StitchColor.OutlineVariant)

            // Cart Items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(StitchColor.Surface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    CartListItem(
                        item = item,
                        onUpdateQuantity = { onUpdateQuantity(item, it) },
                        onRemove = { onRemoveItem(item) }
                    )
                }
            }

            // Totals & Pay
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = StitchColor.SurfaceContainerLowest,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryRow("Subtotal", "RM $subtotal")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("SST (6%)", style = MaterialTheme.typography.bodySmall)
                            Surface(
                                color = StitchColor.Tertiary,
                                shape = CircleShape
                            ) {
                                Text(
                                    "TAX",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelCaps.copy(fontSize = 9.sp, color = Color.White)
                                )
                            }
                        }
                        Text("RM $taxAmount", style = MaterialTheme.typography.bodyLarge)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("BNM Rounding", style = MaterialTheme.typography.bodySmall)
                            Surface(
                                color = StitchColor.BNM,
                                shape = CircleShape
                            ) {
                                Text(
                                    "BNM",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelCaps.copy(fontSize = 9.sp, color = Color.White)
                                )
                            }
                        }
                        Text("RM $rounding", style = MaterialTheme.typography.bodyLarge)
                    }

                    Divider(color = StitchColor.OutlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "TOTAL",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            "RM $total",
                            style = MaterialTheme.typography.priceLarge.copy(color = StitchColor.Primary)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = onPay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StitchColor.Primary)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "PAY RM $total",
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onHold,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
                        ) {
                            Text("HOLD", style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant))
                        }
                        OutlinedButton(
                            onClick = onVoid,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
                        ) {
                            Text("VOID", style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.Error))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartListItem(
    item: CartItem,
    onUpdateQuantity: (BigDecimal) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = StitchColor.SurfaceContainerLowest,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name.uppercase(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    "RM ${item.product.price}",
                    style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.Outline)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quantity Capsule
                Surface(
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (item.quantity > BigDecimal.ONE) onUpdateQuantity(item.quantity - BigDecimal.ONE) else onRemove() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            item.quantity.stripTrailingZeros().toPlainString(),
                            modifier = Modifier.width(32.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        IconButton(
                            onClick = { onUpdateQuantity(item.quantity + BigDecimal.ONE) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Text(
                    "RM ${item.product.price.multiply(item.quantity)}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(80.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = StitchColor.OnSurfaceVariant))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
    }
}
