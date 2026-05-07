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

    override suspend fun printReceipt(content: List<PrintCommand>, charWidth: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val bytes = EscPosEncoder.encode(content, charWidth)
            
            // Chunked transfer to handle large receipts (e.g. 64 byte chunks are safe for most USB printers)
            val chunkSize = 64 
            var offset = 0
            while (offset < bytes.size) {
                val length = (bytes.size - offset).coerceAtMost(chunkSize)
                val result = connection?.bulkTransfer(endpoint, bytes, offset, length, 5000)
                if (result == -1) return@withContext false
                offset += length
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}