package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ThemeLightPurple,
    secondary = ThemePurple,
    tertiary = HydrationBlue,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkBg,
    onSecondary = OnDarkText,
    onTertiary = OnDarkText,
    onBackground = OnDarkText,
    onSurface = OnDarkText
)

private val LightColorScheme = lightColorScheme(
    primary = ThemePurple,
    secondary = ThemeLightPurple,
    tertiary = HydrationBlue,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = OnLightText,
    onTertiary = OnLightText,
    onBackground = OnLightText,
    onSurface = OnLightText
)

@Composable
fun MyApplicationTheme(
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
