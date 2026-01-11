package com.github.musicyou.utils

import android.util.Log

object PlaylistImportParser {
    private const val TAG = "PlaylistImportParser"

    enum class Platform {
        YouTube,
        YouTubeMusic,
        Spotify,
        Unknown
    }

    data class ImportResult(
        val platform: Platform,
        val playlistId: String?
    )

    fun parse(url: String): ImportResult {
        // YouTube & YouTube Music
        val youtubeRegex = Regex("(?:https?://)?(?:www\\.|music\\.)?youtube\\.com/playlist\\?list=([a-zA-Z0-9_-]+)")
        val youtubeMatch = youtubeRegex.find(url)
        if (youtubeMatch != null) {
            val id = youtubeMatch.groupValues[1]
            val platform = if (url.contains("music.youtube.com")) Platform.YouTubeMusic else Platform.YouTube
            Log.d(TAG, "Detected $platform playlist ID: $id")
            return ImportResult(platform, id)
        }

        // Spotify
        val spotifyRegex = Regex("(?:https?://)?open\\.spotify\\.com/playlist/([a-zA-Z0-9]+)")
        val spotifyMatch = spotifyRegex.find(url)
        if (spotifyMatch != null) {
            val id = spotifyMatch.groupValues[1]
            Log.d(TAG, "Detected Spotify playlist ID: $id")
            return ImportResult(Platform.Spotify, id)
        }

        return ImportResult(Platform.Unknown, null)
    }
}
