package com.sparse.launcher

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NowPlayingInfo(val title: String, val artist: String)

object NowPlayingStore {
    private val _nowPlaying = MutableStateFlow<NowPlayingInfo?>(null)
    val nowPlaying: StateFlow<NowPlayingInfo?> = _nowPlaying.asStateFlow()

    fun update(info: NowPlayingInfo?) { _nowPlaying.value = info }

    fun isListenerEnabled(ctx: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)
}
