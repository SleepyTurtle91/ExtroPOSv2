package com.extrotarget.extroposv2.core.util.backup

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    fun exportDatabase(outputStream: OutputStream): Result<Unit> {
        return try {
            database.close()
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

    fun importDatabase(inputStream: InputStream): Result<Unit> {
        return try {
            database.close()
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            
            // Backup current DB before overwrite? 
            // For now, direct overwrite
            FileOutputStream(dbFile).use { output ->
                inputStream.copyTo(output)
            }
            
            // Also handle WAL/SHM files if they exist
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}