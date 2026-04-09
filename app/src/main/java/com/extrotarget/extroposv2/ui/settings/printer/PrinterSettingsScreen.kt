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
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import com.extrotarget.extroposv2.ui.settings.viewmodel.PrinterSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel = viewModel()
) {
    val currentConfig by viewModel.currentConfig.collectAsState()
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
        topBar = { TopAppBar(title = { Text("Printer Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Printer", style = MaterialTheme.typography.titleMedium)
                        if (currentConfig != null) {
                            Button(
                                onClick = { viewModel.testPrint() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Test Print", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    if (currentConfig != null) {
                        Text("Name: ${currentConfig?.name}")
                        Text("Type: ${currentConfig?.connectionType}")
                        Text("Address: ${currentConfig?.address}")
                    } else {
                        Text("No printer configured", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Text("Connect New Printer", style = MaterialTheme.typography.titleLarge)

            PrinterTypeButton(
                icon = Icons.Default.Bluetooth,
                label = "Bluetooth Printer",
                onClick = {
                    viewModel.discoverBluetoothDevices()
                    showBluetoothList = true
                }
            )

            PrinterTypeButton(
                icon = Icons.Default.Computer,
                label = "USB Printer",
                onClick = {
                    viewModel.discoverUsbDevices()
                    showUsbList = true
                }
            )

            PrinterTypeButton(
                icon = Icons.Default.Lan,
                label = "Network (IP) Printer",
                onClick = { showNetworkDialog = true }
            )
        }
    }

    if (showBluetoothList) {
        DeviceSelectionDialog(
            title = "Select Bluetooth Printer",
            devices = bluetoothDevices.map { 
                @SuppressLint("MissingPermission")
                val name = it.name ?: "Unknown Device"
                name to it.address 
            },
            onDismiss = { showBluetoothList = false },
            onSelect = { name, address ->
                viewModel.savePrinterConfig(name, "BLUETOOTH", address)
                showBluetoothList = false
            }
        )
    }

    if (showUsbList) {
        DeviceSelectionDialog(
            title = "Select USB Printer",
            devices = usbDevices.map { "USB Printer ${it.deviceId}" to it.deviceName },
            onDismiss = { showUsbList = false },
            onSelect = { name, address ->
                viewModel.savePrinterConfig(name, "USB", address)
                showUsbList = false
            }
        )
    }

    if (showNetworkDialog) {
        NetworkPrinterDialog(
            onDismiss = { showNetworkDialog = false },
            onConfirm = { name, ip, port ->
                viewModel.savePrinterConfig(name, "NETWORK", ip, port.toIntOrNull() ?: 9100)
                showNetworkDialog = false
            }
        )
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
    onSelect: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (devices.isEmpty()) {
                Text("No devices found. Ensure printer is on and paired.")
            } else {
                LazyColumn {
                    items(devices) { (name, address) ->
                        ListItem(
                            headlineContent = { Text(name) },
                            supportingContent = { Text(address) },
                            modifier = Modifier.clickable { onSelect(name, address) }
                        )
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
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("Network Printer") }
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("9100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Network Printer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("IP Address (e.g. 192.168.1.100)") })
                OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Port") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, ip, port) }, enabled = ip.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}