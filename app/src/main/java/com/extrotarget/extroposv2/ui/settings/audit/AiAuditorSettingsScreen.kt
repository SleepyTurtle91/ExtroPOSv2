package com.extrotarget.extroposv2.ui.settings.audit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAuditorSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiAuditorViewModel = hiltViewModel()
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    
    val isShiftAuditEnabled by viewModel.isShiftAuditEnabled.collectAsState()
    val isReportAssistantEnabled by viewModel.isReportAssistantEnabled.collectAsState()
    val isInventoryAssistantEnabled by viewModel.isInventoryAssistantEnabled.collectAsState()
    val isSearchOptimizationEnabled by viewModel.isSearchOptimizationEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Silent Auditor (BYOK)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Gemini 3.1 Flash-Lite", fontWeight = FontWeight.Bold)
                            Text("Empower your POS with autonomous auditing and natural language reports using Google's latest model (Free Tier).", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Text("Configuration", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            item {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    label = { Text("Gemini API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    placeholder = { Text("Enter your Gemini API Key") },
                    supportingText = {
                        Text("Get a free key from Google AI Studio (aistudio.google.com)")
                    }
                )
            }

            if (isAiEnabled) {
                item {
                    Text("Silent Auditor Features", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                }

                item {
                    AiFeatureToggle(
                        title = "Shift Audit & Discrepancy Finder",
                        description = "Automatically compare daily sales vs closing data to find missing cash.",
                        enabled = isShiftAuditEnabled,
                        onToggle = viewModel::toggleShiftAudit
                    )
                }

                item {
                    AiFeatureToggle(
                        title = "Natural Language Reports",
                        description = "Ask questions like 'Give me a tender report for last month' in plain English.",
                        enabled = isReportAssistantEnabled,
                        onToggle = viewModel::toggleReportAssistant
                    )
                }

                item {
                    AiFeatureToggle(
                        title = "Inventory & Stock Optimization",
                        description = "AI suggests reorder levels and identifies slow-moving items.",
                        enabled = isInventoryAssistantEnabled,
                        onToggle = viewModel::toggleInventoryAssistant
                    )
                }

                item {
                    AiFeatureToggle(
                        title = "Search & Barcode Optimization",
                        description = "Semantic search for products and assistance with unknown barcodes.",
                        enabled = isSearchOptimizationEnabled,
                        onToggle = viewModel::toggleSearchOptimization
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Text("Enter your API key to enable AI features.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiFeatureToggle(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(description, style = MaterialTheme.typography.bodySmall) },
        trailingContent = {
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    )
}
