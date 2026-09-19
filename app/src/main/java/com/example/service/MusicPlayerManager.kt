package com.example.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.repository.MusicRepository
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

class MusicPlayerManager private constructor(
    private val context: Context,
    private val musicRepository: MusicRepository
) : AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val TAG = "MusicPlayerManager"

        @Volatile
        private var INSTANCE: MusicPlayerManager? = null

        fun getInstance(context: Context, repository: MusicRepository): MusicPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicPlayerManager(context.applicationContext, repository).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private var originalQueue: List<Song> = emptyList()

    init {
        // Keep startup lightweight. MediaPlayer is created on first playback.
    }

    private fun initMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { mp ->
                try {
                    mp.start()
                    val dur = mp.duration.toLong().coerceAtLeast(0L)
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = true,
                        isPaused = false,
                        durationMs = dur,
                        errorMessage = null
                    )
                    startProgressTracker()
                    updateService()

                    _playbackState.value.currentSong?.let { song ->
                        playerScope.launch(Dispatchers.IO) {
                            musicRepository.recordSongPlayed(song.id)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in onPrepared", e)
                }
            }
            setOnCompletionListener {
                handleSongCompletion()
            }
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isPaused = false,
                    errorMessage = "Playback error occurred ($what, $extra)"
                )
                stopProgressTracker()
                updateService()
                true
            }
        }
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        originalQueue = songs.toList()
        val queue = if (_playbackState.value.isShuffle) {
            val startSong = songs.getOrNull(startIndex) ?: songs.first()
            val remaining = songs.filter { it.id != startSong.id }.shuffled()
            listOf(startSong) + remaining
        } else {
            songs.toList()
        }

        val actualIndex = if (_playbackState.value.isShuffle) 0 else startIndex.coerceIn(0, queue.size - 1)
        _playbackState.value = _playbackState.value.copy(
            queue = queue,
            currentQueueIndex = actualIndex
        )

        playCurrentQueueIndex()
    }

    fun playSingleSong(song: Song) {
        playSongList(listOf(song), 0)
    }

    fun playQueueIndex(index: Int) {
        val currentQueue = _playbackState.value.queue
        if (index in currentQueue.indices) {
            _playbackState.value = _playbackState.value.copy(
                currentQueueIndex = index
            )
            playCurrentQueueIndex()
        }
    }

    private fun playCurrentQueueIndex() {
        val state = _playbackState.value
        val song = state.queue.getOrNull(state.currentQueueIndex) ?: return

        if (!requestAudioFocus()) {
            _playbackState.value = _playbackState.value.copy(
                errorMessage = "Could not obtain audio focus"
            )
            return
        }

        try {
            initMediaPlayer()
            mediaPlayer?.apply {
                reset()
                setDataSource(context, song.contentUri)
                _playbackState.value = _playbackState.value.copy(
                    currentSong = song,
                    currentPositionMs = 0L,
                    durationMs = song.duration,
                    errorMessage = null
                )
                prepareAsync()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load audio for ${song.title}", e)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                errorMessage = "Cannot play file: ${song.title}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error playing ${song.title}", e)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                errorMessage = "Playback failed: ${e.localizedMessage}"
            )
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        val currentSong = _playbackState.value.currentSong

        if (currentSong == null && _playbackState.value.queue.isNotEmpty()) {
            playQueueIndex(0)
            return
        }

        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isPaused = true
            )
            stopProgressTracker()
            updateService()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing", e)
        }
    }

    fun resume() {
        try {
            if (requestAudioFocus()) {
                mediaPlayer?.start()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    isPaused = false
                )
                startProgressTracker()
                updateService()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming", e)
            playCurrentQueueIndex()
        }
    }

    fun playNext() {
        val state = _playbackState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        val nextIndex = state.currentQueueIndex + 1
        if (nextIndex < queue.size) {
            playQueueIndex(nextIndex)
        } else {
            if (state.repeatMode == RepeatMode.ALL) {
                playQueueIndex(0)
            } else {
                pause()
                seekTo(0L)
            }
        }
    }

    fun playPrevious() {
        val state = _playbackState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        // If played more than 3 seconds, replay current song from beginning
        if (state.currentPositionMs > 3000) {
            seekTo(0L)
            return
        }

        val prevIndex = state.currentQueueIndex - 1
        if (prevIndex >= 0) {
            playQueueIndex(prevIndex)
        } else {
            if (state.repeatMode == RepeatMode.ALL) {
                playQueueIndex(queue.size - 1)
            } else {
                seekTo(0L)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _playbackState.value.durationMs.coerceAtLeast(0L))
        try {
            mediaPlayer?.seekTo(target.toInt())
            _playbackState.value = _playbackState.value.copy(
                currentPositionMs = target
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }

    fun toggleShuffle() {
        val currentShuffle = _playbackState.value.isShuffle
        val newShuffle = !currentShuffle
        val currentSong = _playbackState.value.currentSong

        val newQueue = if (newShuffle) {
            if (currentSong != null) {
                listOf(currentSong) + originalQueue.filter { it.id != currentSong.id }.shuffled()
            } else {
                originalQueue.shuffled()
            }
        } else {
            originalQueue.toList()
        }

        val newIndex = if (currentSong != null) {
            newQueue.indexOfFirst { it.id == currentSong.id }.coerceAtLeast(0)
        } else 0

        _playbackState.value = _playbackState.value.copy(
            isShuffle = newShuffle,
            queue = newQueue,
            currentQueueIndex = newIndex
        )
    }

    fun cycleRepeatMode() {
        val current = _playbackState.value.repeatMode
        val next = when (current) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = next)
    }

    // Queue manipulation
    fun playNextInQueue(song: Song) {
        val currentQueue = _playbackState.value.queue.toMutableList()
        val currentIndex = _playbackState.value.currentQueueIndex

        // If queue is empty
        if (currentQueue.isEmpty()) {
            playSongList(listOf(song), 0)
            return
        }

        // Insert right after current song
        val insertIndex = (currentIndex + 1).coerceAtMost(currentQueue.size)
        currentQueue.add(insertIndex, song)

        _playbackState.value = _playbackState.value.copy(
            queue = currentQueue
        )
    }

    fun addToQueue(song: Song) {
        val currentQueue = _playbackState.value.queue.toMutableList()
        if (currentQueue.isEmpty()) {
            playSongList(listOf(song), 0)
            return
        }

        currentQueue.add(song)
        _playbackState.value = _playbackState.value.copy(
            queue = currentQueue
        )
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _playbackState.value.queue.toMutableList()
        if (index !in currentQueue.indices) return

        val currentIndex = _playbackState.value.currentQueueIndex
        val isRemovingCurrent = (index == currentIndex)

        currentQueue.removeAt(index)

        var newCurrentIndex = when {
            index < currentIndex -> currentIndex - 1
            index == currentIndex -> {
                if (index < currentQueue.size) index else currentQueue.size - 1
            }
            else -> currentIndex
        }

        if (currentQueue.isEmpty()) {
            mediaPlayer?.stop()
            _playbackState.value = PlaybackState()
            stopProgressTracker()
            updateService()
            return
        }

        _playbackState.value = _playbackState.value.copy(
            queue = currentQueue,
            currentQueueIndex = newCurrentIndex
        )

        if (isRemovingCurrent) {
            playCurrentQueueIndex()
        }
    }

    fun clearQueue() {
        mediaPlayer?.stop()
        _playbackState.value = PlaybackState()
        stopProgressTracker()
        updateService()
    }

    private fun handleSongCompletion() {
        when (_playbackState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                mediaPlayer?.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracker()
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                val nextIndex = _playbackState.value.currentQueueIndex + 1
                if (nextIndex < _playbackState.value.queue.size) {
                    playNext()
                } else {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isPaused = false,
                        currentPositionMs = 0L
                    )
                    stopProgressTracker()
                    updateService()
                }
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = playerScope.launch {
            while (isActive) {
                try {
                    val mp = mediaPlayer
                    if (mp != null && mp.isPlaying) {
                        val pos = mp.currentPosition.toLong()
                        val dur = mp.duration.toLong().coerceAtLeast(0L)
                        _playbackState.value = _playbackState.value.copy(
                            currentPositionMs = pos,
                            durationMs = if (dur > 0) dur else _playbackState.value.durationMs
                        )
                    }
                } catch (e: Exception) {
                    // ignore transient exceptions during state switches
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
            audioFocusRequest = req
            return audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            return audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                try {
                    mediaPlayer?.setVolume(0.2f, 0.2f)
                } catch (e: Exception) {}
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                try {
                    mediaPlayer?.setVolume(1.0f, 1.0f)
                    if (_playbackState.value.isPaused) {
                        resume()
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private fun updateService() {
        val state = _playbackState.value
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = if (state.isPlaying || state.isPaused) {
                MusicPlaybackService.ACTION_UPDATE_NOTIFICATION
            } else {
                MusicPlaybackService.ACTION_STOP
            }
        }
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
        }
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
