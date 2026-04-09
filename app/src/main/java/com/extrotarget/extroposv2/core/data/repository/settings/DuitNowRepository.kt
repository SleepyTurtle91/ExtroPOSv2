package com.extrotarget.extroposv2.core.data.repository.settings

import com.extrotarget.extroposv2.core.data.local.dao.settings.DuitNowDao
import com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuitNowRepository @Inject constructor(
    private val duitNowDao: DuitNowDao
) {
    fun getConfig(): Flow<DuitNowConfig?> = duitNowDao.getConfig()

    suspend fun saveConfig(config: DuitNowConfig) = duitNowDao.saveConfig(config)
    
    suspend fun hasConfig(): Boolean = duitNowDao.hasConfig() > 0
}
