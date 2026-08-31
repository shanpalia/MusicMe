package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MusicMeApplication
import com.example.data.local.PlaylistEntity
import com.example.model.Album
import com.example.model.Artist
import com.example.model.PlaybackState
import com.example.model.Playlist
import com.example.model.Song
import com.example.model.SortOrder
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Splash : Screen()
    data object Permission : Screen()
    data object Home : Screen()
    data object Songs : Screen()
    data object Albums : Screen()
    data object Artists : Screen()
    data object Library : Screen()
    data object Search : Screen()
    data object Settings : Screen()
    data class AlbumDetail(val album: Album) : Screen()
    data class ArtistDetail(val artist: Artist) : Screen()
    data class PlaylistDetail(val playlistId: Long, val playlistName: String) : Screen()
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MusicMeApplication
    private val repository = app.musicRepository
    private val playerManager = app.playerManager

    val allSongs: StateFlow<List<Song>> = repository.allSongs
    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
    val mostPlayed: StateFlow<List<Song>> = repository.mostPlayed
    val albums: StateFlow<List<Album>> = repository.albums
    val artists: StateFlow<List<Artist>> = repository.artists
    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlistEntities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val isScanning: StateFlow<Boolean> = repository.isScanning
    val sortOrder: StateFlow<SortOrder> = repository.sortOrder
    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState

    // Navigation & UI State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSongForMenu = MutableStateFlow<Song?>(null)
    val selectedSongForMenu: StateFlow<Song?> = _selectedSongForMenu.asStateFlow()

    private val _selectedSongForDetails = MutableStateFlow<Song?>(null)
    val selectedSongForDetails: StateFlow<Song?> = _selectedSongForDetails.asStateFlow()

    private val _selectedSongForAddToPlaylist = MutableStateFlow<Song?>(null)
    val selectedSongForAddToPlaylist: StateFlow<Song?> = _selectedSongForAddToPlaylist.asStateFlow()

    private val _isCreatePlaylistDialogOpen = MutableStateFlow(false)
    val isCreatePlaylistDialogOpen: StateFlow<Boolean> = _isCreatePlaylistDialogOpen.asStateFlow()

    private val _isQueueSheetOpen = MutableStateFlow(false)
    val isQueueSheetOpen: StateFlow<Boolean> = _isQueueSheetOpen.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    // Filtered search results
    val searchResults: StateFlow<Triple<List<Song>, List<Album>, List<Artist>>> = combine(
        _searchQuery,
        allSongs,
        albums,
        artists
    ) { query, songList, albumList, artistList ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            Triple(emptyList(), emptyList(), emptyList())
        } else {
            val matchedSongs = songList.filter {
                it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q)
            }
            val matchedAlbums = albumList.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
            val matchedArtists = artistList.filter {
                it.name.lowercase().contains(q)
            }
            Triple(matchedSongs, matchedAlbums, matchedArtists)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Triple(emptyList(), emptyList(), emptyList())
    )

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setPermissionGranted(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) {
            refreshLibrary()
            if (_currentScreen.value is Screen.Permission || _currentScreen.value is Screen.Splash) {
                _currentScreen.value = Screen.Home
            }
        } else {
            if (_currentScreen.value !is Screen.Splash) {
                _currentScreen.value = Screen.Permission
            }
        }
    }

    fun onSplashFinished(permissionGranted: Boolean) {
        _hasPermission.value = permissionGranted
        if (permissionGranted) {
            refreshLibrary()
            _currentScreen.value = Screen.Home
        } else {
            _currentScreen.value = Screen.Permission
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            repository.refreshLibrary()
        }
    }

    fun setSortOrder(order: SortOrder) {
        repository.setSortOrder(order)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun openSongMenu(song: Song) {
        _selectedSongForMenu.value = song
    }

    fun closeSongMenu() {
        _selectedSongForMenu.value = null
    }

    fun openSongDetails(song: Song) {
        _selectedSongForDetails.value = song
        _selectedSongForMenu.value = null
    }

    fun closeSongDetails() {
        _selectedSongForDetails.value = null
    }

    fun openAddToPlaylist(song: Song) {
        _selectedSongForAddToPlaylist.value = song
        _selectedSongForMenu.value = null
    }

    fun closeAddToPlaylist() {
        _selectedSongForAddToPlaylist.value = null
    }

    fun setCreatePlaylistDialogOpen(open: Boolean) {
        _isCreatePlaylistDialogOpen.value = open
    }

    fun setQueueSheetOpen(open: Boolean) {
        _isQueueSheetOpen.value = open
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    // Playback Actions
    fun playSong(song: Song, contextSongs: List<Song> = emptyList()) {
        val listToPlay = if (contextSongs.isNotEmpty()) contextSongs else allSongs.value
        val index = listToPlay.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerManager.playSongList(listToPlay, index)
    }

    fun playAll(songs: List<Song>, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        if (shuffle) {
            if (!playbackState.value.isShuffle) {
                playerManager.toggleShuffle()
            }
            playerManager.playSongList(songs, 0)
        } else {
            playerManager.playSongList(songs, 0)
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun playNext() {
        playerManager.playNext()
    }

    fun playPrevious() {
        playerManager.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerManager.cycleRepeatMode()
    }

    fun playNextInQueue(song: Song) {
        playerManager.playNextInQueue(song)
        closeSongMenu()
    }

    fun addToQueue(song: Song) {
        playerManager.addToQueue(song)
        closeSongMenu()
    }

    fun playQueueIndex(index: Int) {
        playerManager.playQueueIndex(index)
    }

    fun removeFromQueue(index: Int) {
        playerManager.removeFromQueue(index)
    }

    fun clearQueue() {
        playerManager.clearQueue()
    }

    // Favorites & Playlists
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id)
        }
    }

    fun createPlaylist(name: String, initialSong: Song? = null) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val id = repository.createPlaylist(name)
                if (initialSong != null) {
                    repository.addSongToPlaylist(id, initialSong.id)
                }
                _isCreatePlaylistDialogOpen.value = false
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _selectedSongForAddToPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun updatePlaylistName(id: Long, newName: String) {
        viewModelScope.launch {
            repository.updatePlaylistName(id, newName)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
            if (_currentScreen.value is Screen.PlaylistDetail &&
                (_currentScreen.value as Screen.PlaylistDetail).playlistId == id) {
                _currentScreen.value = Screen.Library
            }
        }
    }

    fun getPlaylistSongsFlow(playlistId: Long) = repository.getSongsForPlaylist(playlistId)

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearPlayHistory()
        }
    }
}
