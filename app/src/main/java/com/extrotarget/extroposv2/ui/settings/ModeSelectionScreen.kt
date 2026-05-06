package com.extrotarget.extroposv2.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import com.extrotarget.extroposv2.core.data.model.settings.TerminalRole
import com.extrotarget.extroposv2.core.data.model.settings.OperationMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMode = uiState.activeMode

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal Configuration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Business Mode (Loyverse style)
            Text(
                "Primary Business Function",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(340.dp) // Fixed height to work inside scroll
            ) {
                items(BusinessMode.values()) { mode ->
                    ModeCard(
                        mode = mode,
                        isSelected = uiState.activeMode == mode,
                        onClick = { viewModel.setBusinessMode(mode) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))

            // 2. Terminal Role (Master / Slave)
            Text(
                "Terminal Hierarchy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TerminalRole.values().forEach { role ->
                    val isSelected = uiState.terminalRole == role
                    Surface(
                        modifier = Modifier.weight(1f).height(80.dp).clickable { viewModel.setTerminalRole(role) },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF3B82F6) else Color(0xFFE2E8F0)),
                        color = if (isSelected) Color(0xFFEFF6FF) else Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(role.displayName, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF3B82F6) else Color.Gray)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))

            // 3. Operation Mode (POS / Backend / Hybrid)
            Text(
                "Operational Focus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(16.dp))

            OperationMode.entries.forEach { mode ->
                val isSelected = uiState.operationMode == mode
                ListItem(
                    modifier = Modifier.clickable { viewModel.setOperationMode(mode) }.padding(vertical = 4.dp),
                    headlineContent = { Text(mode.displayName, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(mode.description) },
                    leadingContent = { 
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(mode.icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray)
                            }
                        }
                    },
                    trailingContent = {
                        RadioButton(selected = isSelected, onClick = null)
                    }
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF854D0E))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Changes to Hierarchy require an app restart to re-initialize P2P services.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF854D0E)
                    )
                }
            }
        }
    }
}

@Composable
fun ModeCard(
    mode: BusinessMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) mode.color else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(24.dp)
            ),
        color = if (isSelected) mode.color.copy(alpha = 0.05f) else Color.White,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) mode.color else Color(0xFFF1F5F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        mode.icon, 
                        contentDescription = null, 
                        tint = if (isSelected) Color.White else mode.color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                mode.displayName,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = if (isSelected) mode.color else Color(0xFF1E293B)
            )
        }
    }
}
