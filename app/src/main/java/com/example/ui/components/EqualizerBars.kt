package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleLight

@Composable
fun EqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    barWidth: Dp = 3.dp,
    maxHeight: Dp = 18.dp,
    color: Color = CyanAccent
) {
    val transition = rememberInfiniteTransition(label = "equalizer")

    val heights = (0 until barCount).map { i ->
        val duration = 400 + (i * 180) % 500
        val anim by transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$i"
        )
        if (isPlaying) anim else 0.3f
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(maxHeight * h)
                    .clip(RoundedCornerShape(barWidth / 2))
                    .background(color)
            )
        }
    }
}

@Composable
fun LargeSoundWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    maxHeight: Dp = 48.dp
) {
    val transition = rememberInfiniteTransition(label = "large_wave")

    val animValues = (0 until barCount).map { i ->
        val delay = (i * 70) % 600
        val duration = 450 + (i * 90) % 400
        val anim by transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, delayMillis = delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_$i"
        )
        if (isPlaying) anim else 0.2f
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animValues.forEachIndexed { index, scale ->
            val centerWeight = 1.0f - Math.abs(index - (barCount / 2f)) / (barCount / 2f) * 0.4f
            val h = maxHeight * (scale * centerWeight).coerceIn(0.12f, 1.0f)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CyanAccent, PurpleLight)
                        )
                    )
            )
        }
    }
}
