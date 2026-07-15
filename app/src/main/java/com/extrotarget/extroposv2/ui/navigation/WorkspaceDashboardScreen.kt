package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.platform.models.DashboardActionId
import com.extrotarget.extroposv2.ui.components.stitch.StitchKpiCard
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun WorkspaceDashboardScreen(
    onNavigateToAction: (DashboardActionId) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val config by viewModel.dashboardConfig.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val businessName by viewModel.businessName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good Morning, $userName",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = StitchColor.OnSurface
                    )
                )
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = StitchColor.OnSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
            
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(StitchColor.SurfaceContainerHigh)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = StitchColor.Primary)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Quick Actions
        Text(
            text = "QUICK ACTIONS",
            style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant, letterSpacing = 2.sp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(config.quickActions) { action ->
                QuickActionCard(
                    title = action.label,
                    icon = action.icon,
                    onClick = { onNavigateToAction(action.actionId) }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Today's Summary
        Text(
            text = "TODAY'S SUMMARY",
            style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant, letterSpacing = 2.sp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            config.widgets.forEach { widget ->
                StitchKpiCard(
                    label = widget.label,
                    value = widget.value,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(160.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(1.dp, StitchColor.OutlineVariant, RoundedCornerShape(24.dp)),
        color = StitchColor.SurfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = StitchColor.PrimaryContainer.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = StitchColor.Primary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
        }
    }
}
