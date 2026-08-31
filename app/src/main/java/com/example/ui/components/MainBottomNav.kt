package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.Screen

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen,
    val testTag: String
)

@Composable
fun MainBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavTabItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            screen = Screen.Home,
            testTag = "nav_tab_home"
        ),
        NavTabItem(
            title = "Songs",
            selectedIcon = Icons.Filled.LibraryMusic,
            unselectedIcon = Icons.Outlined.LibraryMusic,
            screen = Screen.Songs,
            testTag = "nav_tab_songs"
        ),
        NavTabItem(
            title = "Albums",
            selectedIcon = Icons.Filled.Album,
            unselectedIcon = Icons.Outlined.Album,
            screen = Screen.Albums,
            testTag = "nav_tab_albums"
        ),
        NavTabItem(
            title = "Artists",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            screen = Screen.Artists,
            testTag = "nav_tab_artists"
        ),
        NavTabItem(
            title = "Library",
            selectedIcon = Icons.Filled.QueueMusic,
            unselectedIcon = Icons.Outlined.QueueMusic,
            screen = Screen.Library,
            testTag = "nav_tab_library"
        )
    )

    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        items.forEach { tab ->
            val isSelected = when (currentScreen) {
                Screen.Home -> tab.screen == Screen.Home
                Screen.Songs -> tab.screen == Screen.Songs
                Screen.Albums, is Screen.AlbumDetail -> tab.screen == Screen.Albums
                Screen.Artists, is Screen.ArtistDetail -> tab.screen == Screen.Artists
                Screen.Library, is Screen.PlaylistDetail -> tab.screen == Screen.Library
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(tab.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.8.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IndigoLight,
                    selectedTextColor = IndigoLight,
                    indicatorColor = IndigoPrimary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}

