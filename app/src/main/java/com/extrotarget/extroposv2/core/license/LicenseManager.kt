package com.extrotarget.extroposv2.core.license

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.extrotarget.extroposv2.core.config.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
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
        LicenseUtils.generateHWID(
            Build.MANUFACTURER,
            Build.MODEL,
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
        )
    }

    fun getFormattedDeviceId(): String {
        return LicenseUtils.formatDeviceId(deviceId)
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

    suspend fun activate(key: String, expiryDate: LocalDateTime): Boolean {
        val expectedKey = LicenseUtils.generateActivationKey(deviceId, expiryDate.format(formatter))
        if (key == expectedKey) {
            context.dataStore.edit { settings ->
                settings[ACTIVATION_KEY] = key
                settings[IS_ACTIVATED] = true
                settings[LICENSE_TYPE] = LicenseType.PRO.name
                settings[EXPIRY_DATE] = expiryDate.format(formatter)
            }
            return true
        }
        return false
    }

    fun getLicenseStatus(info: LicenseInfo): LicenseStatus {
        if (info.isActivated) {
            val now = LocalDateTime.now()
            
            // Verify HMAC integrity
            val expectedKey = LicenseUtils.generateActivationKey(deviceId, info.expiryDate?.format(formatter) ?: "")
            if (info.activationKey != expectedKey) {
                return LicenseStatus.Invalid
            }

            return if (info.expiryDate == null || info.expiryDate.isAfter(now)) {
                LicenseStatus.Valid
            } else {
                LicenseStatus.Expired
            }
        }

        val trialStart = info.trialStartDate ?: return LicenseStatus.Invalid
        val now = LocalDateTime.now()
        val daysElapsed = ChronoUnit.DAYS.between(trialStart, now).toInt()
        
        val trialDays = 30
        val graceDays = 3
        
        val remainingTrial = trialDays - daysElapsed
        if (remainingTrial > 0) {
            return LicenseStatus.Trial(remainingTrial)
        }
        
        val remainingGrace = (trialDays + graceDays) - daysElapsed
        if (remainingGrace > 0) {
            return LicenseStatus.GracePeriod(remainingGrace)
        }

        return LicenseStatus.Expired
    }

    fun getDeviceIdForDisplay(): String = getFormattedDeviceId()
}
