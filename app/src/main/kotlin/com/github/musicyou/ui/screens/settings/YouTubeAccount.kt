package com.github.musicyou.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.musicyou.utils.AuthManager
import kotlinx.coroutines.launch

@Composable
fun YouTubeAccount() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager.getInstance(context) }
    val user by authManager.authUser.collectAsState()
    
    var isLoading by remember { mutableStateOf(false) }

    // IMPORTANT: Replace with your Web Client ID from Google Cloud Console for production
    val serverClientId = "940654060010-8p8e5t8k0q8p8e5t8k0q8p8e5t8k0q8p.apps.googleusercontent.com"

    Column {
        Text(
            text = "Google Account",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (user != null) {
            ListItem(
                headlineContent = { Text(user?.displayName ?: "Google User") },
                supportingContent = { Text(user?.email ?: "") },
                leadingContent = {
                    if (user?.profilePictureUri != null) {
                        AsyncImage(
                            model = user?.profilePictureUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = {
                        scope.launch {
                            authManager.signOut()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        } else {
            ListItem(
                headlineContent = { Text("Sign in with Google") },
                supportingContent = { Text("Access your music across devices") },
                leadingContent = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Login,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.clickable(enabled = !isLoading) {
                    isLoading = true
                    scope.launch {
                        val result = authManager.signIn(serverClientId)
                        if (result.isFailure) {
                            // Handle error (e.g., show toast or log)
                            println("Login failed: ${result.exceptionOrNull()?.message}")
                        }
                        isLoading = false
                    }
                }
            )
        }
    }
}
