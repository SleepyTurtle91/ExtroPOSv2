package com.extrotarget.extroposv2.core.license

import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "license_prefs")

@Singleton
class LicenseManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
    }

    companion object {
        private val ACTIVATION_KEY = stringPreferencesKey("activation_key")
        private val IS_ACTIVATED = booleanPreferencesKey("is_activated")
        private val TRIAL_START_DATE = stringPreferencesKey("trial_start_date")
        private val LICENSE_TYPE = stringPreferencesKey("license_type")
        private val EXPIRY_DATE = stringPreferencesKey("expiry_date")
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    val licenseInfo: Flow<LicenseInfo> = context.dataStore.data.map { prefs ->
        val trialStartStr = prefs[TRIAL_START_DATE]
        val expiryStr = prefs[EXPIRY_DATE]
        
        LicenseInfo(
            deviceId = deviceId,
            activationKey = prefs[ACTIVATION_KEY],
            isActivated = prefs[IS_ACTIVATED] ?: false,
            trialStartDate = trialStartStr?.let { LocalDateTime.parse(it, formatter) },
            expiryDate = expiryStr?.let { LocalDateTime.parse(it, formatter) },
            licenseType = LicenseType.valueOf(prefs[LICENSE_TYPE] ?: LicenseType.TRIAL.name)
        )
    }

    suspend fun initializeTrial() {
        val prefs = context.dataStore.data.first()
        if (prefs[TRIAL_START_DATE] == null) {
            context.dataStore.edit { settings ->
                settings[TRIAL_START_DATE] = LocalDateTime.now().format(formatter)
            }
        }
    }

    suspend fun activate(key: String): Boolean {
        val expectedKey = generateKeyForDevice(deviceId)
        if (key == expectedKey) {
            context.dataStore.edit { settings ->
                settings[ACTIVATION_KEY] = key
                settings[IS_ACTIVATED] = true
                settings[LICENSE_TYPE] = LicenseType.PRO.name
                settings[EXPIRY_DATE] = LocalDateTime.now().plusYears(1).format(formatter)
            }
            return true
        }
        return false
    }

    fun getLicenseStatus(info: LicenseInfo): LicenseStatus {
        if (info.isActivated) {
            val now = LocalDateTime.now()
            return if (info.expiryDate == null || info.expiryDate.isAfter(now)) {
                LicenseStatus.Valid
            } else {
                LicenseStatus.Expired
            }
        }

        val trialStart = info.trialStartDate ?: return LicenseStatus.Invalid
        val now = LocalDateTime.now()
        val daysElapsed = ChronoUnit.DAYS.between(trialStart, now).toInt()
        val remaining = 14 - daysElapsed

        return if (remaining > 0) {
            LicenseStatus.Trial(remaining)
        } else {
            LicenseStatus.Expired
        }
    }

    private fun generateKeyForDevice(id: String): String {
        val salt = "EXTRO_SALT_2024"
        val bytes = (id + salt).toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    fun getDeviceIdForDisplay(): String = deviceId
}
