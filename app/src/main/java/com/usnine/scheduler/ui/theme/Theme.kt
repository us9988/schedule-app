package com.usnine.scheduler.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    background = BgLight,
    surface = White,
    onSurface = Color.Black,
    primary = PrimaryLight,
    onPrimary = White,
    secondary = SecondaryLight,
    onSecondary = Color.DarkGray,
    onTertiary = Color.Gray



    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val DarkColorScheme = darkColorScheme(
    background = BgDark,
    surface = BgDark,
    onSurface = White,
    primary = PrimaryDark,
    onPrimary = White,
    secondary = SecondaryDark,
    onSecondary = White,
    onTertiary = Color.Gray
)

@Composable
fun SchedulerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
