package com.quickrec.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = PureBlack,
    secondary = NeonGreenDim,
    onSecondary = PureBlack,
    background = PureBlack,
    onBackground = NeonGreen,
    surface = DarkSurface,
    onSurface = NeonGreen,
    surfaceVariant = DarkCard,
    onSurfaceVariant = NeonGreenDim,
    error = ErrorRed,
    onError = PureBlack,
    outline = DarkBorder
)

@Composable
fun QuickRecTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
