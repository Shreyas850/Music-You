package com.github.musicyou.models

data class ReleaseInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val changelog: String
)
