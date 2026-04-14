package com.extrotarget.extroposv2.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// --- UI State ---

data class ZReportUiState(
    val systemExpectedCash: BigDecimal = BigDecimal.ZERO,
    val actualDrawerCash: String = "",
    val floatAmount: String = "200.00",
    val cashIn: String = "0.00",
    val cashOut: String = "0.00",
    val grossSales: BigDecimal = BigDecimal.ZERO,
    val sstCollected: BigDecimal = BigDecimal.ZERO,
    val roundingAdjustment: BigDecimal = BigDecimal.ZERO,
    val salesByPaymentMethod: Map<String, BigDecimal> = emptyMap(),
    val isPrinting: Boolean = false,
    val isExporting: Boolean = false,
    val currentTime: Date = Date(),
    val terminalId: String = "TERM-01",
    val staffName: String = "Admin"
) {
    val totalCashSales: BigDecimal = salesByPaymentMethod["Cash"] ?: BigDecimal.ZERO
    
    val calculatedExpectedCash: BigDecimal
        get() = try {
            val f = BigDecimal(floatAmount.ifEmpty { "0" })
            val ci = BigDecimal(cashIn.ifEmpty { "0" })
            val co = BigDecimal(cashOut.ifEmpty { "0" })
            f.add(totalCashSales).add(ci).subtract(co)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }

    val discrepancy: BigDecimal
        get() = try {
            val actual = BigDecimal(actualDrawerCash.ifEmpty { "0" })
            actual.subtract(calculatedExpectedCash)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
}

// --- ViewModel ---

@HiltViewModel
class ZReportViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ZReportUiState())
    val uiState: StateFlow<ZReportUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
        updateTime()
    }

    private fun loadMockData() {
        _uiState.update {
            it.copy(
                grossSales = BigDecimal("4580.50"),
                sstCollected = BigDecimal("274.83"),
                roundingAdjustment = BigDecimal("-0.03"),
                salesByPaymentMethod = mapOf(
                    "Cash" to BigDecimal("1250.40"),
                    "DuitNow" to BigDecimal("2100.10"),
                    "Card" to BigDecimal("1230.00")
                )
            )
        }
    }

    private fun updateTime() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(currentTime = Date()) }
                delay(1000)
            }
        }
    }

    fun onActualCashChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(actualDrawerCash = value) }
        }
    }

    fun onFloatChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(floatAmount = value) }
        }
    }

    fun onCashInChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(cashIn = value) }
        }
    }

    fun onCashOutChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(cashOut = value) }
        }
    }

    fun printZReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPrinting = true) }
            delay(2000) // Simulate printer HAL activity
            _uiState.update { it.copy(isPrinting = false) }
        }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            delay(1500) // Simulate export
            _uiState.update { it.copy(isExporting = false) }
        }
    }
}

// --- UI Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZReportScreen(
    viewModel: ZReportViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shift Closeout / Z-Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Terminal: ${uiState.terminalId} | ${dateFormatter.format(uiState.currentTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            ZReportFooter(
                isPrinting = uiState.isPrinting,
                isExporting = uiState.isExporting,
                onPrint = { viewModel.printZReport() },
                onExport = { viewModel.exportToCsv() }
            )
        },
        containerColor = Color(0xFF0F172A) // Slate 900
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Pane: Expected vs Actual
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                CashDrawerCard(
                    uiState = uiState,
                    onActualCashChange = { viewModel.onActualCashChange(it) },
                    onFloatChange = { viewModel.onFloatChange(it) },
                    onCashInChange = { viewModel.onCashInChange(it) },
                    onCashOutChange = { viewModel.onCashOutChange(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DiscrepancyCard(discrepancy = uiState.discrepancy)
            }

            // Right Pane: Daily Summary
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                DailySummaryCard(uiState = uiState)
            }
        }
    }
}

@Composable
fun CashDrawerCard(
    uiState: ZReportUiState,
    onActualCashChange: (String) -> Unit,
    onFloatChange: (String) -> Unit,
    onCashInChange: (String) -> Unit,
    onCashOutChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate 800
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cash Drawer Reconciliation", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))

            FinancialInputField(label = "Starting Float", value = uiState.floatAmount, onValueChange = onFloatChange)
            FinancialInputField(label = "Total Cash Sales", value = CurrencyUtils.format(uiState.totalCashSales), enabled = false)
            FinancialInputField(label = "Cash In (Additions)", value = uiState.cashIn, onValueChange = onCashInChange)
            FinancialInputField(label = "Cash Out (Withdrawals)", value = uiState.cashOut, onValueChange = onCashOutChange)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("System Expected Cash", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        text = CurrencyUtils.format(uiState.calculatedExpectedCash),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Actual Drawer Cash", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            OutlinedTextField(
                value = uiState.actualDrawerCash,
                onValueChange = onActualCashChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                placeholder = { Text("0.00", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("MYR", style = MaterialTheme.typography.bodyLarge) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    cursorColor = Color(0xFF38BDF8)
                )
            )
        }
    }
}

@Composable
fun DiscrepancyCard(discrepancy: BigDecimal) {
    val isShort = discrepancy < BigDecimal.ZERO
    val isOver = discrepancy > BigDecimal.ZERO
    val color = when {
        isShort -> Color(0xFFF87171) // Red
        isOver -> Color(0xFF4ADE80) // Green
        else -> Color(0xFF94A3B8) // Slate 400
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when {
                        isShort -> "CASH SHORTAGE"
                        isOver -> "CASH OVERAGE"
                        else -> "CASH BALANCED"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyUtils.format(discrepancy.abs()),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
            Icon(
                imageVector = when {
                    isShort -> Icons.AutoMirrored.Filled.TrendingDown
                    isOver -> Icons.AutoMirrored.Filled.TrendingUp
                    else -> Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun DailySummaryCard(uiState: ZReportUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Summarize, contentDescription = null, tint = Color(0xFF818CF8))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Financial Summary", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))

            SummaryRow("Gross Sales", CurrencyUtils.format(uiState.grossSales), isMain = true)
            SummaryRow("SST Collected (6%/8%)", CurrencyUtils.format(uiState.sstCollected))
            SummaryRow("Rounding (5-Sen)", CurrencyUtils.format(uiState.roundingAdjustment))
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sales by Payment Method", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            uiState.salesByPaymentMethod.forEach { (method, amount) ->
                val icon = when (method) {
                    "Cash" -> Icons.Default.Payments
                    "DuitNow" -> Icons.Default.QrCodeScanner
                    "Card" -> Icons.Default.CreditCard
                    else -> Icons.Default.Money
                }
                PaymentMethodRow(method, CurrencyUtils.format(amount), icon)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF818CF8).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Total Net Revenue", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC7D2FE))
                    Text(
                        text = CurrencyUtils.format(uiState.grossSales.add(uiState.sstCollected).add(uiState.roundingAdjustment)),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FinancialInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        if (enabled) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(150.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        } else {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isMain: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label, 
            style = if (isMain) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = if (isMain) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isMain) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            value,
            style = if (isMain) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PaymentMethodRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
fun ZReportFooter(
    isPrinting: Boolean,
    isExporting: Boolean,
    onPrint: () -> Unit,
    onExport: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onExport,
                modifier = Modifier.height(64.dp).weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export to CSV", fontSize = 18.sp)
                }
            }

            Button(
                onClick = onPrint,
                modifier = Modifier.height(64.dp).weight(2f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                enabled = !isPrinting
            ) {
                if (isPrinting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Print Z-Report & Close Shift", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(device = Devices.TABLET, showBackground = true)
@Composable
fun ZReportPreview() {
    MaterialTheme {
        ZReportScreen(viewModel = ZReportViewModel())
    }
}
