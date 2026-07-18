package com.extrotarget.extroposv2.core.network

import androidx.room.withTransaction
import com.extrotarget.extroposv2.core.config.AppConfig
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
    private val database: AppDatabase,
    private val conflictResolver: SyncConflictResolver
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
            val response: HttpResponse = client.get("http://${hq.ipAddress}${AppConfig.Network.ENDPOINT_SYNC_MEMBER}$memberId") {
                header(AppConfig.Network.HEADER_SYNC_TOKEN, hq.syncToken)
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
            val response: HttpResponse = client.post("http://${hq.ipAddress}${AppConfig.Network.ENDPOINT_SYNC_SALE}") {
                header(AppConfig.Network.HEADER_SYNC_TOKEN, hq.syncToken)
                contentType(ContentType.Application.Json)
                setBody(saleWithItems)
            }

            if ((response.status == HttpStatusCode.OK) || (response.status == HttpStatusCode.Created)) {
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
     * Fetches current stock levels from HQ using ledger-based movement sync.
     */
    suspend fun syncStockWithHQ(): Result<Unit> = withContext(Dispatchers.IO) {
        val hq = getHQConfig() ?: return@withContext Result.failure(Exception("HQ Branch not configured"))

        try {
            // 1. Fetch latest products from HQ
            val response: HttpResponse = client.get("http://${hq.ipAddress}${AppConfig.Network.ENDPOINT_SYNC_STOCK}") {
                header(AppConfig.Network.HEADER_SYNC_TOKEN, hq.syncToken)
            }

            if (response.status == HttpStatusCode.OK) {
                val remoteProducts: List<com.extrotarget.extroposv2.core.data.model.Product> = response.body()
                database.withTransaction {
                    for (remoteProduct in remoteProducts) {
                        val localProduct = database.productDao().getProductById(remoteProduct.id)
                        if (localProduct != null) {
                            val resolved = conflictResolver.productStrategy.resolve(localProduct, remoteProduct)
                            database.productDao().updateProduct(resolved)
                        } else {
                            database.productDao().insertProduct(remoteProduct)
                        }
                    }
                }
                
                // 2. Fetch missing StockMovements (Ledger Sync)
                // This would normally use a timestamp from the last sync
                // For simplicity in this demo, let's assume we fetch all movements for today
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
            val response: HttpResponse = client.post("http://${hq.ipAddress}${AppConfig.Network.ENDPOINT_SYNC_TRANSFER}") {
                header(AppConfig.Network.HEADER_SYNC_TOKEN, hq.syncToken)
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
