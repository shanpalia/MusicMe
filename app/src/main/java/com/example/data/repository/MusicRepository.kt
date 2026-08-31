package com.example.data.repository

import android.content.Context
import com.example.data.local.FavoriteEntity
import com.example.data.local.FavoritesDao
import com.example.data.local.PlayCountDao
import com.example.data.local.PlayCountEntity
import com.example.data.local.PlaylistDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongEntity
import com.example.data.scanner.MediaScanner
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.model.SortOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val mediaScanner: MediaScanner,
    private val favoritesDao: FavoritesDao,
    private val playCountDao: PlayCountDao,
    private val playlistDao: PlaylistDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    val rawSongs: StateFlow<List<Song>> = _rawSongs

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    val favoriteIds: Flow<List<FavoriteEntity>> = favoritesDao.getAllFavorites()
    val playCounts: Flow<List<PlayCountEntity>> = playCountDao.getAllPlayCounts()
    val recentlyPlayedEntities: Flow<List<PlayCountEntity>> = playCountDao.getRecentlyPlayed(30)
    val playlistEntities: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    // Combined live songs with favorite & play count metadata
    val allSongs: StateFlow<List<Song>> = combine(
        _rawSongs,
        favoriteIds,
        playCounts,
        _sortOrder
    ) { songs, favs, counts, sort ->
        val favSet = favs.map { it.songId }.toSet()
        val countMap = counts.associate { it.songId to Pair(it.count, it.lastPlayedTimestamp) }

        val enriched = songs.map { song ->
            val stat = countMap[song.id]
            song.copy(
                isFavorite = favSet.contains(song.id),
                playCount = stat?.first ?: 0,
                lastPlayedTimestamp = stat?.second ?: 0L
            )
        }

        when (sort) {
            SortOrder.TITLE -> enriched.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> enriched.sortedBy { it.artist.lowercase() }
            SortOrder.ALBUM -> enriched.sortedBy { it.album.lowercase() }
            SortOrder.DATE_ADDED -> enriched.sortedByDescending { it.dateAdded }
            SortOrder.DURATION -> enriched.sortedByDescending { it.duration }
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val favoriteSongs: StateFlow<List<Song>> = allSongs.combine(favoriteIds) { songs, favs ->
        val favMap = favs.associateBy { it.songId }
        songs.filter { favMap.containsKey(it.id) }
            .sortedByDescending { favMap[it.id]?.addedAt ?: 0L }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentlyPlayed: StateFlow<List<Song>> = allSongs.combine(recentlyPlayedEntities) { songs, recent ->
        val songMap = songs.associateBy { it.id }
        recent.mapNotNull { entity -> songMap[entity.songId] }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val mostPlayed: StateFlow<List<Song>> = allSongs.combine(playCounts) { songs, counts ->
        val songMap = songs.associateBy { it.id }
        counts.filter { it.count > 0 }
            .sortedByDescending { it.count }
            .take(20)
            .mapNotNull { entity -> songMap[entity.songId] }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val albums: StateFlow<List<Album>> = allSongs.combine(_rawSongs) { songs, _ ->
        mediaScanner.groupIntoAlbums(songs)
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val artists: StateFlow<List<Artist>> = allSongs.combine(_rawSongs) { songs, _ ->
        mediaScanner.groupIntoArtists(songs)
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    suspend fun refreshLibrary(): List<Song> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            val scanned = mediaScanner.scanDeviceAudio()
            _rawSongs.value = scanned
            scanned
        } finally {
            _isScanning.value = false
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val isFav = favoritesDao.isFavoriteSync(songId)
        if (isFav) {
            favoritesDao.removeFavorite(songId)
        } else {
            favoritesDao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun recordSongPlayed(songId: Long) = withContext(Dispatchers.IO) {
        val current = playCountDao.getPlayCountForSong(songId)
        val newCount = (current?.count ?: 0) + 1
        playCountDao.insertOrUpdate(
            PlayCountEntity(
                songId = songId,
                count = newCount,
                lastPlayedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearPlayHistory() = withContext(Dispatchers.IO) {
        playCountDao.clearHistory()
    }

    // Playlists
    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.createPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun updatePlaylistName(id: Long, newName: String) = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(id)
        if (playlist != null) {
            playlistDao.updatePlaylist(playlist.copy(name = newName.trim()))
        }
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(id)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addSongToPlaylist(
            PlaylistSongEntity(playlistId = playlistId, songId = songId)
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return combine(allSongs, playlistDao.getSongsForPlaylist(playlistId)) { songs, playlistSongs ->
            val songMap = songs.associateBy { it.id }
            playlistSongs.mapNotNull { playlistSong -> songMap[playlistSong.songId] }
        }
    }
}
