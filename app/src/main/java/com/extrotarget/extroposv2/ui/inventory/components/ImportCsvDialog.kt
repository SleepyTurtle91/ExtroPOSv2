package com.extrotarget.extroposv2.ui.inventory.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.extrotarget.extroposv2.ui.inventory.viewmodel.InventoryImportViewModel

@Composable
fun ImportCsvDialog(
    viewModel: InventoryImportViewModel = viewModel(),
    onDismiss: () -> Unit,
    onImportSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importCsv(it) }
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message?.startsWith("Successfully") == true) {
            onImportSuccess()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Import Products") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Select a CSV file to import products in bulk. The CSV should have headers in this order:",
                    style = MaterialTheme.typography.bodySmall
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "Name, SKU, Barcode, Price, TaxRate, Stock, MinStock, CategoryID, Description, PrinterTag",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
                
                uiState.message?.let {
                    Text(
                        text = it,
                        color = if (uiState.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { pickerLauncher.launch(arrayOf("text/comma-separated-values", "text/csv")) },
                enabled = !uiState.isLoading
            ) {
                Text("Select CSV File")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}