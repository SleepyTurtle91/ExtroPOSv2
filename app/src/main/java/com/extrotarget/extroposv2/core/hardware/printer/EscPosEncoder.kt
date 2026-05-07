package com.extrotarget.extroposv2.core.hardware.printer

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream

/**
 * Enhanced ESC/POS encoder for POSMAC and HPRT printers.
 */
object EscPosEncoder {
    private const val ESC: Byte = 0x1B
    private const val GS: Byte = 0x1D
    private const val LF: Byte = 0x0A

    fun encode(commands: List<PrintCommand>, charWidth: Int = 32): ByteArray {
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
                is PrintCommand.BigText -> {
                    out.write(setAlignment(command.alignment))
                    out.write(setFontSize(2)) // Double height/width
                    out.write(setBold(true))
                    out.write(command.content.toByteArray())
                    out.write(LF.toInt())
                    // Reset
                    out.write(setFontSize(1))
                    out.write(setBold(false))
                }
                is PrintCommand.Text -> {
                    out.write(setAlignment(command.alignment))
                    out.write(setBold(command.isBold))
                    out.write(command.content.toByteArray())
                    out.write(LF.toInt())
                }
                is PrintCommand.Image -> {
                    out.write(setAlignment(command.alignment))
                    out.write(encodeImage(command.bitmap))
                }
                is PrintCommand.Divider -> {
                    out.write(setAlignment(Alignment.CENTER))
                    out.write("-".repeat(charWidth).toByteArray())
                    out.write(LF.toInt())
                }
                is PrintCommand.Buzzer -> {
                    // GS ( A pL pH n c t
                    // Trigger buzzer/beeper
                    out.write(byteArrayOf(GS, 0x28, 0x41, 0x04, 0x00, 0x30, 0x02, 0x01, 0x03))
                }
                is PrintCommand.Feed -> {
                    repeat(command.lines) { out.write(LF.toInt()) }
                }
                is PrintCommand.Cut -> {
                    out.write(byteArrayOf(GS, 0x56, 0x01))
                }
                is PrintCommand.DrawerKick -> {
                    // Standard ESC/POS drawer kick command for RJ11 (Pin 2)
                    out.write(byteArrayOf(ESC, 0x70, 0x00, 0x19, 0xFA.toByte()))
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

    /**
     * Encodes a bitmap to ESC/POS bit image format (GS v 0).
     */
    private fun encodeImage(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8
        val out = ByteArrayOutputStream()

        // GS v 0 m xL xH yL yH d1...dk
        out.write(byteArrayOf(GS, 0x76, 0x30, 0x00))
        out.write(widthBytes % 256)
        out.write(widthBytes / 256)
        out.write(height % 256)
        out.write(height / 256)

        for (y in 0 until height) {
            for (x in 0 until widthBytes) {
                var byteValue = 0
                for (b in 0 until 8) {
                    val pixelX = x * 8 + b
                    if (pixelX < width) {
                        val pixel = bitmap.getPixel(pixelX, y)
                        val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                        if (gray < 128) { // Black threshold
                            byteValue = byteValue or (1 shl (7 - b))
                        }
                    }
                }
                out.write(byteValue)
            }
        }
        return out.toByteArray()
    }
}
