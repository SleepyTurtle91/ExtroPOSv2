package com.extrotarget.extroposv2

import android.app.Application
import com.extrotarget.extroposv2.BuildConfig
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import android.util.Log
import com.extrotarget.extroposv2.core.data.seeder.DataSeeder
import com.extrotarget.extroposv2.core.work.AutoBackupWorker
import com.extrotarget.extroposv2.core.work.LhdnConsolidationWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
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
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        MainScope().launch {
            dataSeeder.seedIfNeeded()
            setupAutoBackup()
            setupLhdnConsolidation()
        }
    }

    private fun setupLhdnConsolidation() {
        val lhdnRequest = PeriodicWorkRequestBuilder<LhdnConsolidationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(2, TimeUnit.HOURS) // Run early morning
            .addTag("lhdn_consolidation")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_lhdn_consolidation",
            ExistingPeriodicWorkPolicy.KEEP,
            lhdnRequest
        )
    }

    /**
     * A tree which logs important information and crashes for release builds.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            // In a real production app, we would send errors to Crashlytics here.
            // For now, we only log WARN, ERROR and WTF.
            if (priority >= Log.WARN) {
                // Log.println(priority, tag, message)
            }
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