package com.extrotarget.extroposv2

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.extrotarget.extroposv2.core.data.seeder.DataSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
        }
    }
}