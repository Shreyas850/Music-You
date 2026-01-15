package com.github.musicyou.models

import android.net.Uri

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val idToken: String?,
    val profilePictureUri: Uri?
)
