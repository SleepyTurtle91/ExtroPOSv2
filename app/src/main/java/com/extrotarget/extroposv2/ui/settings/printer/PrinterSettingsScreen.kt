package com.extrotarget.extroposv2.ui.settings.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.hardware.usb.UsbDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import com.extrotarget.extroposv2.ui.settings.viewmodel.PrinterSettingsViewModel

import com.extrotarget.extroposv2.ui.util.BluetoothPermissionWrapper
import androidx.compose.material.icons.filled.Delete
import com.extrotarget.extroposv2.core.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel = hiltViewModel()
) {
    val printers by viewModel.allPrinters.collectAsState()
    val bluetoothDevices by viewModel.availableBluetoothDevices.collectAsState()
    val usbDevices by viewModel.availableUsbDevices.collectAsState()
    val printStatus by viewModel.printStatus.collectAsState()

    var showBluetoothList by remember { mutableStateOf(false) }
    var showUsbList by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(printStatus) {
        printStatus?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Printer Routing & Config") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Configured Printers", style = MaterialTheme.typography.titleLarge)
            }

            if (printers.isEmpty()) {
                item {
                    Text("No printers configured", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                items(printers) { config ->
                    PrinterConfigCard(
                        config = config,
                        onTestPrint = { viewModel.testPrint(config) },
                        onDelete = { viewModel.deletePrinter(config.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Add New Printer", style = MaterialTheme.typography.titleLarge)
            }

            item {
                PrinterTypeButton(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth Printer",
                    onClick = {
                        showBluetoothList = true
                    }
                )
            }

            item {
                PrinterTypeButton(
                    icon = Icons.Default.Computer,
                    label = "USB Printer",
                    onClick = {
                        viewModel.discoverUsbDevices()
                        showUsbList = true
                    }
                )
            }

            item {
                PrinterTypeButton(
                    icon = Icons.Default.Lan,
                    label = "Network (IP) Printer",
                    onClick = { showNetworkDialog = true }
                )
            }
        }
    }

    if (showBluetoothList) {
        BluetoothPermissionWrapper(
            onPermissionGranted = {
                LaunchedEffect(Unit) {
                    viewModel.discoverBluetoothDevices()
                }
                DeviceSelectionDialog(
                    title = "Select Bluetooth Printer",
                    devices = bluetoothDevices.map { 
                        @SuppressLint("MissingPermission")
                        val name = it.name ?: "Unknown Device"
                        name to it.address 
                    },
                    onDismiss = { showBluetoothList = false },
                    onSelect = { name, address, tag ->
                        viewModel.savePrinterConfig(name, "BLUETOOTH", address, tag = tag)
                        showBluetoothList = false
                    }
                )
            },
            onPermissionDenied = {
                showBluetoothList = false
                // Optionally show a toast or message
            }
        )
    }

    if (showUsbList) {
        DeviceSelectionDialog(
            title = "Select USB Printer",
            devices = usbDevices.map { "USB Printer ${it.deviceId}" to it.deviceName },
            onDismiss = { showUsbList = false },
            onSelect = { name, address, tag ->
                viewModel.savePrinterConfig(name, "USB", address, tag = tag)
                showUsbList = false
            }
        )
    }

    if (showNetworkDialog) {
        NetworkPrinterDialog(
            onDismiss = { showNetworkDialog = false },
            onConfirm = { name, ip, port, tag ->
                viewModel.savePrinterConfig(name, "NETWORK", ip, port.toIntOrNull() ?: 9100, tag = tag)
                showNetworkDialog = false
            }
        )
    }
}

@Composable
fun PrinterConfigCard(
    config: PrinterConfig,
    onTestPrint: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (config.isDefault) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${config.connectionType} • ${config.address}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    IconButton(onClick = onTestPrint) {
                        Icon(Icons.Default.Print, contentDescription = "Test Print")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(config.printerTag ?: "RECEIPT") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
                if (config.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Default Receipt Printer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PrinterTypeButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
fun DeviceSelectionDialog(
    title: String,
    devices: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSelect: (String, String, String) -> Unit
) {
    var selectedTag by remember { mutableStateOf("RECEIPT") }
    val tags = listOf("RECEIPT", "KITCHEN", "BAR", "GENERAL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Select Tag / Role", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (devices.isEmpty()) {
                    Text("No devices found. Ensure printer is on and paired.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(devices) { (name, address) ->
                            ListItem(
                                headlineContent = { Text(name) },
                                supportingContent = { Text(address) },
                                modifier = Modifier.clickable { onSelect(name, address, selectedTag) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun NetworkPrinterDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("Network Printer") }
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("9100") }
    var selectedTag by remember { mutableStateOf("KITCHEN") }
    val tags = listOf("RECEIPT", "KITCHEN", "BAR", "GENERAL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Network Printer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("IP Address (e.g. 192.168.1.100)") })
                OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Port") })
                
                Text("Select Tag / Role", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, ip, port, selectedTag) }, enabled = ip.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
