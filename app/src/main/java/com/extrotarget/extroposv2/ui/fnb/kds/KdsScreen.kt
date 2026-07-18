package com.extrotarget.extroposv2.ui.fnb.kds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.ui.fnb.kds.viewmodel.KdsOrder
import com.extrotarget.extroposv2.ui.fnb.kds.viewmodel.KdsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KdsScreen(
    viewModel: KdsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.fnb_kds_title), fontWeight = FontWeight.Black) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A),
                        titleContentColor = Color.White
                    )
                )
                
                if (uiState.stations.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = uiState.stations.indexOf(uiState.selectedStation).coerceAtLeast(0),
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White,
                        edgePadding = 16.dp,
                        divider = {}
                    ) {
                        uiState.stations.forEach { station ->
                            val count = uiState.stationItemCounts[station.id] ?: 0
                            Tab(
                                selected = uiState.selectedStation?.id == station.id,
                                onClick = { viewModel.selectStation(station.id) },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(station.name.uppercase(), fontWeight = FontWeight.Bold)
                                        if (count > 0) {
                                            Spacer(Modifier.width(8.dp))
                                            Badge(
                                                containerColor = Color(0xFF3B82F6),
                                                contentColor = Color.White
                                            ) {
                                                Text(count.toString(), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.fnb_no_orders, uiState.selectedStation?.name ?: ""), 
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8FAFC)),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.orders) { order ->
                    KdsOrderCard(
                        order = order,
                        onDone = { viewModel.markOrderDone(order.sale.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun KdsOrderCard(
    order: KdsOrder,
    onDone: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val waitTime = (System.currentTimeMillis() - order.sale.timestamp) / 60000 // in minutes

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (waitTime > 15) Color(0xFFFEF2F2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.fnb_order_label, order.sale.id.takeLast(6).uppercase()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        timeFormat.format(Date(order.sale.timestamp)),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                }
                
                Surface(
                    color = if (waitTime > 15) Color(0xFFEF4444) else Color(0xFFF1F5F9),
                    shape = CircleShape
                ) {
                    Text(
                        stringResource(R.string.fnb_time_ago, waitTime),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (waitTime > 15) Color.White else Color(0xFF475569)
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFFF1F5F9),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.quantity.toInt().toString(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            if (!item.modifiers.isNullOrBlank()) {
                                Text(
                                    text = item.modifiers,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.fnb_mark_done), fontWeight = FontWeight.Black)
            }
        }
    }
}
