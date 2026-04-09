package com.extrotarget.extroposv2.core.data.local.dao.settings

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.settings.PaymentMethod
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE isEnabled = 1")
    fun getEnabledPaymentMethods(): Flow<List<PaymentMethod>>

    @Query("SELECT * FROM payment_methods")
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod)

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod)

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod)

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun getCount(): Int
}
