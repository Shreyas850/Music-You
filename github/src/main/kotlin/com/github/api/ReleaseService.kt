package com.github.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class ReleaseService(
    private val client: HttpClient
) {
    suspend fun getReleases(): List<Release> {
        return try {
            client.get("https://api.github.com/repos/Shreyas850/Music-You/releases") {
                header("X-GitHub-Api-Version", "2022-11-28")
            }.body()
        } catch (_: Exception) {
            try {
                client.get("https://api.github.com/repos/DanielSevillano/music-you/releases") {
                    header("X-GitHub-Api-Version", "2022-11-28")
                }.body()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}