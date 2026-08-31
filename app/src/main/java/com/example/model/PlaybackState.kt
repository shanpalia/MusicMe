package com.example.model

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val errorMessage: String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedPosition: String
        get() {
            val totalSec = currentPositionMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return String.format("%02d:%02d", min, sec)
        }

    val formattedDuration: String
        get() {
            val totalSec = durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return String.format("%02d:%02d", min, sec)
        }
}
