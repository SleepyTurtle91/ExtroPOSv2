package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.sales.SalesUiState
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierDialog(
    item: CartItem,
    availableModifiers: List<String>,
    onToggleModifier: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "SELECT MODIFIERS",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                item.product.name.uppercase(),
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        availableModifiers.forEach { modifier ->
                            val isSelected = item.modifiers.contains(modifier)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleModifier(modifier) },
                                label = { 
                                    Text(
                                        modifier, 
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                    ) 
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF8FAFC),
                                    labelColor = Color(0xFF64748B)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color(0xFFE2E8F0),
                                    selectedBorderColor = Color(0xFF3B82F6),
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.dp,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("SAVE SELECTION", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun TerminalProgressDialog(status: String?, totalAmount: BigDecimal) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("Processing Card Payment") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(status ?: "Communicating with terminal...", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("Total: ${CurrencyUtils.format(totalAmount)}", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessDialog(
    uiState: SalesUiState,
    onDismiss: () -> Unit,
    onReprint: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .background(Color(0xFFF0FDF4))
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            color = Color.White,
                            shape = CircleShape,
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF10B981))
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text("ORDER PAID", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color(0xFF065F46), letterSpacing = (-1).sp)
                        Text("TRANSACTION SUCCESSFUL", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                        
                        Spacer(Modifier.height(48.dp))
                        
                        if (uiState.lastSaleQrContent != null) {
                            Surface(
                                modifier = Modifier.size(160.dp),
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                    com.extrotarget.extroposv2.ui.components.qr.QrCodeView(content = uiState.lastSaleQrContent!!, modifier = Modifier.fillMaxSize())
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("SCAN FOR E-RECEIPT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                        }
                    }

                    Column(modifier = Modifier.weight(0.6f).padding(40.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text("TICKET SUMMARY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("#${uiState.lastSaleId?.takeLast(8)?.uppercase() ?: "PENDING"}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0F172A))
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SummaryDetailRow("SUBTOTAL", CurrencyUtils.format(uiState.subtotal))
                            SummaryDetailRow("TAX (SST 6%)", CurrencyUtils.format(uiState.totalTax))
                            if (uiState.totalDiscount > BigDecimal.ZERO) {
                                SummaryDetailRow("DISCOUNTS", "-${CurrencyUtils.format(uiState.totalDiscount)}", valueColor = Color(0xFFEF4444))
                            }
                            if (uiState.roundingAdjustment != BigDecimal.ZERO) {
                                SummaryDetailRow("ROUNDING", CurrencyUtils.format(uiState.roundingAdjustment))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TOTAL PAID", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Text(CurrencyUtils.format(uiState.totalAmountCash), fontWeight = FontWeight.Black, fontSize = 36.sp, color = Color(0xFF3B82F6), letterSpacing = (-1).sp)
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(onClick = onReprint, modifier = Modifier.height(64.dp).weight(1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(2.dp, Color(0xFFE2E8F0))) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("PRINT RECEIPT", fontWeight = FontWeight.Black)
                            }
                            Button(onClick = onDismiss, modifier = Modifier.height(64.dp).weight(1f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))) {
                                Text("NEXT ORDER", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SummaryDetailRow(label: String, value: String, valueColor: Color = Color(0xFF0F172A)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}
