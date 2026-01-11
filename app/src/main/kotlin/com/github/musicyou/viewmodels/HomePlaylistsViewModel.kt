package com.github.musicyou.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.innertube.Innertube
import com.github.innertube.requests.playlistPage
import com.github.musicyou.Database
import com.github.musicyou.enums.PlaylistSortBy
import com.github.musicyou.enums.SortOrder
import com.github.musicyou.models.Playlist
import com.github.musicyou.models.PlaylistPreview
import com.github.musicyou.models.Song
import com.github.musicyou.models.SongPlaylistMap
import com.github.musicyou.query
import com.github.musicyou.utils.completed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePlaylistsViewModel : ViewModel() {
    var items: List<PlaylistPreview> by mutableStateOf(emptyList())

    var isImporting by mutableStateOf(false)
    var importError by mutableStateOf<String?>(null)

    suspend fun loadArtists(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ) {
        Database
            .playlistPreviews(sortBy, sortOrder)
            .collect { items = it }
    }

    fun importPlaylist(browseId: String) {
        viewModelScope.launch {
            isImporting = true
            importError = null

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val page = Innertube.playlistPage(browseId)?.completed()?.getOrNull()
                        ?: throw Exception("Failed to fetch playlist")

                    val playlistName = page.title ?: "Imported Playlist"
                    
                    val playlistId = Database.insert(Playlist(name = playlistName, browseId = browseId))
                    
                    val songs = page.songsPage?.items?.map { item ->
                        Song(
                            id = item.key,
                            title = item.info?.name ?: "Unknown",
                            artistsText = item.authors?.joinToString("") { it.name ?: "" },
                            durationText = item.durationText,
                            thumbnailUrl = item.thumbnail?.url
                        )
                    } ?: emptyList()

                    songs.forEach { Database.insert(it) }
                    
                    Database.insertSongPlaylistMaps(
                        songs.mapIndexed { index, song ->
                            SongPlaylistMap(songId = song.id, playlistId = playlistId, position = index)
                        }
                    )
                }
            }

            if (result.isFailure) {
                importError = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            isImporting = false
        }
    }
}
