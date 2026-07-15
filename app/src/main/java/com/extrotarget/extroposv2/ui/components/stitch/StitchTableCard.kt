package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Group
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
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps
import java.math.BigDecimal

@Composable
fun StitchTableCard(
    table: Table,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    orderAmount: BigDecimal = BigDecimal.ZERO,
    itemCount: Int = 0,
    occupancyTime: String? = null
) {
    val statusColor = when (table.status) {
        TableStatus.AVAILABLE -> StitchColor.TertiaryContainer
        TableStatus.OCCUPIED -> StitchColor.PrimaryContainer
        TableStatus.RESERVED -> StitchColor.ErrorContainer
        TableStatus.DIRTY -> StitchColor.OutlineVariant
    }

    Surface(
        modifier = modifier
            .height(140.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = table.status != TableStatus.DIRTY) { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) StitchColor.Primary else StitchColor.OutlineVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        color = StitchColor.SurfaceContainerLowest,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Status Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(statusColor)
                    .align(Alignment.TopCenter)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Info
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = table.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp), tint = StitchColor.OnSurfaceVariant)
                            Text(
                                text = "${table.capacity} Pax",
                                style = MaterialTheme.typography.bodySmall.copy(color = StitchColor.OnSurfaceVariant)
                            )
                        }
                    }

                    if (occupancyTime != null) {
                        Surface(
                            color = StitchColor.PrimaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = occupancyTime,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelCaps.copy(fontSize = 10.sp, color = StitchColor.Primary)
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Bottom Info
                Divider(color = StitchColor.OutlineVariant.copy(alpha = 0.3f))
                
                if (table.status == TableStatus.OCCUPIED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StitchColor.SurfaceContainerLowest)
                            .padding(8.dp, 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$itemCount Items",
                            style = MaterialTheme.typography.bodySmall.copy(color = StitchColor.OnSurfaceVariant)
                        )
                        Text(
                            text = "RM $orderAmount",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = StitchColor.Primary)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StitchColor.SurfaceContainerLow)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (table.status == TableStatus.DIRTY) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp), tint = StitchColor.OnSurfaceVariant)
                            }
                            Text(
                                text = table.status.name,
                                style = MaterialTheme.typography.labelCaps.copy(
                                    color = if (table.status == TableStatus.AVAILABLE) StitchColor.Tertiary else StitchColor.OnSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
