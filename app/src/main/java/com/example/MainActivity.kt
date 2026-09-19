package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.MainBottomNav
import com.example.ui.components.MiniPlayer
import com.example.ui.components.QueueBottomSheet
import com.example.ui.components.SongDetailsDialog
import com.example.ui.components.SongMenuBottomSheet
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.ArtistDetailScreen
import com.example.ui.screens.ArtistsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PermissionScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SongsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MusicMeTheme
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(true)
        window.navigationBarColor = android.graphics.Color.BLACK
        window.statusBarColor = android.graphics.Color.BLACK

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    viewModel.isNowPlayingExpanded.value -> viewModel.setNowPlayingExpanded(false)
                    viewModel.selectedSongForMenu.value != null -> viewModel.closeSongMenu()
                    viewModel.selectedSongForDetails.value != null -> viewModel.closeSongDetails()
                    viewModel.selectedSongForAddToPlaylist.value != null -> viewModel.closeAddToPlaylist()
                    viewModel.isQueueSheetOpen.value -> viewModel.setQueueSheetOpen(false)
                    viewModel.currentScreen.value is Screen.AlbumDetail -> viewModel.navigateTo(Screen.Albums)
                    viewModel.currentScreen.value is Screen.ArtistDetail -> viewModel.navigateTo(Screen.Artists)
                    viewModel.currentScreen.value is Screen.PlaylistDetail -> viewModel.navigateTo(Screen.Library)
                    viewModel.currentScreen.value == Screen.Settings || viewModel.currentScreen.value == Screen.Search -> viewModel.navigateTo(Screen.Home)
                    viewModel.currentScreen.value == Screen.Songs || viewModel.currentScreen.value == Screen.Albums || viewModel.currentScreen.value == Screen.Artists || viewModel.currentScreen.value == Screen.Library -> viewModel.navigateTo(Screen.Home)
                    viewModel.currentScreen.value == Screen.Home -> finish()
                    else -> finish()
                }
            }
        })

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            MusicMeTheme(themeMode = themeMode) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val selectedSongForMenu by viewModel.selectedSongForMenu.collectAsState()
    val selectedSongForDetails by viewModel.selectedSongForDetails.collectAsState()
    val selectedSongForAddToPlaylist by viewModel.selectedSongForAddToPlaylist.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val isQueueSheetOpen by viewModel.isQueueSheetOpen.collectAsState()

    // Required audio permissions depending on Android version
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultsMap ->
        val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resultsMap[Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            resultsMap[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        viewModel.setPermissionGranted(audioGranted)
    }

    fun checkPermission(): Boolean {
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        permissionLauncher.launch(permissionsToRequest)
    }

    val showBottomNav = currentScreen in listOf(
        Screen.Home,
        Screen.Songs,
        Screen.Albums,
        Screen.Artists,
        Screen.Library,
        Screen.Search
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Mini player floating right above the navigation bar
                        MiniPlayer(
                            playbackState = playbackState,
                            onExpand = { viewModel.setNowPlayingExpanded(true) },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onPlayNext = { viewModel.playNext() }
                        )

                        MainBottomNav(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = currentScreen,
                label = "ScreenTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { screen ->
                when (screen) {
                    Screen.Splash -> {
                        SplashScreen(
                            onSplashFinished = {
                                val hasPerm = checkPermission()
                                viewModel.onSplashFinished(hasPerm)
                            }
                        )
                    }
                    Screen.Permission -> {
                        PermissionScreen(
                            onRequestPermission = { requestPermissions() }
                        )
                    }
                    Screen.Home -> {
                        HomeScreen(viewModel = viewModel)
                    }
                    Screen.Songs -> {
                        SongsScreen(viewModel = viewModel)
                    }
                    Screen.Albums -> {
                        AlbumsScreen(viewModel = viewModel)
                    }
                    Screen.Artists -> {
                        ArtistsScreen(viewModel = viewModel)
                    }
                    Screen.Library -> {
                        LibraryScreen(viewModel = viewModel)
                    }
                    Screen.Search -> {
                        SearchScreen(viewModel = viewModel)
                    }
                    Screen.Settings -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                    is Screen.AlbumDetail -> {
                        AlbumDetailScreen(
                            album = screen.album,
                            viewModel = viewModel
                        )
                    }
                    is Screen.ArtistDetail -> {
                        ArtistDetailScreen(
                            artist = screen.artist,
                            viewModel = viewModel
                        )
                    }
                    is Screen.PlaylistDetail -> {
                        PlaylistDetailScreen(
                            playlistId = screen.playlistId,
                            initialName = screen.playlistName,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Full-screen Now Playing Overlay
        NowPlayingScreen(viewModel = viewModel)

        // Song Options Menu Modal Bottom Sheet
        if (selectedSongForMenu != null) {
            val song = selectedSongForMenu!!
            SongMenuBottomSheet(
                song = song,
                onDismiss = { viewModel.closeSongMenu() },
                onPlayNext = { viewModel.playNextInQueue(song) },
                onAddToQueue = { viewModel.addToQueue(song) },
                onAddToPlaylist = { viewModel.openAddToPlaylist(song) },
                onToggleFavorite = {
                    viewModel.toggleFavorite(song)
                    viewModel.closeSongMenu()
                },
                onSongDetails = { viewModel.openSongDetails(song) }
            )
        }

        // Song Details Dialog
        if (selectedSongForDetails != null) {
            SongDetailsDialog(
                song = selectedSongForDetails!!,
                onDismiss = { viewModel.closeSongDetails() }
            )
        }

        // Add to Playlist Dialog
        if (selectedSongForAddToPlaylist != null) {
            AddToPlaylistDialog(
                song = selectedSongForAddToPlaylist!!,
                playlists = playlists,
                onDismiss = { viewModel.closeAddToPlaylist() },
                onSelectPlaylist = { playlistId ->
                    viewModel.addSongToPlaylist(playlistId, selectedSongForAddToPlaylist!!.id)
                },
                onCreateNewPlaylist = { name ->
                    viewModel.createPlaylist(name, selectedSongForAddToPlaylist)
                    viewModel.closeAddToPlaylist()
                }
            )
        }

        // Queue Bottom Sheet
        if (isQueueSheetOpen) {
            QueueBottomSheet(
                playbackState = playbackState,
                onDismiss = { viewModel.setQueueSheetOpen(false) },
                onSelectIndex = { viewModel.playQueueIndex(it) },
                onRemoveIndex = { viewModel.removeFromQueue(it) },
                onClearQueue = { viewModel.clearQueue() }
            )
        }
    }
}
