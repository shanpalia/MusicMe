package com.example.model

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int = 0,
    val songs: List<Song> = emptyList()
)
