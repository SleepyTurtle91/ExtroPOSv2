package com.extrotarget.extroposv2

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.extrotarget.extroposv2.core.data.seeder.DataSeeder
import com.extrotarget.extroposv2.core.work.AutoBackupWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ExtroPosApp : Application(), Configuration.Provider {
    @Inject
    lateinit var dataSeeder: DataSeeder

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        MainScope().launch {
            dataSeeder.seedIfNeeded()
            setupAutoBackup()
        }
    }

    private fun setupAutoBackup() {
        val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(6, TimeUnit.HOURS) // Run during off-peak
            .addTag("auto_backup")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_auto_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }
}