package com.github.musicyou.ui.screens.settings

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.github.innertube.Innertube
import com.github.musicyou.utils.rememberPreference
import com.github.musicyou.utils.youtubeAccessTokenKey
import com.github.musicyou.utils.youtubeUserEmailKey
import com.github.musicyou.utils.youtubeUserNameKey
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun YouTubeAccount() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    var accessToken by rememberPreference(youtubeAccessTokenKey, "")
    var userEmail by rememberPreference(youtubeUserEmailKey, "")
    var userName by rememberPreference(youtubeUserNameKey, "")

    var showConsentDialog by remember { mutableStateOf(false) }

    // Update Innertube token globally
    Innertube.token = accessToken.ifBlank { null }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text("Connect YouTube Account") },
            text = {
                Column {
                    Text("By signing in, Music-You will have read-only access to your YouTube account.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Access public and unlisted playlists.")
                    Text("• No access to your private data or videos.")
                    Text("• No ability to modify your account.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConsentDialog = false
                    scope.launch {
                        try {
                            Log.d("YouTubeAccount", "Creating GetGoogleIdOption")
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId("423592355610-pv775tn2tf32r16b0labksvqsvuvt8co.apps.googleusercontent.com")
                                .build()

                            Log.d("YouTubeAccount", "Creating GetCredentialRequest")
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(
                                context = context,
                                request = request
                            )

                            Log.d("YouTubeAccount", "Credential request successful")
                            val credential = result.credential
                            if (credential is GoogleIdTokenCredential) {
                                // In a real app, you'd exchange this ID token for an access token with YouTube scopes.
                                // For this UI-only task, we simulate the storage.
                                userEmail = credential.id
                                userName = credential.displayName ?: "YouTube User"
                                accessToken = "simulated_access_token" 
                                Innertube.token = accessToken
                            }
                        } catch (e: GetCredentialCancellationException) {
                            Log.d("YouTubeAccount", "User cancelled the account picker (Not now)")
                        } catch (e: Exception) {
                            Log.e("YouTubeAccount", "Sign-in failed", e)
                        }
                    }
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column {
        Text(
            text = "YouTube Account",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (accessToken.isBlank()) {
            ListItem(
                headlineContent = { Text("Sign in with Google") },
                supportingContent = { Text("Connect to access your playlists") },
                leadingContent = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                modifier = Modifier.clickable { showConsentDialog = true }
            )
        } else {
            ListItem(
                headlineContent = { Text(userName) },
                supportingContent = { Text(userEmail) },
                leadingContent = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                trailingContent = {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Sign out",
                        modifier = Modifier.clickable {
                            scope.launch {
                                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                accessToken = ""
                                userEmail = ""
                                userName = ""
                                Innertube.token = null
                            }
                        }
                    )
                }
            )
        }
    }
}
