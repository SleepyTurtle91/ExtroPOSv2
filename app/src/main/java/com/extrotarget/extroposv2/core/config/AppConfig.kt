package com.extrotarget.extroposv2.core.config

import com.extrotarget.extroposv2.BuildConfig

/**
 * Centralized configuration for the application to avoid hardcoded values.
 */
object AppConfig {
    
    object Database {
        const val NAME = "extro_pos_db"
        const val BACKUP_PREFIX = "extropos_backup_"
        const val MASTER_DATA_PREFIX = "extropos_master_data_"
    }

    object Network {
        const val LHDN_SANDBOX_URL = "https://preprod-api.myinvois.hasil.gov.my/"
        const val LHDN_PRODUCTION_URL = "https://api.myinvois.hasil.gov.my/"
        const val AUTOCOUNT_DEFAULT_URL = "http://localhost:8080/"
        
        // Sync API Endpoints
        const val ENDPOINT_SYNC_MEMBER = "/sync/branch/member/"
        const val ENDPOINT_SYNC_SALE = "/sync/branch/sale"
        const val ENDPOINT_SYNC_STOCK = "/sync/branch/stock"
        const val ENDPOINT_SYNC_TRANSFER = "/sync/branch/transfer"
        const val ENDPOINT_SYNC_DATABASE = "/sync/database"
        const val ENDPOINT_SYNC_REALTIME = "/sync/realtime"
        
        const val SYNC_PORT = 8080
        const val SYNC_SERVICE_TYPE = "_extropos_sync._tcp."
        const val SYNC_SERVICE_NAME = "ExtroPOS_Sync"
        const val HEADER_SYNC_TOKEN = "X-Sync-Token"
        
        fun getLhdnBaseUrl(isSandbox: Boolean): String {
            return if (isSandbox) LHDN_SANDBOX_URL else LHDN_PRODUCTION_URL
        }
    }

    object URLs {
        const val LHDN_VIEWER_SANDBOX = "https://preprod.myinvois.hasil.gov.my"
        const val LHDN_VIEWER_PRODUCTION = "https://myinvois.hasil.gov.my"
        const val VERIFY_SALE_BASE = "https://extropos.com/verify/"
        const val ORDER_WEB_BASE = "https://order.extropos.com/order"
        const val WHATSAPP_SEND_BASE = "https://api.whatsapp.com/send"
        
        fun getLhdnViewerUrl(isSandbox: Boolean): String {
            return if (isSandbox) LHDN_VIEWER_SANDBOX else LHDN_VIEWER_PRODUCTION
        }
    }

    object Security {
        const val CRYPTO_SALT = "EXTRO_SALT_2024"
        const val KEY_LHDN_CLIENT_ID = "lhdn_client_id"
        const val KEY_LHDN_CLIENT_SECRET = "lhdn_client_secret"
        const val KEY_DUITNOW_MERCHANT_ID = "duitnow_merchant_id"
    }

    object Locales {
        val MALAYSIA = java.util.Locale("en", "MY")
        val DEFAULT_CURRENCY = MALAYSIA
    }

    object SaleStatus {
        const val PENDING = "PENDING"
        const val COMPLETED = "COMPLETED"
        const val VOID = "VOID"
        const val SENT = "SENT" // For items sent to kitchen
    }

    object PaymentMethod {
        const val CASH = "CASH"
        const val CARD = "CARD"
        const val QR = "QR"
        const val DUITNOW = "DUITNOW"
        const val PENDING = "PENDING"
    }
}
