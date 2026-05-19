package com.blankweather.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    background = Color(0xFFF6F6F6),
    surface = Color(0xFFF6F6F6),
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF111111),
    onSurfaceVariant = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFFDADADA),
    primary = Color(0xFF111111),
    onPrimary = Color(0xFFF6F6F6),
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF0E0E10),
    surface = Color(0xFF0E0E10),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF2A2A2A),
    primary = Color(0xFFEDEDED),
    onPrimary = Color(0xFF0E0E10),
)

@Composable
fun BlankWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    // Touch context to avoid unused warning on older lints; mainly here so future
    // dynamic-color additions can use it.
    LocalContext.current
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
