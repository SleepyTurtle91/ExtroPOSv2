package com.extrotarget.extroposv2.core.util.backup

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {
    suspend fun backupDatabase(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Ensure all changes are flushed to disk
            db.openHelper.writableDatabase.query("PRAGMA checkpoint(FULL)")
            
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (dbFile.exists()) {
                FileInputStream(dbFile).use { input ->
                    input.copyTo(outputStream)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Database file not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreDatabase(inputStream: InputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            
            // Close database before restore
            db.close()
            
            FileOutputStream(dbFile).use { output ->
                inputStream.copyTo(output)
            }
            
            // We might need to restart the app or re-initialize the DB here.
            // In a real scenario, a restart is safest.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
