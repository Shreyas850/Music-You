package com.github.musicyou.utils

import android.content.Context
import android.net.Uri
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.github.musicyou.models.AuthUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _authUser = MutableStateFlow(loadUser())
    val authUser: StateFlow<AuthUser?> = _authUser.asStateFlow()

    private fun loadUser(): AuthUser? {
        val id = sharedPreferences.getString("user_id", null) ?: return null
        return AuthUser(
            id = id,
            email = sharedPreferences.getString("user_email", null),
            displayName = sharedPreferences.getString("user_name", null),
            idToken = sharedPreferences.getString("user_token", null),
            profilePictureUri = sharedPreferences.getString("user_picture", null)?.let { Uri.parse(it) }
        )
    }

    private fun saveUser(user: AuthUser) {
        sharedPreferences.edit().apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            putString("user_name", user.displayName)
            putString("user_token", user.idToken)
            putString("user_picture", user.profilePictureUri?.toString())
            apply()
        }
        _authUser.value = user
    }

    private fun clearUser() {
        sharedPreferences.edit().clear().apply()
        _authUser.value = null
    }

    suspend fun signIn(serverClientId: String): Result<AuthUser> {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        }
    }

    private fun handleSignIn(result: GetCredentialResponse): Result<AuthUser> {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val user = AuthUser(
                id = credential.id,
                email = credential.id, // The ID field in GoogleIdTokenCredential is the email
                displayName = credential.displayName,
                idToken = credential.idToken,
                profilePictureUri = credential.profilePictureUri
            )
            saveUser(user)
            return Result.success(user)
        }
        return Result.failure(Exception("Unsupported credential type"))
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            // Log error
        }
        clearUser()
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
