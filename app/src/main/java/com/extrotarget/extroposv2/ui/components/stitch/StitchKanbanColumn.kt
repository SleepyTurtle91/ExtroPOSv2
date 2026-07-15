package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun <T> StitchKanbanColumn(
    title: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    statusColor: Color = StitchColor.Outline,
    count: Int = items.size
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StitchColor.Surface.copy(alpha = 0.5f))
            .border(1.dp, StitchColor.OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        // Column Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchColor.Surface)
                .padding(12.dp)
                .drawBehindBorderBottom(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurface)
                )
            }
            Surface(
                color = StitchColor.SurfaceContainerHigh,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Column Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items) { item ->
                itemContent(item)
            }
        }
    }
}

// Simple helper for bottom border
private fun Modifier.drawBehindBorderBottom(): Modifier = this.drawBehind {
    val strokeWidth = 1.dp.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(
        color = StitchColor.OutlineVariant.copy(alpha = 0.5f),
        start = androidx.compose.ui.geometry.Offset(0f, y),
        end = androidx.compose.ui.geometry.Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

// Ensure drawBehind is imported
import androidx.compose.ui.draw.drawBehind
