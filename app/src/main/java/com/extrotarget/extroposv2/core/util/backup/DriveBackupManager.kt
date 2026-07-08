package com.extrotarget.extroposv2.core.util.backup

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.util.security.SecurityManager
import com.google.api.client.http.FileContent
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val securityManager: SecurityManager
) {
    private var driveService: Drive? = null

    fun initializeWithAccount(accountName: String) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccountName = accountName

            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("ExtroPOS v2").build()
            
            securityManager.saveString("drive_account_name", accountName)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Drive service")
        }
    }

    fun isInitialized(): Boolean = driveService != null

    /**
     * Uploads the current Room database file to Google Drive.
     */
    suspend fun backupDatabase(): Result<String> = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext Result.failure(Exception("Drive service not initialized"))

        try {
            // 1. Ensure WAL is checkpointed before backup
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (!dbFile.exists()) return@withContext Result.failure(Exception("Database file not found"))

            // 2. Create metadata for the file
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val metadata = com.google.api.services.drive.model.File()
            metadata.name = "ExtroPOS_Backup_$timeStamp.db"
            metadata.parents = listOf("appDataFolder")

            // 3. Upload the file
            val mediaContent = FileContent("application/octet-stream", dbFile)
            val uploadedFile = service.files().create(metadata, mediaContent)
                .setFields("id")
                .execute()

            Timber.i("Database backup successful. File ID: ${uploadedFile.id}")
            Result.success(uploadedFile.id)
        } catch (e: Exception) {
            Timber.e(e, "Database backup to Drive failed")
            Result.failure(e)
        }
    }

    /**
     * Downloads the latest backup from Google Drive.
     * Note: This usually involves a restart of the app.
     */
    suspend fun restoreLatestBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext Result.failure(Exception("Drive service not initialized"))

        try {
            // 1. Find the latest backup file in appDataFolder
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setOrderBy("createdTime desc")
                .setPageSize(1)
                .execute()

            val latestFile = result.files?.firstOrNull() ?: return@withContext Result.failure(Exception("No backup found on Drive"))

            // 2. Download to a temporary file
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val tempFile = java.io.File(context.cacheDir, "temp_restore.db")
            
            FileOutputStream(tempFile).use { output ->
                service.files().get(latestFile.id).executeMediaAndDownloadTo(output)
            }

            // 3. Replace current database (This is destructive and usually requires app restart)
            database.close()
            if (tempFile.renameTo(dbFile)) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to replace database file"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Database restore from Drive failed")
            Result.failure(e)
        }
    }
}
