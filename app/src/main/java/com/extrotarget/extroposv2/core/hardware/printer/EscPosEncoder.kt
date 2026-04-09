package com.extrotarget.extroposv2.core.hardware.printer

import java.io.ByteArrayOutputStream

/**
 * Basic ESC/POS encoder for POSMAC and HPRT printers.
 */
object EscPosEncoder {
    private const val ESC: Byte = 0x1B
    private const val GS: Byte = 0x1D
    private const val LF: Byte = 0x0A

    fun encode(commands: List<PrintCommand>): ByteArray {
        val out = ByteArrayOutputStream()

        // Initialize printer
        out.write(byteArrayOf(ESC, 0x40))

        commands.forEach { command ->
            when (command) {
                is PrintCommand.Header -> {
                    out.write(setAlignment(Alignment.CENTER))
                    out.write(setBold(true))
                    out.write(setFontSize(2))
                    out.write(command.content.toByteArray())
                    out.write(LF.toInt())
                    // Reset to normal
                    out.write(setFontSize(1))
                    out.write(setBold(false))
                }
                is PrintCommand.Text -> {
                    out.write(setAlignment(command.alignment))
                    out.write(setBold(command.isBold))
                    out.write(command.content.toByteArray())
                    out.write(LF.toInt())
                }
                is PrintCommand.Divider -> {
                    out.write(setAlignment(Alignment.CENTER))
                    out.write("--------------------------------".toByteArray())
                    out.write(LF.toInt())
                }
                is PrintCommand.Feed -> {
                    repeat(command.lines) { out.write(LF.toInt()) }
                }
                is PrintCommand.Cut -> {
                    out.write(byteArrayOf(GS, 0x56, 0x01))
                }
                is PrintCommand.QRCode -> {
                    // Simplified QR for common ESC/POS (HPRT/POSMAC)
                    out.write(setAlignment(Alignment.CENTER))
                    out.write(encodeQRCode(command.content))
                }
                is PrintCommand.Raw -> {
                    out.write(command.bytes)
                }
            }
        }

        return out.toByteArray()
    }

    private fun setAlignment(alignment: Alignment): ByteArray {
        val value = when (alignment) {
            Alignment.LEFT -> 0
            Alignment.CENTER -> 1
            Alignment.RIGHT -> 2
        }
        return byteArrayOf(ESC, 0x61, value.toByte())
    }

    private fun setBold(isBold: Boolean): ByteArray {
        return byteArrayOf(ESC, 0x45, if (isBold) 1 else 0)
    }

    private fun setFontSize(size: Int): ByteArray {
        // 0x11 is double height and double width
        val value = if (size > 1) 0x11 else 0x00
        return byteArrayOf(GS, 0x21, value.toByte())
    }

    private fun encodeQRCode(content: String): ByteArray {
        val out = ByteArrayOutputStream()
        val data = content.toByteArray()
        val pL = (data.size + 3) % 256
        val pH = (data.size + 3) / 256

        // QR Code Function 180: Set cell size
        out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06))
        // QR Code Function 181: Set error correction level (M)
        out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31))
        // QR Code Function 180: Store data
        out.write(byteArrayOf(GS, 0x28, 0x6B, pL.toByte(), pH.toByte(), 0x31, 0x50, 0x30))
        out.write(data)
        // QR Code Function 181: Print symbol
        out.write(byteArrayOf(GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30))
        
        return out.toByteArray()
    }
}