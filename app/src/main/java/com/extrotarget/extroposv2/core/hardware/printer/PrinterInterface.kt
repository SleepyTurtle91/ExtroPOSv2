package com.extrotarget.extroposv2.core.hardware.printer

interface PrinterInterface {
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun isConnected(): Boolean
    suspend fun printReceipt(content: List<PrintCommand>)
}

sealed class PrintCommand {
    data class Text(val content: String, val alignment: Alignment = Alignment.LEFT, val isBold: Boolean = false) : PrintCommand()
    data class Header(val content: String) : PrintCommand()
    object Divider : PrintCommand()
    data class Feed(val lines: Int = 1) : PrintCommand()
    object Cut : PrintCommand()
    object DrawerKick : PrintCommand()
    data class QRCode(val content: String) : PrintCommand()
    data class Raw(val bytes: ByteArray) : PrintCommand()
}

enum class Alignment {
    LEFT, CENTER, RIGHT
}