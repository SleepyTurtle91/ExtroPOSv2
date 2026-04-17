package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.sales.SalesUiState
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierDialog(
    item: CartItem,
    availableModifiers: List<com.extrotarget.extroposv2.core.data.model.Modifier>,
    onToggleModifier: (com.extrotarget.extroposv2.core.data.model.Modifier) -> Unit,
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
                                stringResource(R.string.sales_select_modifiers).uppercase(),
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
                            val isSelected = item.selectedModifiers.any { it.id == modifier.id }
                            val isAvailable = modifier.isAvailable
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleModifier(modifier) },
                                enabled = isAvailable,
                                label = { 
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            modifier.name, 
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                        if (modifier.priceAdjustment > BigDecimal.ZERO) {
                                            Text(
                                                "+RM ${modifier.priceAdjustment}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else if (isAvailable) Color(0xFFF59E0B) else Color(0xFFCBD5E1)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF8FAFC),
                                    labelColor = Color(0xFF64748B),
                                    disabledContainerColor = Color(0xFFF1F5F9),
                                    disabledLabelColor = Color(0xFF94A3B8)
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
                        Text(stringResource(R.string.sales_save_selection).uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashReceivedDialog(
    totalAmount: BigDecimal,
    onConfirm: (BigDecimal) -> Unit,
    onDismiss: () -> Unit
) {
    var receivedText by remember { mutableStateOf("") }
    val receivedAmount = receivedText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val change = receivedAmount.subtract(totalAmount).max(BigDecimal.ZERO)
    val isEnough = receivedAmount >= totalAmount

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
                    Text(
                        stringResource(R.string.sales_cash_received).uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${stringResource(R.string.sales_total_payable).uppercase()}: ${CurrencyUtils.format(totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = receivedText,
                        onValueChange = { newValue -> 
                            if (newValue.isEmpty() || newValue.toBigDecimalOrNull() != null) {
                                receivedText = newValue 
                            }
                        },
                        label = { Text(stringResource(R.string.sales_amount_received)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        prefix = { Text("${CurrencyUtils.getCurrencySymbol()} ") },
                        singleLine = true,
                        textStyle = TextStyle(fontWeight = FontWeight.Black, fontSize = 24.sp)
                    )

                    Spacer(Modifier.height(24.dp))

                    if (isEnough && receivedAmount > totalAmount) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.sales_change_to_give).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                Text(
                                    CurrencyUtils.format(change),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val quickAmounts = listOf(
                            totalAmount,
                            totalAmount.setScale(0, RoundingMode.UP),
                            BigDecimal("10"), BigDecimal("20"), BigDecimal("50"), BigDecimal("100")
                        ).filter { it >= totalAmount }.distinct().take(3)

                        quickAmounts.forEach { amount ->
                            OutlinedButton(
                                onClick = { receivedText = amount.stripTrailingZeros().toPlainString() },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("${CurrencyUtils.getCurrencySymbol()} ${amount.toInt()}", fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { onConfirm(receivedAmount) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        enabled = isEnough,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text(stringResource(R.string.sales_confirm_payment).uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalProgressDialog(status: String?, totalAmount: BigDecimal) {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(R.string.sales_processing_card)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(status ?: stringResource(R.string.sales_terminal_comm), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("${stringResource(R.string.sales_total)}: ${CurrencyUtils.format(totalAmount)}", fontWeight = FontWeight.Bold)
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
                        Text(stringResource(R.string.sales_order_paid).uppercase(), fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color(0xFF065F46), letterSpacing = (-1).sp)
                        Text(stringResource(R.string.sales_transaction_success).uppercase(), color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                        
                        Spacer(Modifier.height(48.dp))
                        
                        if (uiState.lastSaleQrContent != null) {
                            Surface(
                                modifier = Modifier.size(160.dp),
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                    com.extrotarget.extroposv2.ui.components.qr.QrCodeView(content = uiState.lastSaleQrContent, modifier = Modifier.fillMaxSize())
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.sales_scan_e_receipt).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                        }
                    }

                    Column(modifier = Modifier.weight(0.6f).padding(40.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text(stringResource(R.string.sales_ticket_summary).uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("#${uiState.lastSaleId?.takeLast(8)?.uppercase() ?: "PENDING"}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0F172A))
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SummaryDetailRow(stringResource(R.string.sales_subtotal).uppercase(), CurrencyUtils.format(uiState.subtotal))
                            if (uiState.taxConfig?.isTaxEnabled == true || uiState.totalTax > BigDecimal.ZERO) {
                                val taxName = uiState.taxConfig?.taxName ?: stringResource(R.string.sales_tax)
                                val taxRate = uiState.taxConfig?.defaultTaxRate?.stripTrailingZeros()?.toPlainString() ?: "0"
                                val taxLabel = "${taxName.uppercase()} (${taxRate}%)"
                                SummaryDetailRow(taxLabel, CurrencyUtils.format(uiState.totalTax))
                            }
                            if (uiState.totalDiscount > BigDecimal.ZERO) {
                                SummaryDetailRow(stringResource(R.string.sales_discount).uppercase(), "-${CurrencyUtils.format(uiState.totalDiscount)}", valueColor = Color(0xFFEF4444))
                            }
                            if (uiState.totalServiceCharge > BigDecimal.ZERO) {
                                val scLabel = "SERVICE CHARGE (${uiState.taxConfig?.serviceChargeRate?.stripTrailingZeros()?.toPlainString()}%)"
                                SummaryDetailRow(scLabel, CurrencyUtils.format(uiState.totalServiceCharge))
                            }
                            if (uiState.roundingAdjustment != BigDecimal.ZERO) {
                                SummaryDetailRow(stringResource(R.string.sales_rounding).uppercase(), CurrencyUtils.format(uiState.roundingAdjustment))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                        
                        if (uiState.lastPaymentMethod == "CASH" && uiState.cashReceived > BigDecimal.ZERO) {
                            SummaryDetailRow(stringResource(R.string.sales_cash_received).uppercase(), CurrencyUtils.format(uiState.cashReceived))
                            SummaryDetailRow(stringResource(R.string.sales_change).uppercase(), CurrencyUtils.format(uiState.changeAmount), valueColor = Color(0xFF166534))
                            Spacer(Modifier.height(8.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.sales_total_paid).uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Text(CurrencyUtils.format(uiState.totalAmountCash), fontWeight = FontWeight.Black, fontSize = 36.sp, color = Color(0xFF3B82F6), letterSpacing = (-1).sp)
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(onClick = onReprint, modifier = Modifier.height(64.dp).weight(1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(2.dp, Color(0xFFE2E8F0))) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.sales_print_receipt).uppercase(), fontWeight = FontWeight.Black)
                            }
                            Button(onClick = onDismiss, modifier = Modifier.height(64.dp).weight(1f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))) {
                                Text(stringResource(R.string.sales_next_order).uppercase(), fontWeight = FontWeight.Black)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodDialog(
    totalAmount: BigDecimal,
    onSelectMethod: (String) -> Unit,
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
                                stringResource(R.string.sales_payment_method).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                "${stringResource(R.string.sales_total_payable).uppercase()}: ${CurrencyUtils.format(totalAmount)}",
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PaymentMethodCard(
                            name = stringResource(R.string.sales_cash).uppercase(),
                            icon = Icons.Default.Payments,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectMethod("CASH") }
                        )
                        PaymentMethodCard(
                            name = stringResource(R.string.sales_card).uppercase(),
                            icon = Icons.Default.CreditCard,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectMethod("CARD") }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PaymentMethodCard(
                            name = stringResource(R.string.sales_duitnow_qr).uppercase(),
                            icon = Icons.Default.QrCodeScanner,
                            color = Color(0xFFEC4899),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectMethod("DUITNOW") }
                        )
                        PaymentMethodCard(
                            name = stringResource(R.string.sales_e_wallet).uppercase(),
                            icon = Icons.Default.AccountBalanceWallet,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectMethod("QR") }
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Text(
                        stringResource(R.string.sales_lhdn_compliance),
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Composable
fun PaymentMethodCard(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(2.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                name,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color(0xFF0F172A)
            )
        }
    }
}
