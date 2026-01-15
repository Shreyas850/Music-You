package com.github.musicyou.utils

object PlaylistImportParser {
    enum class Platform {
        YouTube, Spotify, Unknown
    }

    data class Result(
        val platform: Platform,
        val playlistId: String?
    )

    fun parse(url: String): Result {
        return when {
            url.contains("youtube.com/playlist") || url.contains("music.youtube.com/playlist") -> {
                val playlistId = url.substringAfter("list=", "").substringBefore("&")
                Result(Platform.YouTube, playlistId.ifEmpty { null })
            }
            url.contains("spotify.com/playlist") -> {
                val playlistId = url.substringAfter("playlist/", "").substringBefore("?")
                Result(Platform.Spotify, playlistId.ifEmpty { null })
            }
            else -> Result(Platform.Unknown, null)
        }
    }
}
