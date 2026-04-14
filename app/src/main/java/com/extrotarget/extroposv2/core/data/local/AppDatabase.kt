package com.extrotarget.extroposv2.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.extrotarget.extroposv2.core.data.local.converter.Converters
import com.extrotarget.extroposv2.core.data.local.dao.AuditDao
import com.extrotarget.extroposv2.core.data.local.dao.AutoCountDao
import com.extrotarget.extroposv2.core.data.local.dao.CategoryDao
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.CommissionRecordDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.StaffDao
import com.extrotarget.extroposv2.core.data.model.AuditLog
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.local.dao.settings.ReceiptDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.PaymentMethodDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.TaxDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.DuitNowDao
import com.extrotarget.extroposv2.core.data.local.dao.lhdn.LhdnDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.CarWashDao
import com.extrotarget.extroposv2.core.data.local.dao.dobi.LaundryDao
import com.extrotarget.extroposv2.core.data.local.dao.fnb.TableDao
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryOrder
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnToken
import com.extrotarget.extroposv2.core.data.model.lhdn.SaleEInvoiceSubmission
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import com.extrotarget.extroposv2.core.data.model.settings.AutoCountConfig
import com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig
import com.extrotarget.extroposv2.core.data.model.settings.PaymentMethod
import com.extrotarget.extroposv2.core.data.model.settings.TaxConfig
import com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig

@Database(
    entities = [
        Product::class,
        Category::class,
        Sale::class,
        SaleItem::class,
        StockMovement::class,
        Staff::class,
        CommissionRecord::class,
        PrinterConfig::class,
        Table::class,
        LaundryOrder::class,
        CarWashJob::class,
        ReceiptConfig::class,
        PaymentMethod::class,
        TaxConfig::class,
        LhdnConfig::class,
        SaleEInvoiceSubmission::class,
        LhdnToken::class,
        DuitNowConfig::class,
        AuditLog::class,
        AutoCountConfig::class
    ],
    version = 18,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun saleDao(): SaleDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun staffDao(): StaffDao
    abstract fun commissionRecordDao(): CommissionRecordDao
    abstract fun auditDao(): AuditDao
    abstract fun printerDao(): PrinterDao
    abstract fun tableDao(): TableDao
    abstract fun laundryDao(): LaundryDao
    abstract fun carWashDao(): CarWashDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun taxDao(): TaxDao
    abstract fun lhdnDao(): LhdnDao
    abstract fun duitNowDao(): DuitNowDao
    abstract fun autoCountDao(): AutoCountDao

    companion object {
        const val DATABASE_NAME = "extro_pos_db"
    }
}