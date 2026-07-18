package com.extrotarget.extroposv2.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.CategoryDao
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.SupplierDao
import com.extrotarget.extroposv2.core.data.local.dao.PurchaseOrderDao
import com.extrotarget.extroposv2.core.data.local.dao.RefundDao
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.CarWashDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.CommissionRecordDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.StaffDao
import com.extrotarget.extroposv2.core.data.local.dao.dobi.LaundryDao
import com.extrotarget.extroposv2.core.data.local.dao.fnb.TableDao
import com.extrotarget.extroposv2.core.data.local.dao.fnb.FnbModifierDao
import com.extrotarget.extroposv2.core.data.local.dao.fnb.FnbStationDao
import com.extrotarget.extroposv2.core.data.local.dao.OfflineQueueDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.PaymentMethodDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.TaxDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.ReceiptDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.DuitNowDao
import com.extrotarget.extroposv2.core.data.local.dao.lhdn.LhdnDao
import com.extrotarget.extroposv2.core.data.local.dao.AutoCountDao
import com.extrotarget.extroposv2.core.data.local.dao.ShiftDao
import com.extrotarget.extroposv2.core.data.local.dao.CashMovementDao
import com.extrotarget.extroposv2.core.data.local.dao.BranchDao
import com.extrotarget.extroposv2.core.data.local.dao.StockTransferDao
import com.extrotarget.extroposv2.core.data.local.dao.reporting.ReportingDao
import com.extrotarget.extroposv2.core.data.local.dao.hotel.HotelDao
import com.extrotarget.extroposv2.core.data.local.dao.hardware.PrintJobDao
import com.extrotarget.extroposv2.core.data.local.training.TrainingDbManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add code and displayOrder columns to fnb_tables
                db.execSQL("ALTER TABLE fnb_tables ADD COLUMN code TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE fnb_tables ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                
                // Initialize code with name for existing tables
                db.execSQL("UPDATE fnb_tables SET code = name WHERE code = ''")
            }
        }

        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create retail_products table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `retail_products` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `basePrice` TEXT NOT NULL, 
                        `sku` TEXT NOT NULL, 
                        `barcode` TEXT, 
                        `taxRate` TEXT NOT NULL, 
                        `stockQuantity` TEXT NOT NULL, 
                        `minStockLevel` TEXT NOT NULL, 
                        `categoryId` TEXT, 
                        `supplierId` TEXT, 
                        `imageUrl` TEXT, 
                        `isAvailable` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Create fnb_menu_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fnb_menu_items` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `basePrice` TEXT NOT NULL, 
                        `taxRate` TEXT NOT NULL, 
                        `categoryId` TEXT, 
                        `kitchenStation` TEXT, 
                        `imageUrl` TEXT, 
                        `isAvailable` INTEGER NOT NULL, 
                        `hasModifiers` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fnb_modifier_groups` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `minSelect` INTEGER NOT NULL, 
                        `maxSelect` INTEGER NOT NULL, 
                        `isRequired` INTEGER NOT NULL, 
                        `menuItemId` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fnb_modifier_options` (
                        `id` TEXT NOT NULL, 
                        `groupId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `priceAdjustment` TEXT NOT NULL, 
                        `isAvailable` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`groupId`) REFERENCES `fnb_modifier_groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fnb_modifier_options_groupId` ON `fnb_modifier_options` (`groupId`)")
            }
        }

        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop old stock_movements if exists or rename it
                db.execSQL("DROP TABLE IF EXISTS `stock_movements` ")
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stock_movements` (
                        `id` TEXT NOT NULL, 
                        `productId` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `quantity` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `reason` TEXT, 
                        `createdBy` TEXT NOT NULL, 
                        `referenceId` TEXT, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_productId` ON `stock_movements` (`productId`)")
            }
        }

        val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create suppliers table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `suppliers` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `contactPerson` TEXT, 
                        `phone` TEXT, 
                        `email` TEXT, 
                        `address` TEXT, 
                        `sstId` TEXT, 
                        `isActive` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Create purchase_orders table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `purchase_orders` (
                        `id` TEXT NOT NULL, 
                        `poNumber` TEXT NOT NULL, 
                        `supplierId` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `totalAmount` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `receivedAt` INTEGER, 
                        `createdBy` TEXT NOT NULL, 
                        `receivedBy` TEXT, 
                        `note` TEXT, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_purchase_orders_supplierId` ON `purchase_orders` (`supplierId`)")

                // Create purchase_order_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `purchase_order_items` (
                        `id` TEXT NOT NULL, 
                        `poId` TEXT NOT NULL, 
                        `productId` TEXT NOT NULL, 
                        `quantity` TEXT NOT NULL, 
                        `unitCost` TEXT NOT NULL, 
                        `receivedQuantity` TEXT NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`poId`) REFERENCES `purchase_orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_purchase_order_items_poId` ON `purchase_order_items` (`poId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_purchase_order_items_productId` ON `purchase_order_items` (`productId`)")
            }
        }

        val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create fnb_orders table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fnb_orders` (
                        `id` TEXT NOT NULL, 
                        `tableId` TEXT, 
                        `status` TEXT NOT NULL, 
                        `guestCount` INTEGER NOT NULL, 
                        `orderType` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `subtotal` TEXT NOT NULL, 
                        `totalAmount` TEXT NOT NULL, 
                        `staffId` TEXT NOT NULL, 
                        `note` TEXT, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Create fnb_order_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fnb_order_items` (
                        `id` TEXT NOT NULL, 
                        `orderId` TEXT NOT NULL, 
                        `menuItemId` TEXT NOT NULL, 
                        `quantity` TEXT NOT NULL, 
                        `unitPrice` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `kitchenStation` TEXT, 
                        `notes` TEXT, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`orderId`) REFERENCES `fnb_orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fnb_order_items_orderId` ON `fnb_order_items` (`orderId`)")
            }
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideProductDao(
        mainDb: AppDatabase,
        trainingDbManager: TrainingDbManager
    ): ProductDao {
        return if (trainingDbManager.isTrainingMode.value) {
            trainingDbManager.getTrainingDatabase().productDao()
        } else {
            mainDb.productDao()
        }
    }

    @Provides
    fun provideSupplierDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): SupplierDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().supplierDao() else mainDb.supplierDao()

    @Provides
    fun providePurchaseOrderDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): PurchaseOrderDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().purchaseOrderDao() else mainDb.purchaseOrderDao()

    @Provides
    fun provideRefundDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): RefundDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().refundDao() else mainDb.refundDao()

    @Provides
    fun provideCategoryDao(
        mainDb: AppDatabase,
        trainingDbManager: TrainingDbManager
    ): CategoryDao {
        return if (trainingDbManager.isTrainingMode.value) {
            trainingDbManager.getTrainingDatabase().categoryDao()
        } else {
            mainDb.categoryDao()
        }
    }

    @Provides
    fun provideSaleDao(
        mainDb: AppDatabase,
        trainingDbManager: TrainingDbManager
    ): SaleDao {
        return if (trainingDbManager.isTrainingMode.value) {
            trainingDbManager.getTrainingDatabase().saleDao()
        } else {
            mainDb.saleDao()
        }
    }

    @Provides
    fun provideStockMovementDao(
        mainDb: AppDatabase,
        trainingDbManager: TrainingDbManager
    ): StockMovementDao {
        return if (trainingDbManager.isTrainingMode.value) {
            trainingDbManager.getTrainingDatabase().stockMovementDao()
        } else {
            mainDb.stockMovementDao()
        }
    }

    @Provides
    fun provideStaffDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): StaffDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().staffDao() else mainDb.staffDao()

    @Provides
    fun provideCommissionRecordDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): CommissionRecordDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().commissionRecordDao() else mainDb.commissionRecordDao()

    @Provides
    fun providePrinterDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): PrinterDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().printerDao() else mainDb.printerDao()

    @Provides
    fun provideTableDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): TableDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().tableDao() else mainDb.tableDao()

    @Provides
    fun provideLaundryDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): LaundryDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().laundryDao() else mainDb.laundryDao()

    @Provides
    fun provideCarWashDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): CarWashDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().carWashDao() else mainDb.carWashDao()

    @Provides
    fun provideReceiptDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): ReceiptDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().receiptDao() else mainDb.receiptDao()

    @Provides
    fun providePaymentMethodDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): PaymentMethodDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().paymentMethodDao() else mainDb.paymentMethodDao()

    @Provides
    fun provideTaxDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): TaxDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().taxDao() else mainDb.taxDao()

    @Provides
    fun provideLhdnDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): LhdnDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().lhdnDao() else mainDb.lhdnDao()

    @Provides
    fun provideDuitNowDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): DuitNowDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().duitNowDao() else mainDb.duitNowDao()

    @Provides
    fun provideAutoCountDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): AutoCountDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().autoCountDao() else mainDb.autoCountDao()

    @Provides
    fun provideAuditDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): com.extrotarget.extroposv2.core.data.local.dao.AuditDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().auditDao() else mainDb.auditDao()

    @Provides
    fun provideLoyaltyDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): com.extrotarget.extroposv2.core.data.local.dao.loyalty.LoyaltyDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().loyaltyDao() else mainDb.loyaltyDao()

    @Provides
    fun provideShiftDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): ShiftDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().shiftDao() else mainDb.shiftDao()

    @Provides
    fun provideBranchDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): BranchDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().branchDao() else mainDb.branchDao()

    @Provides
    fun provideStockTransferDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): StockTransferDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().stockTransferDao() else mainDb.stockTransferDao()

    @Provides
    fun provideModifierDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): com.extrotarget.extroposv2.core.data.local.dao.ModifierDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().modifierDao() else mainDb.modifierDao()

    @Provides
    fun provideEndOfDayDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): com.extrotarget.extroposv2.core.data.local.dao.EndOfDayDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().endOfDayDao() else mainDb.endOfDayDao()

    @Provides
    fun provideFnbModifierDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): FnbModifierDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().fnbModifierDao() else mainDb.fnbModifierDao()

    @Provides
    fun provideFnbStationDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): FnbStationDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().fnbStationDao() else mainDb.fnbStationDao()

    @Provides
    fun provideOfflineQueueDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): OfflineQueueDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().offlineQueueDao() else mainDb.offlineQueueDao()

    @Provides
    fun provideCashMovementDao(mainDb: AppDatabase, trainingDbManager: TrainingDbManager): CashMovementDao = if (trainingDbManager.isTrainingMode.value) trainingDbManager.getTrainingDatabase().cashMovementDao() else mainDb.cashMovementDao()

    @Provides
    fun provideReportingDao(mainDb: AppDatabase): ReportingDao = mainDb.reportingDao()

    @Provides
    fun provideHotelDao(mainDb: AppDatabase): HotelDao = mainDb.hotelDao()

    @Provides
    fun provideWorkspaceDao(mainDb: AppDatabase): com.extrotarget.extroposv2.core.data.local.dao.platform.WorkspaceDao = mainDb.workspaceDao()

    @Provides
    fun providePrintJobDao(mainDb: AppDatabase): PrintJobDao = mainDb.printJobDao()
}