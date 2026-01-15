package com.github.musicyou.service

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.github.musicyou.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@UnstableApi
class MusicDownloadManager(private val context: Context) {
    private val downloadManager = DownloadUtil.getDownloadManager(context)
    
    private val _downloads = MutableStateFlow<Map<String, Download>>(emptyMap())
    val downloads = _downloads.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                _downloads.update { it + (download.request.id to download) }
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                _downloads.update { it - download.request.id }
            }

            override fun onInitialized(downloadManager: DownloadManager) {
                val currentDownloads = mutableMapOf<String, Download>()
                val cursor = downloadManager.downloadIndex.getDownloads()
                while (cursor.moveToNext()) {
                    val download = cursor.download
                    currentDownloads[download.request.id] = download
                }
                cursor.close()
                _downloads.value = currentDownloads
            }
        })

        scope.launch {
            while (true) {
                if (downloadManager.currentDownloads.isNotEmpty()) {
                    val currentDownloads = _downloads.value.toMutableMap()
                    downloadManager.currentDownloads.forEach { download ->
                        currentDownloads[download.request.id] = download
                    }
                    _downloads.value = currentDownloads
                }
                delay(1000)
            }
        }
    }

    fun download(song: Song, url: String) {
        val downloadRequest = DownloadRequest.Builder(song.id, url.toUri())
            .setCustomCacheKey(song.id)
            .build()
        DownloadService.sendAddDownload(
            context,
            MusicDownloadService::class.java,
            downloadRequest,
            false
        )
    }

    fun remove(songId: String) {
        DownloadService.sendRemoveDownload(
            context,
            MusicDownloadService::class.java,
            songId,
            false
        )
    }

    private fun String.toUri() = android.net.Uri.parse(this)

    companion object {
        @Volatile
        private var instance: MusicDownloadManager? = null

        fun getInstance(context: Context): MusicDownloadManager {
            return instance ?: synchronized(this) {
                instance ?: MusicDownloadManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
