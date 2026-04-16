package com.extrotarget.extroposv2.core.network

import android.content.Context
import android.content.Intent
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.util.audit.AuditManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.websocket.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.copyTo
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val auditManager: AuditManager
) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
        install(WebSockets)
    }

    private val _realtimeUpdates = MutableSharedFlow<String>()
    val realtimeUpdates = _realtimeUpdates.asSharedFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus = _syncStatus.asStateFlow()

    private var session: DefaultClientWebSocketSession? = null

    suspend fun connectToRealtime(masterIp: String, port: Int = 8080) {
        var delayMs = 1000L
        while (true) {
            _syncStatus.value = SyncStatus.CONNECTING
            try {
                client.webSocket(method = io.ktor.http.HttpMethod.Get, host = masterIp, port = port, path = "/sync/realtime") {
                    session = this
                    _syncStatus.value = SyncStatus.CONNECTED
                    
                    // On connection, push all pending local sales to master
                    pushUnsyncedSales()

                    delayMs = 1000L // Reset delay on success
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            _realtimeUpdates.emit(frame.readText())
                        }
                    }
                }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR(e.message ?: "Realtime connection failed")
                e.printStackTrace()
            } finally {
                _syncStatus.value = SyncStatus.DISCONNECTED
                session = null
            }
            
            // Exponential backoff
            kotlinx.coroutines.delay(delayMs)
            delayMs = (delayMs * 2).coerceAtMost(30000L) // Max 30 seconds
            Timber.d("Retrying P2P connection in ${delayMs/1000}s...")
        }
    }

    private suspend fun pushUnsyncedSales() {
        val unsynced = database.saleDao().getUnsyncedSalesWithItems()
        unsynced.forEach { saleWithItems ->
            val message = mapOf("type" to "PUSH_SALE", "data" to saleWithItems)
            val json = com.google.gson.Gson().toJson(message)
            session?.send(Frame.Text(json))
            database.saleDao().markSaleAsSynced(saleWithItems.sale.id)
        }
    }

    suspend fun syncFromMaster(masterIp: String, port: Int = 8080, force: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Dirty Check
            val unsyncedCount = database.saleDao().getUnsyncedCount()
            if (unsyncedCount > 0 && !force) {
                return@withContext Result.failure(Exception("DIRTY_DATA: $unsyncedCount unsynced sales found."))
            }

            val response: HttpResponse = client.get("http://$masterIp:$port/sync/database")
            
            if (response.status.value == 200) {
                // Close DB before replacement
                database.close()

                val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                val walFile = File(dbPath.path + "-wal")
                val shmFile = File(dbPath.path + "-shm")
                val tempFile = File(context.cacheDir, "temp_db")
                
                response.bodyAsChannel().copyAndClose(tempFile.writeChannel())

                // Safer replacement
                if (tempFile.exists()) {
                    val backupFile = File(dbPath.path + ".bak")
                    
                    try {
                        // Rename current to backup
                        if (dbPath.exists()) {
                            dbPath.renameTo(backupFile)
                        }
                        
                        // Delete WAL/SHM as they are invalid for the new DB file
                        if (walFile.exists()) walFile.delete()
                        if (shmFile.exists()) shmFile.delete()

                        // Move new file to destination
                        tempFile.copyTo(dbPath, overwrite = true)
                        
                        // If everything succeeded, delete backup and temp
                        backupFile.delete()
                        tempFile.delete()
                        
                        auditManager.logAction(
                            action = "SYNC_IMPORT",
                            details = "Database imported successfully from Master: $masterIp",
                            module = "SYSTEM"
                        )
                        Result.success(Unit)
                    } catch (e: Exception) {
                        // Restore backup if possible
                        if (backupFile.exists()) {
                            backupFile.renameTo(dbPath)
                        }
                        Result.failure(e)
                    }
                } else {
                    Result.failure(Exception("Failed to download database file"))
                }
            } else {
                Result.failure(Exception("Master server returned: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun restartApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}

sealed class SyncStatus {
    object IDLE : SyncStatus()
    object CONNECTING : SyncStatus()
    object CONNECTED : SyncStatus()
    object DISCONNECTED : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}
