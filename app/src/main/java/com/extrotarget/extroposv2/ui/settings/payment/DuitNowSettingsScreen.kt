package com.extrotarget.extroposv2.ui.settings.payment

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
import com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig
import com.extrotarget.extroposv2.ui.settings.payment.viewmodel.DuitNowSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuitNowSettingsScreen(
    viewModel: DuitNowSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val config by viewModel.config.collectAsState()
    var editableConfig by remember(config) { mutableStateOf(config) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DuitNow QR Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveConfig(editableConfig) },
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
                "Configure your DuitNow Merchant details to generate dynamic QR codes for payments.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = editableConfig.merchantId,
                onValueChange = { editableConfig = editableConfig.copy(merchantId = it) },
                label = { Text("Merchant ID / DuitNow ID") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Enter your 26-digit ID or the ID provided by your bank") }
            )

            OutlinedTextField(
                value = editableConfig.merchantName,
                onValueChange = { editableConfig = editableConfig.copy(merchantName = it) },
                label = { Text("Merchant Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. EXTROPOS SDN BHD") }
            )

            OutlinedTextField(
                value = editableConfig.city,
                onValueChange = { editableConfig = editableConfig.copy(city = it) },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Enable DuitNow QR") },
                supportingContent = { Text("Allow customers to pay via dynamic QR scan") },
                trailingContent = {
                    Switch(
                        checked = editableConfig.isEnabled,
                        onCheckedChange = { editableConfig = editableConfig.copy(isEnabled = it) }
                    )
                }
            )
        }
    }
}
