package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal

@Composable
fun TableMap(
    tables: List<Table>,
    onTableClick: (Table) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9)).padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "FLOOR PLAN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(
                    "MAIN DINING HALL",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem("AVAILABLE", Color(0xFFE2E8F0))
                LegendItem("OCCUPIED", Color(0xFF3B82F6))
                LegendItem("BILLING", Color(0xFF10B981))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tables) { table ->
                TableCard(table = table, onClick = { onTableClick(table) })
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B))
    }
}

@Composable
fun TableCard(table: Table, onClick: () -> Unit) {
    val statusColor = when (table.status) {
        TableStatus.AVAILABLE -> Color(0xFFE2E8F0)
        TableStatus.OCCUPIED -> Color(0xFF3B82F6)
        TableStatus.BILLING -> Color(0xFF10B981)
        TableStatus.DIRTY -> Color(0xFFFACC15)
        TableStatus.RESERVED -> Color(0xFF8B5CF6)
    }
    
    val isActive = table.status != TableStatus.AVAILABLE

    Surface(
        modifier = Modifier
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isActive) statusColor else Color(0xFFE2E8F0)),
        shadowElevation = if (isActive) 4.dp else 1.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Status Indicator Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "T-${table.name}",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    
                    if (isActive) {
                        Icon(
                            if (table.status == TableStatus.BILLING) Icons.Default.Notifications else Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isActive) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            CurrencyUtils.format(table.currentBillAmount ?: BigDecimal.ZERO),
                            color = statusColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "45 MINS", // Mock time-at-table
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                } else {
                    Text(
                        "VACANT",
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = if (isActive) statusColor.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${table.capacity} SEATS",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (isActive) statusColor else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
