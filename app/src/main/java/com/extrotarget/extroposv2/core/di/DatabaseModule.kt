package com.extrotarget.extroposv2.core.di

import android.content.Context
import androidx.room.Room
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.CategoryDao
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.CarWashDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.CommissionRecordDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.StaffDao
import com.extrotarget.extroposv2.core.data.local.dao.dobi.LaundryDao
import com.extrotarget.extroposv2.core.data.local.dao.fnb.TableDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.PaymentMethodDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.TaxDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.ReceiptDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.DuitNowDao
import com.extrotarget.extroposv2.core.data.local.dao.lhdn.LhdnDao
import com.extrotarget.extroposv2.core.data.local.dao.AutoCountDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao = database.saleDao()

    @Provides
    fun provideStockMovementDao(database: AppDatabase): StockMovementDao = database.stockMovementDao()

    @Provides
    fun provideStaffDao(database: AppDatabase): StaffDao = database.staffDao()

    @Provides
    fun provideCommissionRecordDao(database: AppDatabase): CommissionRecordDao = database.commissionRecordDao()

    @Provides
    fun providePrinterDao(database: AppDatabase): PrinterDao = database.printerDao()

    @Provides
    fun provideTableDao(database: AppDatabase): TableDao = database.tableDao()

    @Provides
    fun provideLaundryDao(database: AppDatabase): LaundryDao = database.laundryDao()

    @Provides
    fun provideCarWashDao(database: AppDatabase): CarWashDao = database.carWashDao()

    @Provides
    fun provideReceiptDao(database: AppDatabase): ReceiptDao = database.receiptDao()

    @Provides
    fun providePaymentMethodDao(database: AppDatabase): PaymentMethodDao = database.paymentMethodDao()

    @Provides
    fun provideTaxDao(database: AppDatabase): TaxDao = database.taxDao()

    @Provides
    fun provideLhdnDao(database: AppDatabase): LhdnDao = database.lhdnDao()

    @Provides
    fun provideDuitNowDao(database: AppDatabase): DuitNowDao = database.duitNowDao()

    @Provides
    fun provideAutoCountDao(database: AppDatabase): AutoCountDao = database.autoCountDao()

    @Provides
    fun provideAuditDao(database: AppDatabase): com.extrotarget.extroposv2.core.data.local.dao.AuditDao = database.auditDao()

    @Provides
    fun provideLoyaltyDao(database: AppDatabase): com.extrotarget.extroposv2.core.data.local.dao.loyalty.LoyaltyDao = database.loyaltyDao()
}