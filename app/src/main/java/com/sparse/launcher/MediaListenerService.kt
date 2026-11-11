package com.sparse.launcher

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MediaListenerService : NotificationListenerService(),
    MediaSessionManager.OnActiveSessionsChangedListener {

    private var mediaSessionManager: MediaSessionManager? = null
    private val activeControllers = mutableListOf<Pair<MediaController, MediaController.Callback>>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationStore.post(sbn.packageName)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Only clear the dot if no remaining notifications from this package
        val remaining = activeNotifications?.any {
            it.packageName == sbn.packageName && it.id != sbn.id
        } ?: false
        if (!remaining) NotificationStore.remove(sbn.packageName)
    }

    override fun onListenerConnected() {
        // Seed initial notification set
        activeNotifications?.forEach { NotificationStore.post(it.packageName) }
        try {
            val msm = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            mediaSessionManager = msm
            val cn = ComponentName(this, MediaListenerService::class.java)
            msm.addOnActiveSessionsChangedListener(this, cn)
            onActiveSessionsChanged(msm.getActiveSessions(cn))
        } catch (_: Exception) {}
    }

    override fun onListenerDisconnected() {
        mediaSessionManager?.removeOnActiveSessionsChangedListener(this)
        clearCallbacks()
        NowPlayingStore.update(null)
        NotificationStore.clear()
    }

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        clearCallbacks()
        controllers?.filter { it.packageName in SUPPORTED_PACKAGES }
            ?.forEach { registerController(it) }
        syncNowPlaying()
    }

    private fun registerController(controller: MediaController) {
        val cb = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) = syncNowPlaying()
            override fun onPlaybackStateChanged(state: PlaybackState?) = syncNowPlaying()
        }
        controller.registerCallback(cb)
        activeControllers += controller to cb
    }

    private fun clearCallbacks() {
        activeControllers.forEach { (ctrl, cb) ->
            try { ctrl.unregisterCallback(cb) } catch (_: Exception) {}
        }
        activeControllers.clear()
    }

    private fun syncNowPlaying() {
        val playing = activeControllers
            .map { (ctrl, _) -> ctrl }
            .firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
        if (playing != null) {
            val meta = playing.metadata ?: run { NowPlayingStore.update(null); return }
            val title = meta.getString(MediaMetadata.METADATA_KEY_TITLE) ?: run { NowPlayingStore.update(null); return }
            val artist = meta.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: ""
            NowPlayingStore.update(NowPlayingInfo(title, artist))
        } else {
            NowPlayingStore.update(null)
        }
    }

    companion object {
        private val SUPPORTED_PACKAGES = setOf(
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
        )
    }
}
