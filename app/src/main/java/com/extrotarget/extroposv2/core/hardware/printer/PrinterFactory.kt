package com.extrotarget.extroposv2.core.hardware.printer

import android.content.Context
import android.hardware.usb.UsbManager
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val printBuffer: PrintBuffer
) {
    fun create(config: PrinterConfig): PrinterInterface? {
        val basePrinter = when (config.connectionType) {
            "INTERNAL" -> {
                try {
                    val clazz = Class.forName("com.extrotarget.extroposv2.core.hardware.printer.IminPrinter")
                    val constructor = clazz.getConstructor(Context::class.java)
                    constructor.newInstance(context) as PrinterInterface
                } catch (e: Exception) {
                    null
                }
            }
            "BLUETOOTH" -> BluetoothPrinter(config.address)
            "NETWORK" -> NetworkPrinter(config.address, config.port)
            "USB" -> {
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                val device = usbManager.deviceList.values.find { it.deviceName == config.address }
                if (device != null) UsbPrinter(context, device) else null
            }
            else -> null
        }

        return basePrinter?.let { ResilientPrinter(it, printBuffer, config.id) }
    }
}
