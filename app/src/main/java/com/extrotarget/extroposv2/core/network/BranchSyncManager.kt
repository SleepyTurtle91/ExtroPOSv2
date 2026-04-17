package com.extrotarget.extroposv2.core.network

import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.data.model.inventory.Branch
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransfer
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BranchSyncManager @Inject constructor(
    private val database: AppDatabase
) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
        }
    }

    private suspend fun getHQConfig(): Branch? = database.branchDao().getHQBranch()

    /**
     * Pulls the latest member data from HQ.
     */
    suspend fun pullMemberFromHQ(memberId: String): Result<Member> = withContext(Dispatchers.IO) {
        val hq = getHQConfig() ?: return@withContext Result.failure(Exception("HQ Branch not configured"))
        
        try {
            val response: HttpResponse = client.get("http://${hq.ipAddress}/sync/branch/member/$memberId") {
                header("X-Sync-Token", hq.syncToken)
            }
            
            if (response.status == HttpStatusCode.OK) {
                val member = response.body<Member>()
                database.loyaltyDao().insertMember(member)
                Result.success(member)
            } else {
                Result.failure(Exception("HQ Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to pull member $memberId from HQ")
            Result.failure(e)
        }
    }

    /**
     * Pushes a completed sale to HQ for centralized tracking and points.
     */
    suspend fun pushSaleToHQ(saleWithItems: SaleWithItems): Result<Unit> = withContext(Dispatchers.IO) {
        val hq = getHQConfig() ?: return@withContext Result.failure(Exception("HQ Branch not configured"))

        try {
            val response: HttpResponse = client.post("http://${hq.ipAddress}/sync/branch/sale") {
                header("X-Sync-Token", hq.syncToken)
                contentType(ContentType.Application.Json)
                setBody(saleWithItems)
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HQ Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to push sale ${saleWithItems.sale.id} to HQ")
            Result.failure(e)
        }
    }

    /**
     * Fetches current stock levels from HQ.
     */
    suspend fun syncStockWithHQ(): Result<Unit> = withContext(Dispatchers.IO) {
        val hq = getHQConfig() ?: return@withContext Result.failure(Exception("HQ Branch not configured"))

        try {
            val response: HttpResponse = client.get("http://${hq.ipAddress}/sync/branch/stock") {
                header("X-Sync-Token", hq.syncToken)
            }

            if (response.status == HttpStatusCode.OK) {
                val products: List<com.extrotarget.extroposv2.core.data.model.Product> = response.body()
                for (product in products) {
                    val localProduct = database.productDao().getProductById(product.id)
                    if (localProduct != null) {
                        // Only update stock and isAvailable from HQ, keep local name/price if modified?
                        // Actually for centralized management, usually HQ overrides everything.
                        // But let's at least keep stock sync simple:
                        database.productDao().insertProduct(product)
                    } else {
                        database.productDao().insertProduct(product)
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("HQ Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync stock with HQ")
            Result.failure(e)
        }
    }

    /**
     * Initiates an inter-branch stock transfer.
     */
    suspend fun initiateStockTransfer(transfer: StockTransfer): Result<Unit> = withContext(Dispatchers.IO) {
        val hq = getHQConfig() ?: return@withContext Result.failure(Exception("HQ Branch not configured"))

        try {
            val response: HttpResponse = client.post("http://${hq.ipAddress}/sync/branch/transfer") {
                header("X-Sync-Token", hq.syncToken)
                contentType(ContentType.Application.Json)
                setBody(transfer)
            }

            if (response.status == HttpStatusCode.OK) {
                database.stockTransferDao().insertTransfer(transfer)
                Result.success(Unit)
            } else {
                Result.failure(Exception("HQ Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initiate stock transfer")
            Result.failure(e)
        }
    }
}
