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
import com.extrotarget.extroposv2.core.util.lhdn.LhdnInvoicingUtils
import com.extrotarget.extroposv2.core.util.security.SecurityManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LhdnRepository @Inject constructor(
    private val lhdnDao: LhdnDao,
    private val myInvoisApi: MyInvoisApi,
    private val gson: Gson,
    private val securityManager: SecurityManager
) {
    private val tokenMutex = Mutex()
    private var activeApi: MyInvoisApi? = null
    private var isUsingSandbox: Boolean? = null

    private fun getApi(isSandbox: Boolean): MyInvoisApi {
        if (activeApi != null && isUsingSandbox == isSandbox) return activeApi!!
        
        val baseUrl = if (isSandbox) "https://preprod-api.myinvois.hasil.gov.my/" 
                     else "https://api.myinvois.hasil.gov.my/"
        
        isUsingSandbox = isSandbox
        activeApi = retrofit2.Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
            .build()
            .create(MyInvoisApi::class.java)
        
        return activeApi!!
    }

    fun getConfig(): Flow<LhdnConfig?> = lhdnDao.getConfig()

    suspend fun saveConfig(config: LhdnConfig) = lhdnDao.saveConfig(config)

    suspend fun getSubmission(saleId: String) = lhdnDao.getSubmissionBySaleId(saleId)

    fun getSubmissionFlow(saleId: String) = lhdnDao.getSubmissionFlowBySaleId(saleId)

    suspend fun updateSubmission(submission: SaleEInvoiceSubmission) = 
        lhdnDao.updateSubmission(submission)

    suspend fun insertSubmission(submission: SaleEInvoiceSubmission) =
        lhdnDao.insertSubmission(submission)

    suspend fun getPendingSubmissions(): List<SaleEInvoiceSubmission> =
        lhdnDao.getSubmissionsByStatus(EInvoiceStatus.SUBMITTED)

    /**
     * Consolidates low-value B2C transactions for a given period.
     */
    suspend fun submitConsolidatedEInvoice(
        sales: List<Sale>,
        salesWithItems: List<com.extrotarget.extroposv2.core.data.model.SaleWithItems>,
        businessDate: Long
    ): Result<String> {
        if (sales.isEmpty()) return Result.failure(Exception("No sales to consolidate"))
        
        val config = lhdnDao.getConfig().firstOrNull() ?: return Result.failure(Exception("LHDN not configured"))
        
        try {
            // Aggregate all items from all sales
            val allItems = salesWithItems.flatMap { it.items }
            
            // Create a dummy sale object representing the consolidation
            val totalAmount = sales.sumOf { it.totalAmount }
            val totalTax = sales.sumOf { it.taxAmount }
            val totalDisc = sales.sumOf { it.discountAmount }
            val totalRounding = sales.sumOf { it.roundingAdjustment }
            
            val consolidationSale = Sale(
                id = "CONSOL-${System.currentTimeMillis()}",
                timestamp = businessDate,
                totalAmount = totalAmount,
                taxAmount = totalTax,
                discountAmount = totalDisc,
                roundingAdjustment = totalRounding,
                paymentMethod = "CONSOLIDATED"
            )

            // Submit using InvoisMapper with isConsolidated = true
            val invoiceJson = InvoisMapper.mapToDocument(
                consolidationSale, 
                allItems, 
                config, 
                isConsolidated = true
            )
            
            val jsonString = gson.toJson(invoiceJson)
            val jsonHash = LhdnInvoicingUtils.calculateDocumentHash(jsonString)
            val document = DocumentItem(
                format = "JSON",
                document = Base64.encodeToString(jsonString.toByteArray(), Base64.NO_WRAP),
                documentHash = jsonHash,
                codeNumber = consolidationSale.id
            )

            val token = getValidToken() ?: return Result.failure(Exception("Failed to authenticate with LHDN"))
            val response = getApi(config.isSandbox).submitDocuments(token, DocumentSubmissionRequest(listOf(document)))
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.acceptedDocuments.isNotEmpty()) {
                    return Result.success(body.acceptedDocuments.first().uuid)
                }
            }
            return Result.failure(Exception("LHDN API Error: ${response.code()}"))
        } catch (e: Exception) {
            Timber.e(e, "LHDN Consolidation Exception")
            return Result.failure(e)
        }
    }

    /**
     * Polls the status of a specific document from LHDN.
     */
    suspend fun pollDocumentStatus(uuid: String): Result<DocumentDetailsResponse> {
        val config = lhdnDao.getConfig().firstOrNull() ?: return Result.failure(Exception("LHDN not configured"))
        val token = getValidToken() ?: return Result.failure(Exception("Failed to authenticate with LHDN"))

        return try {
            val response = getApi(config.isSandbox).getDocumentDetails(token, uuid)
            if (response.isSuccessful && response.body() != null) {
                val details = response.body()!!
                
                // Update local database based on status
                val submission = lhdnDao.getSubmissionByUuid(uuid)
                if (submission != null) {
                    val newStatus = when (details.status) {
                        "Valid" -> EInvoiceStatus.VALID
                        "Invalid" -> EInvoiceStatus.REJECTED
                        "Cancelled" -> EInvoiceStatus.CANCELLED
                        else -> submission.status
                    }
                    
                    val errorMessage = details.validationResults?.validationSteps
                        ?.firstOrNull { it.status == "Failed" }
                        ?.error?.message

                    lhdnDao.updateSubmission(submission.copy(
                        status = newStatus,
                        lhdnValidationMessage = errorMessage ?: submission.lhdnValidationMessage,
                        lastResponse = "Status: ${details.status}",
                        lastAttemptTimestamp = System.currentTimeMillis()
                    ))
                }
                
                Result.success(details)
            } else {
                Result.failure(Exception("LHDN API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "LHDN Polling Exception for document $uuid")
            Result.failure(e)
        }
    }

    /**
     * Gets a valid access token. If the current token is expired or missing, 
     * it performs a fresh login. Thread-safe using Mutex.
     */
    suspend fun getValidToken(): String? = tokenMutex.withLock {
        val config = lhdnDao.getConfig().firstOrNull() ?: return null
        val currentToken = lhdnDao.getToken()

        // Check if token is still valid (with 2-minute buffer for network latency)
        if (currentToken != null && currentToken.expiryTimestamp > System.currentTimeMillis() + 120000) {
            return "${currentToken.tokenType} ${currentToken.accessToken}"
        }

        // Perform Login
        val clientId = securityManager.getString(SecurityManager.KEY_LHDN_CLIENT_ID) ?: config.clientId
        val clientSecret = securityManager.getString(SecurityManager.KEY_LHDN_CLIENT_SECRET) ?: config.clientSecret

        if (clientId.isNullOrBlank() || clientSecret.isNullOrBlank()) {
            Timber.e("LHDN Credentials missing in config and security manager")
            return null
        }

        return try {
            Timber.d("Requesting fresh LHDN token...")
            val response = getApi(config.isSandbox).login(
                clientId = clientId,
                clientSecret = clientSecret
            )
            if (response.isSuccessful && response.body() != null) {
                val tokenResp = response.body()!!
                val newToken = LhdnToken(
                    accessToken = tokenResp.access_token,
                    expiryTimestamp = System.currentTimeMillis() + (tokenResp.expires_in * 1000L),
                    tokenType = tokenResp.token_type
                )
                lhdnDao.saveToken(newToken)
                Timber.i("LHDN Token refreshed successfully. Expires in ${tokenResp.expires_in}s")
                "${newToken.tokenType} ${newToken.accessToken}"
            } else {
                Timber.e("LHDN Login failed: ${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "LHDN Token refresh exception")
            null
        }
    }

    /**
     * Prepares and submits an e-invoice for a sale.
     * This follows the LHDN MyInvois API requirements.
     */
    suspend fun submitEInvoice(
        sale: Sale,
        items: List<SaleItem>,
        buyer: BuyerInfo,
        isConsolidated: Boolean = false
    ): Result<String> {
        val config = lhdnDao.getConfig().firstOrNull() ?: return Result.failure(Exception("LHDN not configured"))

        try {
            // 1. Map to LHDN JSON format
            val invoiceJson = InvoisMapper.mapToDocument(sale, items, config, buyer, isConsolidated)
            val jsonString = gson.toJson(invoiceJson)

            // 2. Calculate JSON Canonicalization Hash (Simplified for Sandbox)
            val jsonHash = LhdnInvoicingUtils.calculateDocumentHash(jsonString)
            val document = DocumentItem(
                format = "JSON",
                document = Base64.encodeToString(jsonString.toByteArray(), Base64.NO_WRAP),
                documentHash = jsonHash,
                codeNumber = sale.id
            )

            // 3. Mark as submitted locally first
            val initialSubmission = SaleEInvoiceSubmission(
                saleId = sale.id,
                status = EInvoiceStatus.SUBMITTED,
                lastAttemptTimestamp = System.currentTimeMillis(),
                lastResponse = "Attempting submission to ${if (config.isSandbox) "Sandbox" else "Production"}"
            )
            lhdnDao.insertSubmission(initialSubmission)

            // 4. API Call with OAuth2 Token
            val token = getValidToken() ?: return Result.failure(Exception("Failed to authenticate with LHDN"))
            
            val response = getApi(config.isSandbox).submitDocuments(token, DocumentSubmissionRequest(listOf(document)))
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.acceptedDocuments.isNotEmpty()) {
                    val accepted = body.acceptedDocuments.first()
                    lhdnDao.updateSubmission(initialSubmission.copy(
                        status = EInvoiceStatus.SUBMITTED,
                        submissionId = body.submissionId,
                        uuid = accepted.uuid,
                        digitalSignature = jsonHash // Using the hash as a temporary local signature
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
            Timber.e(e, "LHDN Submission Exception for sale ${sale.id}")
            return Result.failure(e)
        }
    }
}
