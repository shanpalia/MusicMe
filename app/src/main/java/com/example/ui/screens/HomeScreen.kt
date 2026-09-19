package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.Song
import com.example.ui.components.EqualizerBars
import com.example.ui.components.SongItemRow
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allSongs by viewModel.allSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val currentOrRecentSong = playbackState.currentSong ?: recentlyPlayed.firstOrNull() ?: allSongs.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.musicme_icon),
                                contentDescription = "MusicMe App Icon",
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MusicME By PaliaAPK HUB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.35).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Search) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .testTag("home_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = {
                            val next = if (themeMode == com.example.ui.theme.AppThemeMode.DARK)
                                com.example.ui.theme.AppThemeMode.LIGHT
                            else
                                com.example.ui.theme.AppThemeMode.DARK
                            viewModel.setThemeMode(next)
                        },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .testTag("home_theme_button")
                    ) {
                        Icon(
                            imageVector = if (themeMode == com.example.ui.theme.AppThemeMode.DARK)
                                Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .testTag("home_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (allSongs.isEmpty() && !isScanning) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Music Found",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add audio files to your device and tap refresh to build your music library.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.refreshLibrary() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_state_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh Library")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Sleek Greeting Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your Music Space",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Continue Listening / Current Playing Hero Card
                if (currentOrRecentSong != null) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = if (playbackState.isPlaying) "Now Playing" else "Continue Listening",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = IndigoPrimary.copy(alpha = 0.25f))
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable {
                                        if (playbackState.currentSong?.id == currentOrRecentSong.id) {
                                            viewModel.setNowPlayingExpanded(true)
                                        } else {
                                            viewModel.playSong(currentOrRecentSong, allSongs)
                                        }
                                    }
                                    .testTag("continue_listening_card"),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    Color(0xFF222634),
                                                    Color(0xFF13151D)
                                                )
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Artwork
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.background),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(currentOrRecentSong.albumArtUri)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Artwork",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(72.dp),
                                                placeholder = painterResource(id = R.drawable.musicme_icon),
                                                error = painterResource(id = R.drawable.musicme_icon)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = IndigoLight.copy(alpha = 0.5f),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Song Info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = currentOrRecentSong.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${currentOrRecentSong.artist} • ${currentOrRecentSong.album}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = IndigoLight,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))

                                            if (playbackState.isPlaying && playbackState.currentSong?.id == currentOrRecentSong.id) {
                                                EqualizerBars(
                                                    isPlaying = true,
                                                    barCount = 4,
                                                    barWidth = 3.dp,
                                                    maxHeight = 16.dp,
                                                    color = IndigoLight
                                                )
                                            } else {
                                                Text(
                                                    text = "Tap to Play",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = IndigoLight
                                                )
                                            }
                                        }

                                        // Play/Pause Action Button
                                        IconButton(
                                            onClick = {
                                                if (playbackState.currentSong?.id == currentOrRecentSong.id) {
                                                    viewModel.togglePlayPause()
                                                } else {
                                                    viewModel.playSong(currentOrRecentSong, allSongs)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .shadow(12.dp, CircleShape, spotColor = IndigoPrimary.copy(alpha = 0.5f))
                                                .clip(CircleShape)
                                                .background(IndigoPrimary)
                                                .testTag("hero_play_pause_button")
                                        ) {
                                            val isPlayingThis = playbackState.isPlaying && playbackState.currentSong?.id == currentOrRecentSong.id
                                            Icon(
                                                imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlayingThis) "Pause" else "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Play Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Shuffle All Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.playAll(allSongs, shuffle = true) }
                                    .testTag("quick_play_shuffle_all"),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            tint = IndigoLight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Shuffle All",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${allSongs.size} tracks",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Play Favorites Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (favoriteSongs.isNotEmpty()) {
                                            viewModel.playAll(favoriteSongs, shuffle = false)
                                        } else {
                                            viewModel.playAll(allSongs, shuffle = false)
                                        }
                                    }
                                    .testTag("quick_play_favorites"),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(PinkAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = PinkAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Favourites",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${favoriteSongs.size} loved",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recently Played Section (Horizontal list)
                if (recentlyPlayed.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recently Played",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${recentlyPlayed.size} songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(recentlyPlayed, key = { it.id }) { song ->
                                    RecentlyPlayedCard(
                                        song = song,
                                        isCurrentSong = playbackState.currentSong?.id == song.id,
                                        isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                                        onClick = { viewModel.playSong(song, recentlyPlayed) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Most Played Section
                if (mostPlayed.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Most Played",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Top Tracks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AmberAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                mostPlayed.take(5).forEachIndexed { index, song ->
                                    SongItemRow(
                                        song = song,
                                        isCurrentSong = playbackState.currentSong?.id == song.id,
                                        isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                                        onClick = { viewModel.playSong(song, mostPlayed) },
                                        onMenuClick = { viewModel.openSongMenu(song) },
                                        trailingContent = {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${song.playCount} plays",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AmberAccent
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentlyPlayedCard(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(134.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(126.dp)
                .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = if (isCurrentSong) IndigoPrimary.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(126.dp),
                error = null
            )
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
            )

            if (isCurrentSong) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    EqualizerBars(
                        isPlaying = isPlaying,
                        barCount = 4,
                        barWidth = 3.dp,
                        maxHeight = 22.dp,
                        color = IndigoLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (isCurrentSong) IndigoLight else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
