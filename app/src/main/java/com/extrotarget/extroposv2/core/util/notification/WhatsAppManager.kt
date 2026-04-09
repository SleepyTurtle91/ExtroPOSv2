package com.extrotarget.extroposv2.core.util.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryOrder
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Generates a WhatsApp deep link for a laundry order notification.
     * Localization: Bahasa Melayu (BM) is the default for Malaysian SMEs.
     */
    fun sendOrderReadyNotification(order: LaundryOrder) {
        if (order.customerPhone.isBlank()) return

        val message = """
            *EXTRO POS: Pesanan Dobi Sedia Diambil!* 🧺
            
            Hai ${order.customerName},
            
            Pesanan anda (#${order.id.takeLast(4)}) telah siap diproses dan sedia untuk diambil.
            
            *Butiran:*
            Berat: ${order.weightKg} KG
            Jumlah: ${CurrencyUtils.format(order.totalPrice)}
            
            Terima kasih kerana menggunakan perkhidmatan kami!
        """.trimIndent()

        launchWhatsApp(order.customerPhone, message)
    }

    private fun launchWhatsApp(phone: String, message: String) {
        try {
            // Format phone to international standard if it starts with 0
            val formattedPhone = if (phone.startsWith("0")) "6$phone" else phone
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${URLEncoder.encode(message, "UTF-8")}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
