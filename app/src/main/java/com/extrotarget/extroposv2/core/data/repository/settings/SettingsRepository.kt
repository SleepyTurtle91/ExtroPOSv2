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
    private val ONBOARDING_COMPLETED = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")
    private val TRAINING_MODE_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("training_mode_enabled")
    private val TERMINAL_ROLE = stringPreferencesKey("terminal_role")
    private val OPERATION_MODE = stringPreferencesKey("operation_mode")
    private val LANGUAGE_CODE = stringPreferencesKey("language_code")

    val activeBusinessMode: Flow<BusinessMode> = context.dataStore.data
        .map { preferences ->
            val modeId = preferences[ACTIVE_BUSINESS_MODE] ?: BusinessMode.RETAIL.id
            BusinessMode.values().find { it.id == modeId } ?: BusinessMode.RETAIL
        }

    val languageCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_CODE] ?: "en"
        }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    val isTrainingModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TRAINING_MODE_ENABLED] ?: false
        }

    val terminalRole: Flow<com.extrotarget.extroposv2.core.data.model.settings.TerminalRole> = context.dataStore.data
        .map { preferences ->
            val roleName = preferences[TERMINAL_ROLE] ?: com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.MASTER.name
            try {
                com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.valueOf(roleName)
            } catch (e: Exception) {
                com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.MASTER
            }
        }

    val operationMode: Flow<com.extrotarget.extroposv2.core.data.model.settings.OperationMode> = context.dataStore.data
        .map { preferences ->
            val modeId = preferences[OPERATION_MODE] ?: com.extrotarget.extroposv2.core.data.model.settings.OperationMode.HYBRID.id
            com.extrotarget.extroposv2.core.data.model.settings.OperationMode.entries.find { it.id == modeId } ?: com.extrotarget.extroposv2.core.data.model.settings.OperationMode.HYBRID
        }

    suspend fun updateBusinessMode(mode: BusinessMode) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_BUSINESS_MODE] = mode.id
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setTrainingMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TRAINING_MODE_ENABLED] = enabled
        }
    }

    suspend fun updateTerminalRole(role: com.extrotarget.extroposv2.core.data.model.settings.TerminalRole) {
        context.dataStore.edit { preferences ->
            preferences[TERMINAL_ROLE] = role.name
        }
    }

    suspend fun updateOperationMode(mode: com.extrotarget.extroposv2.core.data.model.settings.OperationMode) {
        context.dataStore.edit { preferences ->
            preferences[OPERATION_MODE] = mode.id
        }
    }

    suspend fun updateLanguage(code: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = code
        }
    }
}
