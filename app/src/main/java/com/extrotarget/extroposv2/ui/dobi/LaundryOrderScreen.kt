package com.extrotarget.extroposv2.ui.dobi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryOrder
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryStatus
import com.extrotarget.extroposv2.ui.components.stitch.StitchKanbanColumn
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchButton
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchTextField
import com.extrotarget.extroposv2.ui.dobi.viewmodel.LaundryViewModel
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun LaundryOrderScreen(
    viewModel: LaundryViewModel
) {
    val orders by viewModel.orders.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Kanban Columns
        StitchKanbanColumn(
            title = "RECEIVED",
            items = orders.filter { it.status == LaundryStatus.RECEIVED },
            statusColor = StitchColor.Outline,
            itemContent = { order -> LaundryOrderStitchCard(order) },
            modifier = Modifier.weight(1f)
        )

        StitchKanbanColumn(
            title = "PROCESSING",
            items = orders.filter { it.status == LaundryStatus.PROCESSING },
            statusColor = StitchColor.Secondary,
            itemContent = { order -> LaundryOrderStitchCard(order) },
            modifier = Modifier.weight(1f)
        )

        StitchKanbanColumn(
            title = "READY / SIAP",
            items = orders.filter { it.status == LaundryStatus.READY },
            statusColor = StitchColor.Primary,
            itemContent = { order -> LaundryOrderStitchCard(order) },
            modifier = Modifier.weight(1f)
        )

        // Right Sidebar: Quick Check-in
        Surface(
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight(),
            color = StitchColor.SurfaceContainerLowest,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "QUICK CHECK-IN",
                    style = MaterialTheme.typography.labelCaps.copy(
                        color = StitchColor.OnSurface,
                        fontSize = 14.sp
                    )
                )
                
                Spacer(Modifier.height(24.dp))

                StitchTextField(
                    value = "",
                    onValueChange = {},
                    label = "CUSTOMER (OPTIONAL)",
                    placeholder = "Phone or Name...",
                    leadingIcon = Icons.Default.PersonAdd
                )

                Spacer(Modifier.height(16.dp))

                // Weight Input Simulation
                Surface(
                    color = StitchColor.SurfaceContainerLow,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("WEIGHT / LOAD", style = MaterialTheme.typography.labelCaps)
                            Surface(
                                color = StitchColor.Primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Scale Connected",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelCaps.copy(fontSize = 10.sp, color = StitchColor.Primary)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            "0.0",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                StitchButton(
                    text = "CREATE ORDER",
                    onClick = { /* TODO */ },
                    size = com.extrotarget.extroposv2.ui.components.stitch.common.StitchButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.ReceiptLong
                )
            }
        }
    }
}

@Composable
private fun LaundryOrderStitchCard(order: LaundryOrder) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant),
        color = StitchColor.Surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "#${order.id.takeLast(4)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    color = StitchColor.SurfaceContainerHigh,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        (order.items.firstOrNull()?.name ?: "SERVICE").uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelCaps.copy(fontSize = 9.sp)
                    )
                }
            }
            
            Text(
                order.customerName,
                style = MaterialTheme.typography.bodySmall.copy(color = StitchColor.OnSurfaceVariant),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(14.dp), tint = StitchColor.Primary)
                        Text("${order.weightKg} kg", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    "RM ${order.totalPrice}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = StitchColor.Primary)
                )
            }
        }
    }
}
