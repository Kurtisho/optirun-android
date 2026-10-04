package com.kurtis.optirun.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = RunPink, onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE1D3), onPrimaryContainer = Color(0xFF3A1000),
    secondary = Teal, onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3ECEC), onSecondaryContainer = Color(0xFF0B3033),
    background = Color(0xFFF8F7F5), onBackground = Color(0xFF1B1C1E),
    surface = Color(0xFFF8F7F5), onSurface = Color(0xFF1B1C1E),
    surfaceVariant = Color(0xFFECEAE6), onSurfaceVariant = Color(0xFF5C5F63),
    surfaceContainerHighest = Color.White,   // card color
    outline = Color(0xFFD5D2CC),
)

private val DarkColors = darkColorScheme(
    primary = RunPinkLight, onPrimary = Color(0xFF3A1000),
    primaryContainer = Color(0xFF5C2200), onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = TealLight, onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF1F4E52), onSecondaryContainer = Color(0xFFD3ECEC),
    background = Color(0xFF111316), onBackground = Color(0xFFE3E2E0),
    surface = Color(0xFF111316), onSurface = Color(0xFFE3E2E0),
    surfaceVariant = Color(0xFF2A2D31), onSurfaceVariant = Color(0xFFB9BCC0),
    surfaceContainerHighest = Color(0xFF1E2125),
    outline = Color(0xFF3A3E43),
)

@Composable
fun OptiRunTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}