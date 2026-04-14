package com.extrotarget.extroposv2.core.network

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.util.audit.AuditManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import com.extrotarget.extroposv2.core.network.NsdHelper
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val auditManager: AuditManager,
    private val nsdHelper: NsdHelper
) {
    private var server: NettyApplicationEngine? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sessions = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketServerSession>())

    fun start(port: Int = 8080) {
        if (server != null) return

        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            install(WebSockets)
            
            routing {
                get("/") {
                    call.respond(mapOf("status" to "ExtroPOS Sync Server Running", "version" to "v2.0"))
                }

                // Database Export for Slaves
                get("/sync/database") {
                    val dbFile = this@SyncServer.context.getDatabasePath(AppDatabase.DATABASE_NAME)
                    if (dbFile.exists()) {
                        // Ensure WAL is checkpointed before copying
                        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
                        call.respondFile(dbFile)
                        
                        val remoteAddr = call.request.local.remoteAddress
                        scope.launch {
                            auditManager.logAction(
                                action = "SYNC_EXPORT",
                                details = "Database exported to terminal: $remoteAddr",
                                module = "SYSTEM"
                            )
                        }
                    } else {
                        call.respond(io.ktor.http.HttpStatusCode.NotFound, "Database not found")
                    }
                }

                webSocket("/sync/realtime") {
                    sessions += this
                    try {
                        for (frame in incoming) {
                            // Handle incoming messages from slaves if needed
                        }
                    } finally {
                        sessions -= this
                    }
                }
            }
        }.start(wait = false)
        nsdHelper.registerService(port)
    }

    fun broadcastUpdate(type: String, data: Any) {
        scope.launch {
            val message = mapOf("type" to type, "data" to data)
            val json = com.google.gson.Gson().toJson(message)
            val sessionsToNotify = sessions.toList()
            sessionsToNotify.forEach { session ->
                try {
                    session.send(Frame.Text(json))
                } catch (e: Exception) {
                    // Handle session disconnection if needed
                }
            }
        }
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        nsdHelper.unregisterService()
    }

    fun isRunning(): Boolean = server != null
}
