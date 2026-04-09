package com.extrotarget.extroposv2.core.data.repository.settings

import com.extrotarget.extroposv2.core.data.local.dao.settings.TaxDao
import com.extrotarget.extroposv2.core.data.model.settings.TaxConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxRepository @Inject constructor(
    private val taxDao: TaxDao
) {
    fun getTaxConfig(): Flow<TaxConfig?> = taxDao.getTaxConfig()

    suspend fun updateTaxConfig(config: TaxConfig) {
        taxDao.insertTaxConfig(config)
    }
}
