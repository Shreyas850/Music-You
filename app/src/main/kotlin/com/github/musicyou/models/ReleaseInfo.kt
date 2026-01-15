package com.github.musicyou.models

data class ReleaseInfo(
    val latestVersion: String,
    val isUpdateAvailable: Boolean,
    val changelog: String?
)
