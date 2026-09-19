package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val MusicNavy = Color(0xFF07152D)
private val Cyan = Color(0xFF08CFF0)
private val Blue = Color(0xFF2563EB)
private val Purple = Color(0xFF7C3AED)
private val Pink = Color(0xFFF000D8)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MusicMeLogo(modifier = Modifier.size(260.dp))

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "MusicMe",
                style = TextStyle(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2).sp,
                    brush = Brush.linearGradient(
                        colors = listOf(MusicNavy, MusicNavy, Cyan, Purple, Pink)
                    )
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "P L A Y   Y O U R   M O O D",
                color = MusicNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "By PaliaAPK HUB",
                style = TextStyle(
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(Cyan, Blue, Purple, Pink)
                    )
                )
            )

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .size(width = 250.dp, height = 7.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Cyan, Blue, Purple, Pink)
                        ),
                        shape = CircleShape
                    )
            )
        }

        Text(
            text = "Developer by ShanPalia",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 34.dp),
            color = MusicNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        BottomWaves(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MusicMeLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.46f

        drawCircle(
            brush = Brush.sweepGradient(listOf(Cyan, Blue, Purple, Pink, Cyan)),
            radius = radius,
            center = center,
            style = Stroke(width = size.minDimension * 0.035f)
        )

        val waveColor = Brush.horizontalGradient(listOf(Cyan, Blue, Purple, Pink))
        val barXs = listOf(
            -0.34f, -0.27f, -0.20f, -0.13f,
             0.18f,  0.25f,  0.32f,  0.39f
        )
        val heights = listOf(0.10f, 0.18f, 0.29f, 0.38f, 0.31f, 0.23f, 0.15f, 0.09f)

        barXs.zip(heights).forEach { (x, h) ->
            val xPos = center.x + size.width * x
            val half = size.height * h
            drawLine(
                brush = waveColor,
                start = Offset(xPos, center.y - half),
                end = Offset(xPos, center.y + half),
                strokeWidth = size.minDimension * 0.028f,
                cap = StrokeCap.Round
            )
        }

        // Music note stem
        val noteX = center.x + size.width * 0.02f
        val noteTop = center.y - size.height * 0.29f
        val noteBottom = center.y + size.height * 0.22f

        drawLine(
            brush = waveColor,
            start = Offset(noteX, noteTop),
            end = Offset(noteX, noteBottom),
            strokeWidth = size.minDimension * 0.055f,
            cap = StrokeCap.Round
        )

        drawLine(
            brush = waveColor,
            start = Offset(noteX, noteTop),
            end = Offset(noteX + size.width * 0.16f, noteTop + size.height * 0.08f),
            strokeWidth = size.minDimension * 0.055f,
            cap = StrokeCap.Round
        )

        drawOval(
            brush = waveColor,
            topLeft = Offset(
                noteX - size.width * 0.075f,
                noteBottom - size.height * 0.02f
            ),
            size = androidx.compose.ui.geometry.Size(
                size.width * 0.15f,
                size.height * 0.10f
            )
        )

        // Play button inside the note head.
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.105f,
            center = Offset(noteX - size.width * 0.035f, noteBottom + size.height * 0.03f)
        )

        val playCenter = Offset(noteX - size.width * 0.035f, noteBottom + size.height * 0.03f)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(playCenter.x - size.width * 0.025f, playCenter.y - size.height * 0.045f)
            lineTo(playCenter.x + size.width * 0.055f, playCenter.y)
            lineTo(playCenter.x - size.width * 0.025f, playCenter.y + size.height * 0.045f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(listOf(Cyan, Purple, Pink))
        )
    }
}

@Composable
private fun BottomWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val waveBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE8F8FF),
                Color(0xFFF2EFFF),
                Color(0xFFFFEAFB)
            )
        )
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, size.height * 0.62f)
            cubicTo(
                size.width * 0.15f, size.height * 0.35f,
                size.width * 0.27f, size.height * 0.92f,
                size.width * 0.43f, size.height * 0.62f
            )
            cubicTo(
                size.width * 0.58f, size.height * 0.34f,
                size.width * 0.70f, size.height * 0.90f,
                size.width * 0.84f, size.height * 0.60f
            )
            cubicTo(
                size.width * 0.92f, size.height * 0.45f,
                size.width * 0.97f, size.height * 0.54f,
                size.width, size.height * 0.50f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, waveBrush)
    }
}
