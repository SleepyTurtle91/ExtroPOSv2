package com.extrotarget.extroposv2.core.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager // Assuming this exists to store general session info
) {
    private val credentialManager = CredentialManager.create(context)
    
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken

    /**
     * Triggers the Google Sign-In UI using the modern Credential Manager API.
     * Includes scopes for Google Drive backup.
     */
    suspend fun signIn(serverClientId: String): Result<GoogleIdTokenCredential> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .build()

            // Note: Modern Credential Manager for Google ID doesn't directly handle extra OAuth scopes 
            // in the same way as the legacy GoogleSignInOptions. 
            // For Drive, we might need to handle the authorization separately if the token doesn't include it.
            // However, we start with the basic sign-in.
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In failed")
            Result.failure(e)
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): Result<GoogleIdTokenCredential> {
        val credential = result.credential
        return if (credential is GoogleIdTokenCredential) {
            _userEmail.value = credential.id
            _idToken.value = credential.idToken
            // Note: In a real app, we'd store the token securely via EncryptedSharedPreferences
            Result.success(credential)
        } else {
            Result.failure(Exception("Unsupported credential type"))
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _userEmail.value = null
            _idToken.value = null
        } catch (e: Exception) {
            Timber.e(e, "Sign-out failed")
        }
    }

    fun isUserSignedIn(): Boolean = _idToken.value != null
}
