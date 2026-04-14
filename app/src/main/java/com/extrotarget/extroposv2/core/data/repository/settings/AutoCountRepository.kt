package com.extrotarget.extroposv2.core.data.repository.settings

import com.extrotarget.extroposv2.core.data.local.dao.AutoCountDao
import com.extrotarget.extroposv2.core.data.model.settings.AutoCountConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoCountRepository @Inject constructor(
    private val autoCountDao: AutoCountDao
) {
    fun getConfig(): Flow<AutoCountConfig?> = autoCountDao.getConfig()

    suspend fun getConfigSync(): AutoCountConfig? = autoCountDao.getConfigSync()

    suspend fun saveConfig(config: AutoCountConfig) = autoCountDao.saveConfig(config)
}
