package com.sparse.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

val BG_COLORS = listOf(
    Color(0xFF000000),  // AMOLED
    Color(0xFF1C1C1C),  // Charcoal
    Color(0xFF0E1628),  // Navy
    Color(0xFF0D1A13),  // Forest
    Color(0xFF1A1108),  // Espresso
    Color(0xFF2A2A2A),  // Ash
    Color(0xFF1A0D24),  // Plum
    Color(0xFF0D1A1A),  // Teal
)

val BG_COLOR_NAMES = listOf("AMOLED", "Charcoal", "Navy", "Forest", "Espresso", "Ash", "Plum", "Teal")

val TEXT_COLORS = listOf(
    Color(0xFFEEEEEE),  // White
    Color(0xFFA8A8A8),  // Silver
    Color(0xFFF0EAD6),  // Cream
    Color(0xFFAECFA4),  // Sage
    Color(0xFFE8BCBC),  // Rose
    Color(0xFFB4CCE0),  // Sky
    Color(0xFFC8BADC),  // Lavender
    Color(0xFF9FD4C5),  // Mint
    Color(0xFFF0C4B0),  // Peach
    Color(0xFFE0D4A0),  // Gold
    Color(0xFFFFF5E0),  // Warm
    Color(0xFFF4B8A8),  // Coral
)

val TEXT_COLOR_NAMES = listOf(
    "White", "Silver", "Cream", "Sage", "Rose", "Sky", "Lavender",
    "Mint", "Peach", "Gold", "Warm", "Coral"
)

val LocalSparseFont = compositionLocalOf<FontFamily> { FontFamily.SansSerif }

@Composable
fun SparseTheme(
    bgColor: Color = BG_COLORS[0],
    textColor: Color = TEXT_COLORS[0],
    fontFamily: FontFamily = FontFamily.SansSerif,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSparseFont provides fontFamily) {
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
}

val MaterialTheme.dimContent: Color
    @Composable get() = colorScheme.onBackground.copy(alpha = 0.45f)
