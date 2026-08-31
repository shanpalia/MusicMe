package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.session.MediaSession
import android.media.session.PlaybackState as AndroidPlaybackState
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MusicMeApplication
import com.example.R
import com.example.model.PlaybackState
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicPlaybackService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.ACTION_STOP"
        const val ACTION_UPDATE_NOTIFICATION = "com.example.ACTION_UPDATE_NOTIFICATION"
        private const val TAG = "MusicPlaybackService"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "MusicMeMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    getApp().playerManager.resume()
                }

                override fun onPause() {
                    getApp().playerManager.pause()
                }

                override fun onSkipToNext() {
                    getApp().playerManager.playNext()
                }

                override fun onSkipToPrevious() {
                    getApp().playerManager.playPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    getApp().playerManager.seekTo(pos)
                }

                override fun onStop() {
                    getApp().playerManager.pause()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val playerManager = getApp().playerManager

        when (action) {
            ACTION_PLAY_PAUSE -> playerManager.togglePlayPause()
            ACTION_NEXT -> playerManager.playNext()
            ACTION_PREVIOUS -> playerManager.playPrevious()
            ACTION_STOP -> {
                playerManager.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_NOTIFICATION, null -> {
                // update notification with current state
            }
        }

        val state = playerManager.playbackState.value
        val currentSong = state.currentSong

        if (currentSong != null) {
            updateMediaSessionState(state)
            serviceScope.launch {
                val albumBitmap = loadArtworkBitmap(currentSong)
                val notification = buildNotification(currentSong, state.isPlaying, albumBitmap)
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun updateMediaSessionState(state: PlaybackState) {
        val pbState = if (state.isPlaying) {
            AndroidPlaybackState.STATE_PLAYING
        } else if (state.isPaused) {
            AndroidPlaybackState.STATE_PAUSED
        } else {
            AndroidPlaybackState.STATE_STOPPED
        }

        val actions = AndroidPlaybackState.ACTION_PLAY or
                AndroidPlaybackState.ACTION_PAUSE or
                AndroidPlaybackState.ACTION_PLAY_PAUSE or
                AndroidPlaybackState.ACTION_SKIP_TO_NEXT or
                AndroidPlaybackState.ACTION_SKIP_TO_PREVIOUS or
                AndroidPlaybackState.ACTION_SEEK_TO

        mediaSession?.setPlaybackState(
            AndroidPlaybackState.Builder()
                .setState(pbState, state.currentPositionMs, 1.0f)
                .setActions(actions)
                .build()
        )
    }

    private fun buildNotification(
        song: Song,
        isPlaying: Boolean,
        artworkBitmap: Bitmap?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPending = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPausePending = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPending = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(this, MusicMeApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.musicme_icon)
            .setContentTitle(song.title)
            .setContentText("${song.artist} • ${song.album}")
            .setSubText("MusicMe")
            .setLargeIcon(artworkBitmap)
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(stopPending)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPending)
            .addAction(playPauseIcon, playPauseTitle, playPausePending)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPending)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession?.sessionToken?.let {
                        android.support.v4.media.session.MediaSessionCompat.Token.fromToken(it)
                    })
            )
            .build()
    }

    private suspend fun loadArtworkBitmap(song: Song): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this@MusicPlaybackService, song.contentUri)
            val rawArt = retriever.embeddedPicture
            retriever.release()

            if (rawArt != null && rawArt.isNotEmpty()) {
                return@withContext BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size)
            }
        } catch (e: Exception) {
            // fallback
        }

        // Try album art uri
        try {
            contentResolver.openInputStream(song.albumArtUri)?.use { stream ->
                return@withContext BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            // no album art
        }
        null
    }

    private fun getApp(): MusicMeApplication {
        return applicationContext as MusicMeApplication
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
