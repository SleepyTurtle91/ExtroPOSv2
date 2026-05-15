package com.extrotarget.extroposv2.feature.reporting.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.feature.reporting.ui.components.TrendChart
import com.extrotarget.extroposv2.feature.reporting.ui.viewmodel.ReportingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingDashboard(
    viewModel: ReportingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val salesSummary by viewModel.salesSummary.collectAsState()
    val productPerformance by viewModel.productPerformance.collectAsState()
    val commissionReport by viewModel.commissionReport.collectAsState()
    val taxCompliance by viewModel.taxCompliance.collectAsState()
    val addonPerformance by viewModel.addonPerformance.collectAsState()
    val occupancyTrends by viewModel.occupancyTrends.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    viewModel.exportTaxReport(outputStream)
                }
            }
        }
    )

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporting Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Icon for back
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val fileName = "Tax_Report_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.csv"
                        exportLauncher.launch(fileName)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV")
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
            // Date Filter
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Period", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { /* Open Date Picker */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Range")
                        }
                    }
                }
            }

            // Sales Trend Chart
            item {
                Text("Sales Trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp)
                ) {
                    TrendChart(
                        data = listOf("Mon" to 10f, "Tue" to 15f, "Wed" to 8f, "Thu" to 20f, "Fri" to 12f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Summary Cards
            item {
                salesSummary?.let { summary ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryItem(
                                title = "Total Sales",
                                value = CurrencyUtils.format(summary.totalSales),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryItem(
                                title = "Net Sales",
                                value = CurrencyUtils.format(summary.netSales),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryItem(
                                title = "Tax",
                                value = CurrencyUtils.format(summary.totalTax),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryItem(
                                title = "Avg Ticket",
                                value = CurrencyUtils.format(summary.averageTicketSize),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Top Products
            item {
                Text("Top Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(productPerformance.size) { index ->
                val product = productPerformance[index]
                ListItem(
                    headlineContent = { Text(product.productName) },
                    supportingContent = { Text("Qty: ${product.totalQuantity}") },
                    trailingContent = { Text(CurrencyUtils.format(product.totalRevenue)) }
                )
            }

            // Commission Report
            if (commissionReport.isNotEmpty()) {
                item {
                    Text("Staff Commissions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                items(commissionReport.size) { index ->
                    val commission = commissionReport[index]
                    ListItem(
                        headlineContent = { Text(commission.staffName) },
                        supportingContent = { Text("Sales: ${CurrencyUtils.format(commission.totalSales)}") },
                        trailingContent = { Text(CurrencyUtils.format(commission.totalCommission), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Tax Compliance
            if (taxCompliance.isNotEmpty()) {
                item {
                    Text("Tax Compliance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                items(taxCompliance.size) { index ->
                    val tax = taxCompliance[index]
                    ListItem(
                        headlineContent = { Text("Tax Rate: ${tax.taxRate}%") },
                        supportingContent = { Text("Net Sales: ${CurrencyUtils.format(tax.netSales)}") },
                        trailingContent = { Text(CurrencyUtils.format(tax.taxAmount), fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Addon Performance
            if (addonPerformance.isNotEmpty()) {
                item {
                    Text("Addon & Upsell Performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                items(addonPerformance.size) { index ->
                    val addon = addonPerformance[index]
                    ListItem(
                        headlineContent = { Text(addon.addonName) },
                        supportingContent = { Text("Qty: ${addon.totalQuantity}") },
                        trailingContent = { Text(CurrencyUtils.format(addon.totalRevenue)) }
                    )
                }
            }

            // Occupancy Trends
            if (occupancyTrends.isNotEmpty()) {
                item {
                    Text("Occupancy Trends", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 8.dp)
                    ) {
                        TrendChart(
                            data = occupancyTrends.map { SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(it.date)) to it.occupancyRate },
                            modifier = Modifier.padding(16.dp),
                            barColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}
