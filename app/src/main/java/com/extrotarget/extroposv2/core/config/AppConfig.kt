package com.extrotarget.extroposv2.core.config

import java.util.Locale

object AppConfig {
    object Database {
        const val NAME = "extropos_v2_db"
    }

    object Network {
        const val LHDN_SANDBOX_URL = "https://preprod-api.myinvois.hasil.gov.my/"
        const val LHDN_PRODUCTION_URL = "https://api.myinvois.hasil.gov.my/"
        const val AUTOCOUNT_DEFAULT_URL = "http://localhost:8080/"
        
        const val SYNC_PORT = 8081
        const val ENDPOINT_SYNC_REALTIME = "/sync/realtime"
        const val ENDPOINT_SYNC_BRANCH = "/sync/branch"
        const val ENDPOINT_SYNC_MEMBER = "/sync/member/"
        const val ENDPOINT_SYNC_SALE = "/sync/sale"
        const val ENDPOINT_SYNC_STOCK = "/sync/stock"
        const val ENDPOINT_SYNC_TRANSFER = "/sync/transfer"
        const val ENDPOINT_SYNC_DATABASE = "/sync/database"
        const val HEADER_SYNC_TOKEN = "X-Sync-Token"

        fun getLhdnBaseUrl(isSandbox: Boolean): String {
            return if (isSandbox) LHDN_SANDBOX_URL else LHDN_PRODUCTION_URL
        }
    }

    object SaleStatus {
        const val COMPLETED = "COMPLETED"
        const val VOIDED = "VOIDED"
        const val REFUNDED = "REFUNDED"
        const val PENDING = "PENDING"
        const val SENT = "SENT"
        const val PREPARING = "PREPARING"
        const val READY = "READY"
        const val SERVED = "SERVED"
    }

    object PaymentMethod {
        const val CASH = "CASH"
        const val CARD = "CARD"
        const val DUITNOW = "DUITNOW"
        const val QR = "QR"
        const val PENDING = "PENDING"
    }

    object URLs {
        const val WHATSAPP_SEND_BASE = "https://api.whatsapp.com/send"
        const val VERIFY_SALE_BASE = "https://verify.extropos.com/sale/"
        const val LHDN_VIEWER_SANDBOX_URL = "https://preprod.myinvois.hasil.gov.my/documents/"
        const val LHDN_VIEWER_PRODUCTION_URL = "https://myinvois.hasil.gov.my/documents/"
        const val ORDER_WEB_BASE = "https://order.extropos.com/"

        fun getLhdnViewerUrl(isSandbox: Boolean): String {
            return if (isSandbox) LHDN_VIEWER_SANDBOX_URL else LHDN_VIEWER_PRODUCTION_URL
        }
    }

    object Security {
        const val CRYPTO_SALT = "extropos_v2_salt_2024"
    }

    object Locales {
        val DEFAULT_CURRENCY = Locale("en", "MY")
    }
}
