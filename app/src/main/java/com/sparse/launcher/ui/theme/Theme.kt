package com.sparse.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BG_COLORS = listOf(
    Color(0xFF0A0A0A),  // Black
    Color(0xFF1C1C1C),  // Charcoal
    Color(0xFF0E1628),  // Navy
    Color(0xFF0D1A13),  // Forest
    Color(0xFF1A1108),  // Espresso
)

val BG_COLOR_NAMES = listOf("Black", "Charcoal", "Navy", "Forest", "Espresso")

val TEXT_COLORS = listOf(
    Color(0xFFEEEEEE),  // White
    Color(0xFFA8A8A8),  // Silver
    Color(0xFFF0EAD6),  // Cream
    Color(0xFFAECFA4),  // Sage
    Color(0xFFE8BCBC),  // Rose
    Color(0xFFB4CCE0),  // Sky
    Color(0xFFC8BADC),  // Lavender
)

val TEXT_COLOR_NAMES = listOf("White", "Silver", "Cream", "Sage", "Rose", "Sky", "Lavender")

@Composable
fun SparseTheme(
    bgColor: Color = BG_COLORS[0],
    textColor: Color = TEXT_COLORS[0],
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = bgColor,
            surface = bgColor,
            onBackground = textColor,
            onSurface = textColor,
            primary = textColor,
            onPrimary = bgColor,
        ),
        content = content,
    )
}

val MaterialTheme.dimContent: Color
    @Composable get() = colorScheme.onBackground.copy(alpha = 0.45f)
