package com.sobhaty.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PropheticGreenPrimary,
    secondary = PropheticGreenSecondary,
    tertiary = PropheticGreenTertiary,
    background = DarkBackground,
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PropheticGreenPrimary,
    secondary = PropheticGreenSecondary,
    tertiary = PropheticGreenTertiary,
    background = LightBackground,
    surface = SurfaceGreen,
    onPrimary = Color.White,
    onBackground = Color(0xFF1B5E20),
    onSurface = OnSurfaceGreen
)

@Composable
fun SobhatyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
