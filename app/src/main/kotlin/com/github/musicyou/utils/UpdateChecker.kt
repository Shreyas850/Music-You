package com.github.musicyou.utils

import com.github.musicyou.BuildConfig
import com.github.musicyou.models.ReleaseInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object UpdateChecker {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    private data class GitHubRelease(
        @SerialName("tag_name") val tagName: String,
        val body: String
    )

    suspend fun checkForUpdate(): ReleaseInfo? {
        return try {
            val response: GitHubRelease = client.get("https://api.github.com/repos/Shreyas850/Music-You/releases/latest") {
                header("X-GitHub-Api-Version", "2022-11-28")
            }.body()

            val latestVersion = response.tagName.removePrefix("v")
            val currentVersion = BuildConfig.VERSION_NAME

            ReleaseInfo(
                isUpdateAvailable = isNewerVersion(latestVersion, currentVersion),
                latestVersion = latestVersion,
                changelog = response.body
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        for (i in 0 until minOf(latestParts.size, currentParts.size)) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }
        return latestParts.size > currentParts.size
    }
}
