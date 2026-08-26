package com.github.musicyou.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.musicyou.database
import com.github.musicyou.enums.SongSortBy
import com.github.musicyou.enums.SortOrder
import com.github.musicyou.models.Song

class HomeSongsViewModel : ViewModel() {
    var items: List<Song> by mutableStateOf(emptyList())

    suspend fun loadSongs(
        sortBy: SongSortBy,
        sortOrder: SortOrder
    ) {
        database
            .songs(sortBy, sortOrder)
            .collect { items = it }
    }
}