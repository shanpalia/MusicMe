package com.example.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val contentUri: Uri,
    val size: Long,
    val dateAdded: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L
) {
    val albumArtUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")

    val formattedDuration: String
        get() {
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = size / (1024.0 * 1024.0)
            return String.format("%.1f MB", mb)
        }
}

enum class SortOrder {
    TITLE,
    ARTIST,
    ALBUM,
    DATE_ADDED,
    DURATION
}
