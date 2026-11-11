package com.mekanik.wiring.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EarthGreen,
    onPrimary = OledBlack,
    primaryContainer = NeutralSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = SignalYellow,
    onSecondary = OledBlack,
    secondaryContainer = NeutralSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = NeutralSurface,
    onSurface = TextPrimary,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NeutralBorder,
    outlineVariant = NeutralBorderLight
)

@Composable
fun MekanikWiringTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
