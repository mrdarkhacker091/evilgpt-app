package com.evilgpt.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val EvilGreen = Color(0xFF00FF88)
val EvilGreenDim = Color(0xFF00CC6A)
val EvilBg = Color(0xFF0A0A0A)
val EvilSurface = Color(0xFF141414)
val EvilSurfaceVariant = Color(0xFF1E1E1E)
val EvilBorder = Color(0xFF2A2A2A)
val EvilText = Color(0xFFE8E8E8)
val EvilTextMuted = Color(0xFF9A9A9A)
val EvilError = Color(0xFFFF4466)
val UserBubble = Color(0xFF1A3D2E)
val AssistantBubble = Color(0xFF1A1A1A)

private val EvilDarkScheme = darkColorScheme(
    primary = EvilGreen,
    onPrimary = Color.Black,
    secondary = EvilGreenDim,
    background = EvilBg,
    surface = EvilSurface,
    surfaceVariant = EvilSurfaceVariant,
    onBackground = EvilText,
    onSurface = EvilText,
    onSurfaceVariant = EvilTextMuted,
    outline = EvilBorder,
    error = EvilError
)

@Composable
fun EvilGptTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EvilDarkScheme,
        content = content
    )
}
