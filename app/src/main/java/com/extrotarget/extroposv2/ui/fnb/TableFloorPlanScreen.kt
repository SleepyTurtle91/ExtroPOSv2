package com.extrotarget.extroposv2.ui.fnb

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus
import com.extrotarget.extroposv2.ui.components.stitch.StitchTableCard
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchButton
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchOutlinedButton
import com.extrotarget.extroposv2.ui.fnb.viewmodel.TableViewModel
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableFloorPlanScreen(
    viewModel: TableViewModel,
    onTableClick: (Table) -> Unit
) {
    val tables by viewModel.tables.collectAsState()
    val selectedZone by viewModel.selectedZone.collectAsState()
    val zones by viewModel.zones.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(zones) { zone ->
                val isSelected = zone == selectedZone
                if (isSelected) {
                    StitchButton(
                        text = zone.uppercase(),
                        onClick = { viewModel.selectZone(zone) },
                        containerColor = StitchColor.Primary,
                        contentColor = Color.White
                    )
                } else {
                    StitchOutlinedButton(
                        text = zone.uppercase(),
                        onClick = { viewModel.selectZone(zone) },
                        contentColor = StitchColor.OnSurfaceVariant
                    )
                }
            }
        }

        // Legend & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem("Available", StitchColor.TertiaryContainer)
                LegendItem("Occupied", StitchColor.PrimaryContainer)
                LegendItem("Reserved", StitchColor.ErrorContainer)
                LegendItem("Cleaning", StitchColor.OutlineVariant)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StitchOutlinedButton(
                    text = "Bulk Add",
                    icon = Icons.Default.AddBusiness,
                    onClick = { 
                        // Logic to show Bulk Add Dialog
                    },
                    contentColor = StitchColor.Primary
                )
                StitchOutlinedButton(
                    text = "Transfer",
                    icon = Icons.Default.SwapHoriz,
                    onClick = { 
                        /* Logic to start moveTable workflow */
                    },
                    contentColor = StitchColor.OnSurface
                )
                StitchOutlinedButton(
                    text = "Merge",
                    icon = Icons.AutoMirrored.Filled.CallMerge,
                    onClick = { 
                        /* Logic to start joinTable workflow */
                    },
                    contentColor = StitchColor.OnSurface
                )
            }
        }

        // Floor Plan Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tables, key = { it.id }) { table ->
                var showMenu by remember { mutableStateOf(false) }

                Box {
                    StitchTableCard(
                        table = table,
                        onClick = { onTableClick(table) },
                        itemCount = 0,
                        orderAmount = java.math.BigDecimal.ZERO,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onTableClick(table) },
                                onLongClick = { showMenu = true }
                            )
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { 
                                showMenu = false
                                // Edit Logic
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = { 
                                showMenu = false
                                viewModel.duplicateTable(table)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { 
                                showMenu = false
                                viewModel.deleteTable(table.id)
                            },
                            enabled = table.status == TableStatus.AVAILABLE
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = StitchColor.OnSurfaceVariant))
    }
}
