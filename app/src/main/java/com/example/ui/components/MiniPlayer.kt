package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.PlaybackState
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary

@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = playbackState.currentSong
    val isVisible = song != null

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (song != null) {
            val context = LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = IndigoPrimary.copy(alpha = 0.3f))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onExpand)
                        .testTag("mini_player_container"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Album art with subtle overlay
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IndigoPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(song.albumArtUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Mini album art",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(46.dp),
                                    error = null
                                )
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = IndigoLight.copy(alpha = 0.5f),
                                    modifier = Modifier.size(22.dp)
                                )

                                if (playbackState.isPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.Black.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        EqualizerBars(
                                            isPlaying = true,
                                            barCount = 3,
                                            barWidth = 2.5.dp,
                                            maxHeight = 14.dp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Track details
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (playbackState.isPlaying) "NOW PLAYING • ${song.artist}" else song.artist,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 0.6.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (playbackState.isPlaying) IndigoLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Play/Pause button
                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("mini_player_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Next button
                            IconButton(
                                onClick = onPlayNext,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("mini_player_next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Song",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Sleek glowing progress indicator at container bottom
                        LinearProgressIndicator(
                            progress = { playbackState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp),
                            color = IndigoLight,
                            trackColor = Color.White.copy(alpha = 0.06f),
                        )
                    }
                }
            }
        }
    }
}
