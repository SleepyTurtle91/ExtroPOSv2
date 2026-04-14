package com.extrotarget.extroposv2.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.analytics.components.SimpleBarChart
import com.extrotarget.extroposv2.ui.analytics.viewmodel.AnalyticsViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onNavigateToLowStock: () -> Unit,
    onNavigateToStaffEarnings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val result = viewModel.exportSstReport(outputStream)
                        if (result.isSuccess) {
                            android.widget.Toast.makeText(context, "SST Report exported", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Export failed: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Analytics & SST") },
                actions = {
                    IconButton(onClick = {
                        val fileName = "SST_Report_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(uiState.startDate))}_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(uiState.endDate))}.csv"
                        exportLauncher.launch(fileName)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export SST CSV")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Header
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Reporting Period", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${dateFormat.format(Date(uiState.startDate))} - ${dateFormat.format(Date(uiState.endDate))}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { /* Date Picker Logic */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                }
            }

            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(
                        title = "Gross Sales",
                        value = CurrencyUtils.format(uiState.totalSales),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    SummaryCard(
                        title = "SST Collected",
                        value = CurrencyUtils.format(uiState.totalTax),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }

            // Per-Rate SST Breakdown
            if (uiState.taxReports.isNotEmpty()) {
                item {
                    Text(
                        "SST Breakdown by Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(uiState.taxReports.size) { index ->
                    val report = uiState.taxReports[index]
                    TaxBreakdownRow(report)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(
                        title = "Discounts",
                        value = "-${CurrencyUtils.format(uiState.totalDiscount)}",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                    SummaryCard(
                        title = "Rounding",
                        value = (if (uiState.totalRounding >= java.math.BigDecimal.ZERO) "+" else "") + CurrencyUtils.format(uiState.totalRounding),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            item {
                SummaryCard(
                    title = "Transaction Count",
                    value = uiState.salesCount.toString(),
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            // Sales Trend Chart
            item {
                Text(
                    "Sales Trend (Today)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp)
                ) {
                    SimpleBarChart(
                        data = uiState.salesTrend,
                        modifier = Modifier.padding(16.dp),
                        barColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Top Products / Categories
            item {
                Text(
                    "Top 5 Products by Sales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.categorySplit.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(point.label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    CurrencyUtils.format(java.math.BigDecimal(point.value.toDouble())),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { if (uiState.totalSales.toFloat() > 0) point.value / uiState.totalSales.toFloat() else 0f },
                                modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small)
                            )
                        }
                    }
                }
            }

            // Industry Specific Reports
            item {
                Text(
                    "Industry Specific Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedCard(
                        onClick = onNavigateToLowStock,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Inventory Alerts", fontWeight = FontWeight.Bold)
                            Text("Low stock items", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    ElevatedCard(
                        onClick = onNavigateToStaffEarnings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Staff Earnings", fontWeight = FontWeight.Bold)
                            Text("Commission report", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // SST Compliance Section
            item {
                Text(
                    "Malaysian Tax Compliance (SST)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Light Green
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Ready for SST Filing",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Total SST (6% / 8%): ${CurrencyUtils.format(uiState.totalTax)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Net Sales (Excl. Tax): ${CurrencyUtils.format(uiState.totalSales.subtract(uiState.totalTax))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaxBreakdownRow(report: com.extrotarget.extroposv2.ui.analytics.viewmodel.TaxReportItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(report.categoryName, fontWeight = FontWeight.Bold)
                Text("Net Sales: ${CurrencyUtils.format(report.netSales)}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                CurrencyUtils.format(report.taxAmount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}