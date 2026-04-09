package com.extrotarget.extroposv2.core.data.local.dao.lhdn

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.lhdn.EInvoiceStatus
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnToken
import com.extrotarget.extroposv2.core.data.model.lhdn.SaleEInvoiceSubmission
import kotlinx.coroutines.flow.Flow

@Dao
interface LhdnDao {
    @Query("SELECT * FROM lhdn_config WHERE id = 1")
    fun getConfig(): Flow<LhdnConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: LhdnConfig)

    @Query("SELECT COUNT(*) FROM lhdn_config")
    suspend fun hasConfig(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SaleEInvoiceSubmission)

    @Update
    suspend fun updateSubmission(submission: SaleEInvoiceSubmission)

    @Query("SELECT * FROM sale_einvoice_submission WHERE saleId = :saleId")
    suspend fun getSubmissionBySaleId(saleId: String): SaleEInvoiceSubmission?

    @Query("SELECT * FROM sale_einvoice_submission WHERE status = :status")
    suspend fun getSubmissionsByStatus(status: EInvoiceStatus): List<SaleEInvoiceSubmission>

    @Query("SELECT * FROM lhdn_tokens WHERE id = 1")
    suspend fun getToken(): LhdnToken?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: LhdnToken)

    @Query("DELETE FROM lhdn_tokens")
    suspend fun clearTokens()
}
