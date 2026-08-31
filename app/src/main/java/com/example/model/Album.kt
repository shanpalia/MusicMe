package com.example.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
) {
    val albumArtUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$id")
}
