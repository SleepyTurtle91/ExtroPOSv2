package com.extrotarget.extroposv2.ui.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.extrotarget.extroposv2.core.platform.models.QuickAction
import com.extrotarget.extroposv2.core.platform.models.DashboardWidget
import com.extrotarget.extroposv2.core.platform.models.WidgetType

@Composable
fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = Color(0xFF1E293B), // Slate 800
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                action.icon,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = action.label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryWidgetCard(
    widget: DashboardWidget,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp)),
        color = Color(0xFF0F172A), // Slate 900
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = widget.label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = widget.value,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
