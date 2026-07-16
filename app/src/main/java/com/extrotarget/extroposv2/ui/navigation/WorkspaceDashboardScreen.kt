package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch

@Composable
fun WorkspaceDashboardScreen(
    onNavigateToAction: (DashboardActionId) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val config by viewModel.dashboardConfig.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(24.dp)
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
                            color = StitchColor.OnSurface,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = businessName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = StitchColor.OnSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 18.sp
                        )
                    )
                }
                
                IconButton(
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("No new notifications")
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StitchColor.SurfaceContainerHigh)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = StitchColor.Primary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant, letterSpacing = 2.sp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                config.quickActions.chunked(3).forEach { rowActions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowActions.forEach { action ->
                            QuickActionCard(
                                title = action.label,
                                icon = action.icon,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToAction(action.actionId) }
                            )
                        }
                        repeat(3 - rowActions.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Today's Summary
            Text(
                text = "TODAY'S SUMMARY",
                style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant, letterSpacing = 2.sp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                config.widgets.chunked(3).forEach { rowWidgets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowWidgets.forEach { widget ->
                            StitchKpiCard(
                                label = widget.label,
                                value = widget.value,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowWidgets.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, StitchColor.OutlineVariant, RoundedCornerShape(16.dp)),
        color = StitchColor.SurfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(8.dp),
                color = StitchColor.PrimaryContainer.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = StitchColor.Primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            )
        }
    }
}
