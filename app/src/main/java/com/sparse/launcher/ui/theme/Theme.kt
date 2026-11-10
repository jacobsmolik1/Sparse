package com.sparse.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0A0A0A)
val Surface = Color(0xFF141414)
val OnBackground = Color(0xFFEEEEEE)
val OnBackgroundDim = Color(0xFF888888)
val Accent = Color(0xFFFFFFFF)

private val ColorScheme = darkColorScheme(
    background = Background,
    surface = Surface,
    onBackground = OnBackground,
    onSurface = OnBackground,
    primary = Accent,
    onPrimary = Background,
)

@Composable
fun SparseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content,
    )
}
