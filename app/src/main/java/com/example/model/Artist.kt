package com.example.model

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val songs: List<Song> = emptyList()
)
