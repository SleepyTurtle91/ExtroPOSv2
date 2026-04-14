package com.extrotarget.extroposv2.core.auth

import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val USER_KEY = stringPreferencesKey("current_user")
    
    private val _currentUser = MutableStateFlow<Staff?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = context.sessionDataStore.data.first()
            val userJson = prefs[USER_KEY]
            if (userJson != null) {
                _currentUser.value = gson.fromJson(userJson, Staff::class.java)
            }
        }
    }

    fun login(staff: Staff) {
        _currentUser.value = staff
        CoroutineScope(Dispatchers.IO).launch {
            context.sessionDataStore.edit { it[USER_KEY] = gson.toJson(staff) }
        }
    }

    fun logout() {
        _currentUser.value = null
        CoroutineScope(Dispatchers.IO).launch {
            context.sessionDataStore.edit { it.remove(USER_KEY) }
        }
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null

    fun getCurrentStaff(): Staff? = _currentUser.value

    fun hasPermission(permission: String): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == "ADMIN") return true
        
        return when (permission) {
            "VOID_SALE" -> user.role == "SUPERVISOR"
            "GIVE_DISCOUNT" -> user.role in listOf("SUPERVISOR", "CASHIER")
            "MANAGE_INVENTORY" -> user.role == "SUPERVISOR"
            "VIEW_REPORTS" -> user.role == "SUPERVISOR"
            "MANAGE_STAFF" -> false // Only ADMIN
            "SYSTEM_SETTINGS" -> false // Only ADMIN
            else -> false
        }
    }

    fun hasRole(role: String): Boolean {
        return _currentUser.value?.role == role || _currentUser.value?.role == "ADMIN"
    }
}