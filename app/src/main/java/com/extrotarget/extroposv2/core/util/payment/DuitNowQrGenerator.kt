package com.extrotarget.extroposv2.core.util.payment

import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * Utility to generate EMVCo-compatible DuitNow QR strings for Malaysian merchants.
 */
object DuitNowQrGenerator {

    /**
     * Generates a dynamic DuitNow QR string.
     * @param merchantId The merchant's DuitNow ID (ID Type 02/03 for Merchant ID)
     * @param amount The transaction amount
     * @param merchantName The name of the merchant (max 25 chars)
     * @param city The merchant city (max 15 chars)
     */
    fun generateDynamicQr(
        merchantId: String,
        amount: BigDecimal,
        merchantName: String = "EXTROPOS MERCHANT",
        city: String = "KUALA LUMPUR"
    ): String {
        val sb = StringBuilder()

        // 00: Payload Format Indicator (01)
        sb.append(formatTag("00", "01"))

        // 01: Point of Initiation Method (12 for Dynamic)
        sb.append(formatTag("01", "12"))

        // 26: Merchant Account Information - DuitNow (Reversed logic for BNM Spec)
        val merchantData = StringBuilder()
        merchantData.append(formatTag("00", "my.com.duitnow"))
        merchantData.append(formatTag("01", "00000000000000000000")) // Merchant ID Placeholder
        merchantData.append(formatTag("02", merchantId)) // ID Type 02/03 for Merchant ID
        sb.append(formatTag("26", merchantData.toString()))

        // 52: Merchant Category Code (5812 for Eating Places/Restaurants)
        sb.append(formatTag("52", "5812"))

        // 53: Transaction Currency (458 for MYR)
        sb.append(formatTag("53", "458"))

        // 54: Transaction Amount
        val df = java.text.DecimalFormat("0.00")
        sb.append(formatTag("54", df.format(amount)))

        // 58: Country Code (MY)
        sb.append(formatTag("58", "MY"))

        // 59: Merchant Name
        sb.append(formatTag("59", merchantName.take(25)))

        // 60: Merchant City
        sb.append(formatTag("60", city.take(15)))

        // 62: Additional Data Field Template (Unique Reference)
        val reference = "REF${System.currentTimeMillis() % 1000000}"
        sb.append(formatTag("62", formatTag("01", reference)))

        // 63: CRC (Checksum)
        val partialQr = sb.toString() + "6304"
        val crc = calculateCrc16(partialQr)
        return partialQr + crc
    }

    private fun formatTag(tag: String, value: String): String {
        val length = value.length.toString().padStart(2, '0')
        return "$tag$length$value"
    }

    private fun calculateCrc16(data: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021

        for (b in data.toByteArray()) {
            for (i in 0..7) {
                val bit = (b.toInt() shr (7 - i) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor polynomial
            }
        }
        crc = crc and 0xFFFF
        return Integer.toHexString(crc).uppercase().padStart(4, '0')
    }
}