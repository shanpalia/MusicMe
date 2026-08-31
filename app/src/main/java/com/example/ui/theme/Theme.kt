package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = BackgroundDark,
    primaryContainer = IndigoDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = CyanAccent,
    onSecondary = BackgroundDark,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = CyanGlow,
    tertiary = PinkAccent,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = IndigoDark,
    secondary = CyanAccent,
    onSecondary = SurfaceLight,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0284C7),
    tertiary = PinkAccent,
    onTertiary = SurfaceLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SurfaceBorderLight
)

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

@Composable
fun MusicMeTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
