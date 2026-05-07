package com.extrotarget.extroposv2.core.hardware.printer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class NetworkPrinter(private val ipAddress: String, private val port: Int = 9100) : PrinterInterface {
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null

    override suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = Socket()
            socket?.connect(InetSocketAddress(ipAddress, port), 5000) // 5s timeout
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

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            socket?.isConnected == true && !socket!!.isClosed
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun printReceipt(content: List<PrintCommand>, charWidth: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (socket == null || !socket!!.isConnected) {
                if (!connect()) return@withContext false
            }
            
            val bytes = EscPosEncoder.encode(content, charWidth)
            outputStream?.write(bytes)
            outputStream?.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}