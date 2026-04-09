package com.extrotarget.extroposv2

import android.app.Application
import com.extrotarget.extroposv2.core.data.seeder.DataSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ExtroPosApp : Application() {
    @Inject
    lateinit var dataSeeder: DataSeeder

    override fun onCreate() {
        super.onCreate()
        MainScope().launch {
            dataSeeder.seedIfNeeded()
        }
    }
}