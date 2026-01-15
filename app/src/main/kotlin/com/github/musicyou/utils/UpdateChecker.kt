package com.github.musicyou.utils

import com.github.api.GitHub
import com.github.musicyou.BuildConfig
import com.github.musicyou.models.ReleaseInfo

object UpdateChecker {
    suspend fun checkForUpdate(): ReleaseInfo? {
        val latestRelease = GitHub.getLastestRelease() ?: return null
        val latestVersion = latestRelease.name.removePrefix("v")
        val currentVersion = BuildConfig.VERSION_NAME

        return ReleaseInfo(
            latestVersion = latestVersion,
            isUpdateAvailable = latestVersion != currentVersion,
            changelog = null // Changelog can be added if available in Release model
        )
    }
}
