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
import com.extrotarget.extroposv2.core.data.local.dao.ShiftDao
import com.extrotarget.extroposv2.core.data.local.dao.BranchDao
import com.extrotarget.extroposv2.core.data.local.dao.StockTransferDao
import com.extrotarget.extroposv2.core.data.local.dao.reporting.ReportingDao
import com.extrotarget.extroposv2.core.data.local.dao.hotel.HotelDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
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
    fun provideReportingDao(mainDb: AppDatabase): ReportingDao = mainDb.reportingDao()

    @Provides
    fun provideHotelDao(mainDb: AppDatabase): HotelDao = mainDb.hotelDao()
}