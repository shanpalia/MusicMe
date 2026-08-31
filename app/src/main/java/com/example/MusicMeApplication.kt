package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.MusicDatabase
import com.example.data.repository.MusicRepository
import com.example.data.scanner.MediaScanner
import com.example.service.MusicPlayerManager

class MusicMeApplication : Application() {

    companion object {
        const val CHANNEL_ID = "musicme_playback_channel"
        const val CHANNEL_NAME = "Music Playback"
        lateinit var instance: MusicMeApplication
            private set
    }

    val database: MusicDatabase by lazy { MusicDatabase.getDatabase(this) }
    val mediaScanner: MediaScanner by lazy { MediaScanner(this) }
    val musicRepository: MusicRepository by lazy {
        MusicRepository(
            context = this,
            mediaScanner = mediaScanner,
            favoritesDao = database.favoritesDao(),
            playCountDao = database.playCountDao(),
            playlistDao = database.playlistDao()
        )
    }
    val playerManager: MusicPlayerManager by lazy {
        MusicPlayerManager.getInstance(this, musicRepository)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "MusicMe media playback notification and controls"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
