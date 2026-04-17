package com.extrotarget.extroposv2.ui.settings.receipt

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
import com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig
import com.extrotarget.extroposv2.ui.settings.viewmodel.ReceiptSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptSettingsScreen(
    viewModel: ReceiptSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val config by viewModel.receiptConfig.collectAsState()
    var editableConfig by remember(config) { mutableStateOf(config) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Customization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.updateConfig(editableConfig) },
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
            OutlinedTextField(
                value = editableConfig.storeName,
                onValueChange = { editableConfig = editableConfig.copy(storeName = it) },
                label = { Text("Store Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editableConfig.address ?: "",
                onValueChange = { editableConfig = editableConfig.copy(address = it) },
                label = { Text("Store Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = editableConfig.phone ?: "",
                onValueChange = { editableConfig = editableConfig.copy(phone = it) },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editableConfig.brn ?: "",
                onValueChange = { editableConfig = editableConfig.copy(brn = it) },
                label = { Text("Business Registration Number (BRN)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editableConfig.sstId ?: "",
                onValueChange = { editableConfig = editableConfig.copy(sstId = it) },
                label = { Text("Tax Registration Number (e.g. SST ID)") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            OutlinedTextField(
                value = editableConfig.headerMessage ?: "",
                onValueChange = { editableConfig = editableConfig.copy(headerMessage = it) },
                label = { Text("Header Message (e.g. Welcome!)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editableConfig.footerMessage ?: "",
                onValueChange = { editableConfig = editableConfig.copy(footerMessage = it) },
                label = { Text("Footer Message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Show Tax Summary") },
                supportingContent = { Text("Display tax breakdown on receipt") },
                trailingContent = {
                    Switch(
                        checked = editableConfig.showTaxSummary,
                        onCheckedChange = { editableConfig = editableConfig.copy(showTaxSummary = it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Show Cash Rounding") },
                supportingContent = { Text("Display 5-sen rounding on receipt") },
                trailingContent = {
                    Switch(
                        checked = editableConfig.showRounding,
                        onCheckedChange = { editableConfig = editableConfig.copy(showRounding = it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Show LHDN QR Code") },
                supportingContent = { Text("Include e-Invoice validation QR on receipt") },
                trailingContent = {
                    Switch(
                        checked = editableConfig.showLhdnQr,
                        onCheckedChange = { editableConfig = editableConfig.copy(showLhdnQr = it) }
                    )
                }
            )

            HorizontalDivider()
            Text("Paper Size", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                com.extrotarget.extroposv2.core.data.model.settings.PaperWidth.entries.forEach { width ->
                    FilterChip(
                        selected = editableConfig.paperWidth == width,
                        onClick = { editableConfig = editableConfig.copy(paperWidth = width) },
                        label = { Text(width.value.toString() + "mm") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}