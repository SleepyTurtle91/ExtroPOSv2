package com.extrotarget.extroposv2.ui.settings.tax

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.settings.TaxConfig
import com.extrotarget.extroposv2.ui.settings.viewmodel.TaxSettingsViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSettingsScreen(
    viewModel: TaxSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val config by viewModel.taxConfig.collectAsState()
    var editableConfig by remember(config) { mutableStateOf(config) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tax Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.updateTaxConfig(editableConfig) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Configure tax and other charges applied to your sales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Enable Tax") },
                supportingContent = { Text("Apply tax automatically to all transactions") },
                trailingContent = {
                    Switch(
                        checked = editableConfig.isTaxEnabled,
                        onCheckedChange = { editableConfig = editableConfig.copy(isTaxEnabled = it) }
                    )
                }
            )

            OutlinedTextField(
                value = editableConfig.taxName,
                onValueChange = { editableConfig = editableConfig.copy(taxName = it) },
                label = { Text("Tax Label") },
                placeholder = { Text("e.g. SST") },
                modifier = Modifier.fillMaxWidth(),
                enabled = editableConfig.isTaxEnabled
            )

            OutlinedTextField(
                value = editableConfig.defaultTaxRate.toString(),
                onValueChange = { 
                    val rate = it.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    editableConfig = editableConfig.copy(defaultTaxRate = rate) 
                },
                label = { Text("Default Tax Rate (%)") },
                suffix = { Text("%") },
                modifier = Modifier.fillMaxWidth(),
                enabled = editableConfig.isTaxEnabled
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Enable Service Charge") },
                supportingContent = { Text("Commonly used in F&B (e.g. 10%)") },
                trailingContent = {
                    Switch(
                        checked = editableConfig.isServiceChargeEnabled,
                        onCheckedChange = { editableConfig = editableConfig.copy(isServiceChargeEnabled = it) }
                    )
                }
            )

            OutlinedTextField(
                value = editableConfig.serviceChargeRate.toString(),
                onValueChange = { 
                    val rate = it.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    editableConfig = editableConfig.copy(serviceChargeRate = rate) 
                },
                label = { Text("Service Charge Rate (%)") },
                suffix = { Text("%") },
                modifier = Modifier.fillMaxWidth(),
                enabled = editableConfig.isServiceChargeEnabled
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.infoContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Information",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onInfoContainer
                    )
                    Text(
                        "Individual product tax rates will override this default setting if specified in the Inventory section.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onInfoContainer
                    )
                }
            }
        }
    }
}

// Custom color extension for Info container if not in theme
private val ColorScheme.infoContainer: androidx.compose.ui.graphics.Color
    @Composable
    get() = primaryContainer.copy(alpha = 0.1f)

private val ColorScheme.onInfoContainer: androidx.compose.ui.graphics.Color
    @Composable
    get() = primary
