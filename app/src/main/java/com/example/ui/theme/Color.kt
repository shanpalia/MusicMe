package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Sleek Interface Theme Primary Accents (Indigo / Violet)
val IndigoPrimary = Color(0xFF6366F1) // Indigo 500
val IndigoLight = Color(0xFF818CF8)   // Indigo 400
val IndigoDark = Color(0xFF4F46E5)    // Indigo 600
val IndigoGlow = Color(0xFFA5B4FC)    // Indigo 300

// Secondary Accent Colors
val CyanAccent = Color(0xFF38BDF8)    // Sky 400
val CyanGlow = Color(0xFF7DD3FC)      // Sky 300
val PurplePrimary = Color(0xFF6366F1) // Maintain backward alias
val PurpleLight = Color(0xFF818CF8)
val PurpleDark = Color(0xFF4F46E5)

val PinkAccent = Color(0xFFF43F5E)    // Rose 500
val AmberAccent = Color(0xFFF59E0B)   // Amber 500
val EmeraldAccent = Color(0xFF10B981) // Emerald 500

// Sleek Obsidian Dark Theme Palette
val BackgroundDark = Color(0xFF0B0D11)       // Deep Obsidian Canvas
val SurfaceDark = Color(0xFF12151B)          // Dark Sleek Surface
val SurfaceElevatedDark = Color(0xFF1A1C20)  // Elevated Container / Card Background
val SurfaceBorderDark = Color(0x14FFFFFF)    // border-white/8 subtle outline

val TextPrimaryDark = Color(0xFFE2E8F0)      // Slate 200 light text
val TextSecondaryDark = Color(0xFF94A3B8)    // Slate 400 secondary text
val TextTertiaryDark = Color(0xFF64748B)     // Slate 500 muted text

// Light Theme Palette
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFF1F5F9)
val SurfaceBorderLight = Color(0xFFE2E8F0)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextTertiaryLight = Color(0xFF94A3B8)

// Sleek Gradients
val BrandGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFF818CF8))
)

val GlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x406366F1), Color(0x000B0D11))
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1F222A), Color(0xFF12151B))
)

