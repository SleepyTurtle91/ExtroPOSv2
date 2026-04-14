package com.extrotarget.extroposv2.core.data.repository.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ACTIVE_BUSINESS_MODE = stringPreferencesKey("active_business_mode")

    val activeBusinessMode: Flow<BusinessMode> = context.dataStore.data
        .map { preferences ->
            val modeId = preferences[ACTIVE_BUSINESS_MODE] ?: BusinessMode.RETAIL.id
            BusinessMode.values().find { it.id == modeId } ?: BusinessMode.RETAIL
        }

    suspend fun updateBusinessMode(mode: BusinessMode) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_BUSINESS_MODE] = mode.id
        }
    }
}
