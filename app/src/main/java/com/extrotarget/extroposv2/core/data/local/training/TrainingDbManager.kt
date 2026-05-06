package com.extrotarget.extroposv2.core.data.local.training

import android.content.Context
import androidx.room.Room
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingDbManager @Inject constructor(
    @ApplicationContext private val context: Context,
    settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    val isTrainingMode: StateFlow<Boolean> = settingsRepository.isTrainingModeEnabled
        .stateIn(scope, SharingStarted.Eagerly, false)

    private var trainingDb: AppDatabase? = null

    fun getTrainingDatabase(): AppDatabase {
        return trainingDb ?: synchronized(this) {
            trainingDb ?: Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java
            )
            .fallbackToDestructiveMigration()
            .build()
            .also { trainingDb = it }
        }
    }

    fun clearTrainingData() {
        synchronized(this) {
            trainingDb?.close()
            trainingDb = null
        }
    }
}
