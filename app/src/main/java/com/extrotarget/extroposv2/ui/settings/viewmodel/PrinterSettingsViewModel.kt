package com.extrotarget.extroposv2.ui.settings.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import com.extrotarget.extroposv2.core.hardware.printer.BluetoothPrinter
import com.extrotarget.extroposv2.core.hardware.printer.NetworkPrinter
import com.extrotarget.extroposv2.core.hardware.printer.PrintCommand
import com.extrotarget.extroposv2.core.hardware.printer.UsbPrinter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterSettingsViewModel @Inject constructor(
    private val printerDao: PrinterDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _availableBluetoothDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val availableBluetoothDevices: StateFlow<List<BluetoothDevice>> = _availableBluetoothDevices.asStateFlow()

    private val _availableUsbDevices = MutableStateFlow<List<UsbDevice>>(emptyList())
    val availableUsbDevices: StateFlow<List<UsbDevice>> = _availableUsbDevices.asStateFlow()

    private val _printStatus = MutableStateFlow<String?>(null)
    val printStatus: StateFlow<String?> = _printStatus.asStateFlow()

    val currentConfig: StateFlow<PrinterConfig?> = printerDao.getDefaultPrinter()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @SuppressLint("MissingPermission")
    fun discoverBluetoothDevices() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            _availableBluetoothDevices.value = bluetoothAdapter.bondedDevices.toList()
        }
    }

    fun discoverUsbDevices() {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        _availableUsbDevices.value = usbManager.deviceList.values.toList()
    }

    fun savePrinterConfig(name: String, type: String, address: String, port: Int = 9100) {
        viewModelScope.launch {
            val config = PrinterConfig(
                name = name,
                connectionType = type,
                address = address,
                port = port
            )
            printerDao.insertConfig(config)
        }
    }

    fun testPrint() {
        viewModelScope.launch {
            val config = currentConfig.value ?: return@launch
            _printStatus.value = "Connecting to ${config.name}..."
            
            val printer = when (config.connectionType) {
                "BLUETOOTH" -> BluetoothPrinter(config.address)
                "USB" -> {
                    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                    val device = usbManager.deviceList.values.find { it.deviceName == config.address }
                    if (device != null) UsbPrinter(context, device) else null
                }
                "NETWORK" -> NetworkPrinter(config.address, config.port)
                else -> null
            }

            if (printer != null) {
                try {
                    if (printer.connect()) {
                        _printStatus.value = "Printing test page..."
                        val testContent = listOf(
                            PrintCommand.Header("ExtroPOS v2"),
                            PrintCommand.Text("Test Print Successful!", isBold = true),
                            PrintCommand.Divider,
                            PrintCommand.Text("Connection: ${config.connectionType}"),
                            PrintCommand.Text("Address: ${config.address}"),
                            PrintCommand.Text("Port: ${config.port}"),
                            PrintCommand.Divider,
                            PrintCommand.Feed(3),
                            PrintCommand.Cut
                        )
                        printer.printReceipt(testContent)
                        _printStatus.value = "Test print sent successfully"
                    } else {
                        _printStatus.value = "Failed to connect to printer"
                    }
                } catch (e: Exception) {
                    _printStatus.value = "Error: ${e.message}"
                } finally {
                    printer.disconnect()
                }
            } else {
                _printStatus.value = "Unsupported printer type"
            }
        }
    }

    fun clearStatus() {
        _printStatus.value = null
    }
}
