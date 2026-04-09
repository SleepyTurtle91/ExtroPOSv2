package com.extrotarget.extroposv2.core.data.repository.lhdn

import com.extrotarget.extroposv2.core.data.local.dao.lhdn.LhdnDao
import com.extrotarget.extroposv2.core.data.model.lhdn.EInvoiceStatus
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.data.model.lhdn.SaleEInvoiceSubmission
import com.extrotarget.extroposv2.core.network.api.lhdn.MyInvoisApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LhdnRepository @Inject constructor(
    private val lhdnDao: LhdnDao,
    private val myInvoisApi: MyInvoisApi
) {
    fun getConfig(): Flow<LhdnConfig?> = lhdnDao.getConfig()

    suspend fun saveConfig(config: LhdnConfig) = lhdnDao.saveConfig(config)

    suspend fun getSubmission(saleId: String) = lhdnDao.getSubmissionBySaleId(saleId)

    suspend fun updateSubmission(submission: SaleEInvoiceSubmission) = 
        lhdnDao.updateSubmission(submission)

    suspend fun insertSubmission(submission: SaleEInvoiceSubmission) =
        lhdnDao.insertSubmission(submission)

    // TODO: Implement actual API calls for login and submission
}
