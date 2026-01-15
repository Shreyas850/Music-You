package com.github.musicyou.service

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import com.github.musicyou.enums.ExoPlayerDiskCacheMaxSize
import com.github.musicyou.utils.exoPlayerDiskCacheMaxSizeKey
import com.github.musicyou.utils.getEnum
import com.github.musicyou.utils.preferences
import java.io.File
import java.util.concurrent.Executors

@UnstableApi
object DownloadUtil {
    private var downloadManager: DownloadManager? = null
    private var cache: Cache? = null
    private var dataSourceFactory: DataSource.Factory? = null

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager {
        if (downloadManager == null) {
            val databaseProvider = StandaloneDatabaseProvider(context)
            downloadManager = DownloadManager(
                context,
                databaseProvider,
                getCache(context),
                getHttpDataSourceFactory(),
                Executors.newFixedThreadPool(6)
            ).apply {
                maxParallelDownloads = 3
            }
        }
        return downloadManager!!
    }

    @Synchronized
    fun getCache(context: Context): Cache {
        if (cache == null) {
            val preferences = context.preferences
            val cacheEvictor = when (val size =
                preferences.getEnum(exoPlayerDiskCacheMaxSizeKey, ExoPlayerDiskCacheMaxSize.`2GB`)) {
                ExoPlayerDiskCacheMaxSize.Unlimited -> NoOpCacheEvictor()
                else -> LeastRecentlyUsedCacheEvictor(size.bytes)
            }
            
            val directory = File(context.cacheDir, "exoplayer")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            cache = SimpleCache(directory, cacheEvictor, StandaloneDatabaseProvider(context))
        }
        return cache!!
    }

    @Synchronized
    fun getHttpDataSourceFactory(): DataSource.Factory {
        if (dataSourceFactory == null) {
            dataSourceFactory = DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(16000)
                .setReadTimeoutMs(8000)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")
        }
        return dataSourceFactory!!
    }
}
