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
import androidx.compose.runtime.*
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
    onReprint: () -> Unit = {},
    onDiscount: () -> Unit = {},
    modifier: Modifier = Modifier,
    isCollapsed: Boolean = false,
    onToggleCollapse: () -> Unit = {}
) {
    if (isCollapsed) {
        Surface(
            modifier = modifier
                .width(48.dp)
                .fillMaxHeight(),
            color = StitchColor.SurfaceContainerLowest,
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(top = 16.dp)) {
                IconButton(onClick = onToggleCollapse) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Expand", tint = StitchColor.Primary)
                }
            }
        }
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
    ) {
        val sidebarWidth = when {
            this.maxWidth < 800.dp -> 320.dp
            this.maxWidth < 1200.dp -> 360.dp
            else -> 420.dp
        }
        val isCompact = sidebarWidth < 360.dp

        Surface(
            modifier = Modifier.width(sidebarWidth),
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
                    var showMenu by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleCollapse) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Collapse", tint = StitchColor.Outline)
                        }
                        Column {
                            Text(
                                "ORDER SUMMARY",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = if (isCompact) 14.sp else 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            if (!isCompact) {
                                Text(
                                    "Register 01 • Table 12",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = StitchColor.OnSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Cart Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear Cart") },
                                onClick = { 
                                    onVoid()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Apply Cart Discount") },
                                onClick = { 
                                    onDiscount()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Reprint Last Receipt") },
                                onClick = { 
                                    onReprint()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = StitchColor.OutlineVariant)

                // Cart Items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(StitchColor.Surface)
                        .padding(if (isCompact) 8.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems) { item ->
                        CartListItem(
                            item = item,
                            isCompact = isCompact,
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
                        modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
                    ) {
                        SummaryRow("Subtotal", "RM $subtotal", isCompact)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("SST (6%)", style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isCompact) 11.sp else 12.sp))
                                if (!isCompact) {
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
                            }
                            Text("RM $taxAmount", style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isCompact) 14.sp else 16.sp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("BNM Rounding", style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isCompact) 11.sp else 12.sp))
                            }
                            Text("RM $rounding", style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isCompact) 14.sp else 16.sp))
                        }

                        HorizontalDivider(color = StitchColor.OutlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                "TOTAL",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isCompact) 18.sp else 22.sp
                                )
                            )
                            Text(
                                "RM $total",
                                style = if (isCompact) MaterialTheme.typography.headlineMedium.copy(color = StitchColor.Primary, fontWeight = FontWeight.Black) else MaterialTheme.typography.priceLarge.copy(color = StitchColor.Primary)
                            )
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
    isCompact: Boolean,
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
            modifier = Modifier.padding(if (isCompact) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name.uppercase(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompact) 12.sp else 14.sp
                    ),
                    maxLines = 1
                )
                Text(
                    "RM ${item.product.price}",
                    style = MaterialTheme.typography.labelCaps.copy(
                        color = StitchColor.Outline,
                        fontSize = if (isCompact) 9.sp else 10.sp
                    )
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
            ) {
                // Quantity Capsule
                Surface(
                    modifier = Modifier.height(if (isCompact) 28.dp else 32.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (item.quantity > BigDecimal.ONE) onUpdateQuantity(item.quantity - BigDecimal.ONE) else onRemove() },
                            modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(if (isCompact) 14.dp else 16.dp))
                        }
                        Text(
                            item.quantity.stripTrailingZeros().toPlainString(),
                            modifier = Modifier.width(if (isCompact) 24.dp else 32.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompact) 12.sp else 14.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        IconButton(
                            onClick = { onUpdateQuantity(item.quantity + BigDecimal.ONE) },
                            modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(if (isCompact) 14.dp else 16.dp))
                        }
                    }
                }

                Text(
                    "RM ${item.product.price.multiply(item.quantity)}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = if (isCompact) 13.sp else 16.sp, 
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.width(if (isCompact) 60.dp else 80.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isCompact: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(
            color = StitchColor.OnSurfaceVariant,
            fontSize = if (isCompact) 11.sp else 12.sp
        ))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = if (isCompact) 14.sp else 16.sp
        ))
    }
}
