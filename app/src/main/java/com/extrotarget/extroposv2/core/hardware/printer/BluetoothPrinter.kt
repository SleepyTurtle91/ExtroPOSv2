package com.extrotarget.extroposv2.core.hardware.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.*

class BluetoothPrinter(private val deviceAddress: String) : PrinterInterface {
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-0000-1000-800000805F9B")

    @SuppressLint("MissingPermission")
    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            outputStream = socket?.outputStream
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun isConnected(): Boolean {
        return socket?.isConnected ?: false
    }

    override suspend fun printReceipt(content: List<PrintCommand>) {
        withContext(Dispatchers.IO) {
            try {
                val bytes = EscPosEncoder.encode(content)
                outputStream?.write(bytes)
                outputStream?.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}