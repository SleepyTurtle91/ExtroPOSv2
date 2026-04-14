package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.extrotarget.extroposv2.core.util.exporter.MasterExportManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val masterExportManager: MasterExportManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val backupDir = File(applicationContext.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "extropos_backup_$timestamp.zip")

            FileOutputStream(backupFile).use { fos ->
                masterExportManager.exportAllData(fos)
            }

            // Keep only last 7 backups to save space
            val backups = backupDir.listFiles()?.sortedByDescending { it.lastModified() }
            if (backups != null && backups.size > 7) {
                backups.drop(7).forEach { it.delete() }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
