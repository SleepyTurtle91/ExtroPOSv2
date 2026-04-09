package com.extrotarget.extroposv2.core.hardware.printer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsbPrinter(
    private val context: Context,
    private val device: UsbDevice
) : PrinterInterface {
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var endpoint: UsbEndpoint? = null

    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        if (!usbManager.hasPermission(device)) {
            // Permission should be requested by the UI before calling connect
            return@withContext false
        }

        try {
            connection = usbManager.openDevice(device)
            usbInterface = device.getInterface(0)
            
            // Find the bulk out endpoint
            for (i in 0 until usbInterface!!.endpointCount) {
                val ep = usbInterface!!.getEndpoint(i)
                if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == UsbConstants.USB_DIR_OUT) {
                    endpoint = ep
                    break
                }
            }

            connection?.claimInterface(usbInterface, true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            connection?.releaseInterface(usbInterface)
            connection?.close()
        }
    }

    override suspend fun isConnected(): Boolean {
        return connection != null && endpoint != null
    }

    override suspend fun printReceipt(content: List<PrintCommand>) {
        withContext(Dispatchers.IO) {
            try {
                val bytes = EscPosEncoder.encode(content)
                connection?.bulkTransfer(endpoint, bytes, bytes.size, 5000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}