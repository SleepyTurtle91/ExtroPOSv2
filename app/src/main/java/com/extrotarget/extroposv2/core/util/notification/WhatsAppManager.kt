package com.extrotarget.extroposv2.core.util.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.config.AppConfig
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
     */
    fun sendOrderReadyNotification(order: LaundryOrder) {
        if (order.customerPhone.isBlank()) return

        val message = context.getString(
            R.string.whatsapp_order_ready_msg,
            order.customerName,
            order.id.takeLast(4),
            order.weightKg.toString(),
            CurrencyUtils.format(order.totalPrice)
        )

        launchWhatsApp(order.customerPhone, message)
    }

    private fun launchWhatsApp(phone: String, message: String) {
        try {
            // Format phone to international standard if it starts with 0
            val formattedPhone = if (phone.startsWith("0")) "6$phone" else phone
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val url = "${AppConfig.URLs.WHATSAPP_SEND_BASE}?phone=$formattedPhone&text=${URLEncoder.encode(message, "UTF-8")}"
                data = Uri.parse(url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
