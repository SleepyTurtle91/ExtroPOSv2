package com.extrotarget.extroposv2.core.platform

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    suspend fun checkDatabaseIntegrity(): Boolean = withContext(Dispatchers.IO) {
        try {
            val cursor = database.openHelper.readableDatabase.query("PRAGMA integrity_check")
            if (cursor.moveToFirst()) {
                val result = cursor.getString(0)
                cursor.close()
                return@withContext result.equals("ok", ignoreCase = true)
            }
            cursor.close()
            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun performMaintenance(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Full Checkpoint
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            
            // 2. Vacuum to reclaim space and defragment
            database.openHelper.writableDatabase.execSQL("VACUUM")
            
            // 3. Optimize (Room/SQLite 3.37+)
            database.openHelper.writableDatabase.execSQL("PRAGMA optimize")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getStorageInfo(): StorageInfo {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val dbSize = if (dbFile.exists()) dbFile.length() else 0L
        
        val freeSpace = context.filesDir.freeSpace
        val totalSpace = context.filesDir.totalSpace
        
        return StorageInfo(
            databaseSize = dbSize,
            freeSpace = freeSpace,
            totalSpace = totalSpace
        )
    }

    suspend fun runFullDiagnostics(): DiagnosticsReport = withContext(Dispatchers.Default) {
        DiagnosticsReport(
            isDatabaseHealthy = checkDatabaseIntegrity(),
            storageInfo = getStorageInfo(),
            timestamp = System.currentTimeMillis()
        )
    }
}

data class StorageInfo(
    val databaseSize: Long,
    val freeSpace: Long,
    val totalSpace: Long
)

data class DiagnosticsReport(
    val isDatabaseHealthy: Boolean,
    val storageInfo: StorageInfo,
    val timestamp: Long
)
