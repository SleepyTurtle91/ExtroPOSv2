package com.extrotarget.extroposv2.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.analytics.viewmodel.AnalyticsViewModel
import com.extrotarget.extroposv2.ui.components.stitch.StitchKpiCard
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchButton
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateToLowStock: () -> Unit,
    onNavigateToStaffEarnings: () -> Unit,
    onNavigateToReporting: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Bar Simulation
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            color = StitchColor.SurfaceContainerLowest,
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "REPORTS & ANALYTICS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StitchButton(text = "TODAY", onClick = {}, containerColor = StitchColor.Primary)
                    StitchButton(text = "WEEK", onClick = {}, containerColor = StitchColor.SurfaceContainerLow, contentColor = StitchColor.OnSurfaceVariant)
                    StitchButton(text = "MONTH", onClick = {}, containerColor = StitchColor.SurfaceContainerLow, contentColor = StitchColor.OnSurfaceVariant)
                }
            }
        }

        // Performance Overview Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StitchKpiCard(
                label = "Gross Sales",
                value = "RM ${uiState.totalSales}",
                trend = "+12%",
                modifier = Modifier.weight(1f)
            )
            StitchKpiCard(
                label = "Total Orders",
                value = uiState.salesCount.toString(),
                trend = "+5%",
                modifier = Modifier.weight(1f)
            )
            StitchKpiCard(
                label = "Avg Transaction",
                value = "RM 41.66", // TODO: Logic
                trend = "-2%",
                isTrendUp = false,
                modifier = Modifier.weight(1f)
            )
            StitchKpiCard(
                label = "New Members",
                value = "28", // TODO: Logic
                trend = "+18%",
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Chart & Insights Area
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trend Chart Simulation
            Surface(
                modifier = Modifier.weight(2f).fillMaxHeight(),
                color = StitchColor.SurfaceContainerLowest,
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SALES TRENDS (THIS WEEK)", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Export CSV", color = StitchColor.Primary, style = MaterialTheme.typography.labelCaps)
                    }
                    
                    Box(modifier = Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        Text("Chart Component Integration Ready", color = StitchColor.Outline)
                    }
                }
            }

            // Top Categories Simulation
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = StitchColor.SurfaceContainerLowest,
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("TOP CATEGORIES", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    
                    Spacer(Modifier.height(24.dp))
                    
                    CategoryProgressRow("Beverages", 0.45f, StitchColor.Primary)
                    CategoryProgressRow("Hot Meals", 0.30f, StitchColor.Tertiary)
                    CategoryProgressRow("Pastries", 0.15f, StitchColor.Secondary)
                    CategoryProgressRow("Merchandise", 0.10f, StitchColor.OutlineVariant)
                }
            }
        }
    }
}

@Composable
private fun CategoryProgressRow(label: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall.copy(color = color, fontWeight = FontWeight.Bold))
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
            color = color,
            trackColor = StitchColor.SurfaceContainerLow,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
