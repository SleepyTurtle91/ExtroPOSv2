package com.extrotarget.extroposv2.core.data.repository.lhdn

import android.util.Base64
import com.extrotarget.extroposv2.core.data.local.dao.lhdn.LhdnDao
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.lhdn.BuyerInfo
import com.extrotarget.extroposv2.core.data.model.lhdn.EInvoiceStatus
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnToken
import com.extrotarget.extroposv2.core.data.model.lhdn.SaleEInvoiceSubmission
import com.extrotarget.extroposv2.core.network.api.lhdn.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LhdnRepository @Inject constructor(
    private val lhdnDao: LhdnDao,
    private val myInvoisApi: MyInvoisApi,
    private val gson: Gson
) {
    fun getConfig(): Flow<LhdnConfig?> = lhdnDao.getConfig()

    suspend fun saveConfig(config: LhdnConfig) = lhdnDao.saveConfig(config)

    suspend fun getSubmission(saleId: String) = lhdnDao.getSubmissionBySaleId(saleId)

    suspend fun updateSubmission(submission: SaleEInvoiceSubmission) = 
        lhdnDao.updateSubmission(submission)

    suspend fun insertSubmission(submission: SaleEInvoiceSubmission) =
        lhdnDao.insertSubmission(submission)

    /**
     * Gets a valid access token. If the current token is expired or missing, 
     * it performs a fresh login.
     */
    suspend fun getValidToken(): String? {
        val config = lhdnDao.getConfig().firstOrNull() ?: return null
        val currentToken = lhdnDao.getToken()

        // Check if token is still valid (with 1-minute buffer)
        if (currentToken != null && currentToken.expiryTimestamp > System.currentTimeMillis() + 60000) {
            return "${currentToken.tokenType} ${currentToken.accessToken}"
        }

        // Perform Login
        if (config.clientId == null || config.clientSecret == null) return null

        val body = "client_id=${config.clientId}&client_secret=${config.clientSecret}&grant_type=client_credentials&scope=InvoicingAPI"
        
        return try {
            val response = myInvoisApi.login(body = body)
            if (response.isSuccessful && response.body() != null) {
                val tokenResp = response.body()!!
                val newToken = LhdnToken(
                    accessToken = tokenResp.access_token,
                    expiryTimestamp = System.currentTimeMillis() + (tokenResp.expires_in * 1000L),
                    tokenType = tokenResp.token_type
                )
                lhdnDao.saveToken(newToken)
                "${newToken.tokenType} ${newToken.accessToken}"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Prepares and submits an e-invoice for a sale.
     * This follows the LHDN MyInvois API requirements.
     */
    suspend fun submitEInvoice(sale: Sale, items: List<SaleItem>, buyer: BuyerInfo): Result<String> {
        val config = lhdnDao.getConfig().firstOrNull() ?: return Result.failure(Exception("LHDN not configured"))

        try {
            // 1. Map to LHDN JSON format
            val invoiceJson = InvoisMapper.mapToDocument(sale, items, config, buyer)
            val jsonString = gson.toJson(invoiceJson)

            // 2. Prepare submission request (simplified)
            val jsonHash = sha256(jsonString)
            val document = DocumentItem(
                format = "JSON",
                document = Base64.encodeToString(jsonString.toByteArray(), Base64.NO_WRAP),
                documentHash = jsonHash,
                codeNumber = sale.id
            )

            // 3. Local digital signature (pre-submission hash as a baseline)
            val localSignature = jsonHash.take(64)

            // 3. Mark as submitted locally first
            val initialSubmission = SaleEInvoiceSubmission(
                saleId = sale.id,
                status = EInvoiceStatus.SUBMITTED,
                lastAttemptTimestamp = System.currentTimeMillis()
            )
            lhdnDao.insertSubmission(initialSubmission)

            // 4. API Call with OAuth2 Token
            val token = getValidToken() ?: return Result.failure(Exception("Failed to authenticate with LHDN"))
            
            val response = myInvoisApi.submitDocuments(token, DocumentSubmissionRequest(listOf(document)))
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.acceptedDocuments.isNotEmpty()) {
                    val accepted = body.acceptedDocuments.first()
                    lhdnDao.updateSubmission(initialSubmission.copy(
                        status = EInvoiceStatus.SUBMITTED,
                        submissionId = body.submissionId,
                        uuid = accepted.uuid,
                        digitalSignature = localSignature
                    ))
                    return Result.success(accepted.uuid)
                } else if (body.rejectedDocuments.isNotEmpty()) {
                    val rejected = body.rejectedDocuments.first()
                    lhdnDao.updateSubmission(initialSubmission.copy(
                        status = EInvoiceStatus.REJECTED,
                        lhdnValidationMessage = rejected.error.message
                    ))
                    return Result.failure(Exception(rejected.error.message))
                }
            }

            return Result.failure(Exception("LHDN API Error: ${response.code()}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
