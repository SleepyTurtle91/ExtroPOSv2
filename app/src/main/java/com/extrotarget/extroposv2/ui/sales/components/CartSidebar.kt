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
            .width(420.dp)
            .fillMaxHeight(),
        color = Color.White,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cart Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.sales_cart).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        "TICKET #00${System.currentTimeMillis() / 1000000}",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onAddCustomer,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (uiState.selectedMember != null) Color(0xFFEFF6FF) else Color.White,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (uiState.selectedMember != null) Color(0xFF3B82F6) else Color(0xFFE2E8F0),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            if (uiState.selectedMember != null) Icons.Default.Person else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = if (uiState.selectedMember != null) Color(0xFF3B82F6) else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onClearCart,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Cart Items
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (uiState.cartItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFE2E8F0))
                            Spacer(Modifier.height(16.dp))
                            Text("NO ITEMS ADDED", color = Color(0xFF94A3B8), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                        }
                    }
                } else {
                    items(uiState.cartItems) { item ->
                        CartItemTile(
                            item = item,
                            activeColor = uiState.activeMode.color,
                            onUpdateQty = { onUpdateQuantity(item, it) },
                            onShowModifiers = { onShowModifiers(item) },
                            onRemove = { onRemoveFromCart(item) }
                        )
                    }
                }
            }

            // Footer / Payment Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (uiState.selectedMember != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Column {
                                        Text(uiState.selectedMember.name.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                                        Text("${uiState.selectedMember.totalPoints.toInt()} pts", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                IconButton(onClick = { onAddCustomer() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Change", modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                                }
                            }
                            if (uiState.selectedMember.totalPoints >= BigDecimal("100")) {
                                Button(
                                    onClick = { onRedeemPoints(if (uiState.redeemedPoints > BigDecimal.ZERO) BigDecimal.ZERO else uiState.selectedMember.totalPoints) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.redeemedPoints > BigDecimal.ZERO) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        if (uiState.redeemedPoints > BigDecimal.ZERO) "REDEEMED RM${uiState.redeemedAmount}" else "REDEEM POINTS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        SummaryRow(stringResource(R.string.sales_subtotal).uppercase(), CurrencyUtils.format(uiState.subtotal))
                        if (uiState.taxConfig?.isTaxEnabled == true || uiState.totalTax > BigDecimal.ZERO) {
                            val taxName = uiState.taxConfig?.taxName ?: stringResource(R.string.sales_tax)
                            val taxRate = uiState.taxConfig?.defaultTaxRate?.stripTrailingZeros()?.toPlainString() ?: "0"
                            val taxLabel = "${taxName.uppercase()} (${taxRate}%)"
                            SummaryRow(taxLabel, CurrencyUtils.format(uiState.totalTax), valueColor = Color(0xFF10B981))
                        }
                        if (uiState.totalDiscount > BigDecimal.ZERO) {
                            SummaryRow(stringResource(R.string.sales_discount).uppercase(), "-${CurrencyUtils.format(uiState.totalDiscount)}", valueColor = Color(0xFFEF4444))
                        }
                        if (uiState.totalServiceCharge > BigDecimal.ZERO) {
                            val scRate = uiState.taxConfig?.serviceChargeRate?.stripTrailingZeros()?.toPlainString() ?: "0"
                            val scLabel = "SERVICE CHARGE (${scRate}%)"
                            SummaryRow(scLabel, CurrencyUtils.format(uiState.totalServiceCharge))
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color(0xFFE2E8F0))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.sales_total_payable).uppercase(), color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text(
                            CurrencyUtils.format(uiState.totalAmount),
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            letterSpacing = (-1).sp
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (uiState.activeMode.hasTables) {
                            val allSaved = uiState.cartItems.isNotEmpty() && uiState.cartItems.all { it.isSentToKitchen }
                            Button(
                                onClick = onSendToKitchen,
                                modifier = Modifier.weight(0.4f).height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (allSaved) Color(0xFF10B981) else Color(0xFF475569)
                                ),
                                enabled = uiState.cartItems.isNotEmpty()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        if (allSaved) Icons.Default.CheckCircle else Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        if (allSaved) "SENT" else "SEND",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = { onCompleteSale("OPEN_DIALOG") },
                            modifier = Modifier.weight(1f).height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            enabled = uiState.cartItems.isNotEmpty()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(24.dp))
                                Text(stringResource(R.string.sales_pay).uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
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
    activeColor: Color,
    onUpdateQty: (BigDecimal) -> Unit,
    onShowModifiers: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF64748B).copy(alpha = 0.1f),
                spotColor = Color(0xFF64748B).copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable { onShowModifiers() },
        color = Color(0xFFF8FAFC)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.product.name.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            CurrencyUtils.format(item.unitPrice),
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        if (item.assignedStaffName != null) {
                            Text(
                                "• ${item.assignedStaffName}",
                                color = activeColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }
                    if (item.selectedModifiers.isNotEmpty()) {
                        Text(
                            item.selectedModifiers.joinToString(", ") { it.name }.uppercase(),
                            color = activeColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Text(
                    CurrencyUtils.format(item.totalPrice),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFEF4444).copy(alpha = 0.5f)
                    )
                }

                Row(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onUpdateQty(item.quantity.subtract(BigDecimal.ONE)) },
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                    }
                    Text(
                        item.quantity.stripTrailingZeros().toPlainString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.widthIn(min = 20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(
                        onClick = { onUpdateQty(item.quantity.add(BigDecimal.ONE)) },
                        modifier = Modifier
                            .size(28.dp)
                            .background(activeColor, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = Color(0xFF0F172A)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}
