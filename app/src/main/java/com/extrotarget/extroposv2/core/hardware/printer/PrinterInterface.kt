package com.extrotarget.extroposv2.core.hardware.printer

interface PrinterInterface {
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun isConnected(): Boolean
    suspend fun printReceipt(content: List<PrintCommand>, charWidth: Int = 32): Boolean
    suspend fun getStatus(): PrinterStatus = PrinterStatus.READY
}


sealed class PrintCommand {
    data class Text(val content: String, val alignment: Alignment = Alignment.LEFT, val isBold: Boolean = false) : PrintCommand()
    data class BigText(val content: String, val alignment: Alignment = Alignment.LEFT) : PrintCommand()
    data class Header(val content: String) : PrintCommand()
    data class Image(val bitmap: android.graphics.Bitmap, val alignment: Alignment = Alignment.CENTER) : PrintCommand()
    object Divider : PrintCommand()
    object Buzzer : PrintCommand()
    data class Feed(val lines: Int = 1) : PrintCommand()
    object Cut : PrintCommand()
    object DrawerKick : PrintCommand()
    data class QRCode(val content: String) : PrintCommand()
    data class Raw(val bytes: ByteArray) : PrintCommand()
}

enum class PrinterStatus {
    READY, OUT_OF_PAPER, ERROR, OFFLINE
}


enum class Alignment {
    LEFT, CENTER, RIGHT
}