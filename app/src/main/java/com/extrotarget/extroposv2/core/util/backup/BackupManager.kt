package com.extrotarget.extroposv2.core.util.backup

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.platform.DeviceIdentityManager
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val identityManager: DeviceIdentityManager
) {
    private val gson = Gson()

    suspend fun createBackupPackage(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Flush DB to disk
            db.openHelper.writableDatabase.query("PRAGMA checkpoint(FULL)")
            
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (!dbFile.exists()) return@withContext Result.failure(Exception("Database file not found"))

            val identity = identityManager.getDeviceIdentity()
            val metadata = BackupMetadata(
                branchId = identity.branchId,
                dbVersion = db.openHelper.readableDatabase.version,
                deviceInfo = "${identity.terminalName} (${android.os.Build.MODEL})"
            )

            ZipOutputStream(outputStream).use { zipOut ->
                // 2. Add Database File
                zipOut.putNextEntry(ZipEntry("database.db"))
                FileInputStream(dbFile).use { it.copyTo(zipOut) }
                zipOut.closeEntry()

                // 3. Add Metadata
                zipOut.putNextEntry(ZipEntry("metadata.json"))
                zipOut.write(gson.toJson(metadata).toByteArray())
                zipOut.closeEntry()

                // 4. Add Checksum
                val checksum = calculateChecksum(dbFile)
                zipOut.putNextEntry(ZipEntry("checksum.sha256"))
                zipOut.write(checksum.toByteArray())
                zipOut.closeEntry()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromPackage(inputStream: InputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "backup_restore")
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            ZipInputStream(inputStream).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val file = File(tempDir, entry.name)
                    FileOutputStream(file).use { zipIn.copyTo(it) }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            // 1. Verify Checksum
            val dbFile = File(tempDir, "database.db")
            val checksumFile = File(tempDir, "checksum.sha256")
            if (!dbFile.exists() || !checksumFile.exists()) return@withContext Result.failure(Exception("Invalid backup package"))

            val expectedChecksum = checksumFile.readText()
            val actualChecksum = calculateChecksum(dbFile)
            if (expectedChecksum != actualChecksum) return@withContext Result.failure(Exception("Checksum verification failed"))

            // 2. Close DB and Overwrite
            db.close()
            val targetDbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            
            // Delete WAL/SHM
            File(targetDbFile.path + "-wal").delete()
            File(targetDbFile.path + "-shm").delete()

            dbFile.copyTo(targetDbFile, overwrite = true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun cleanupOldBackups(backupDir: File, keepDays: Int = 7): Int = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val expiryTime = currentTime - (keepDays * 24 * 60 * 60 * 1000L)
        
        val files = backupDir.listFiles { _, name -> name.endsWith(".extro") } ?: return@withContext 0
        var deletedCount = 0
        for (file in files) {
            if (file.lastModified() < expiryTime) {
                if (file.delete()) deletedCount++
            }
        }
        deletedCount
    }
}
