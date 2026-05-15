package com.example.sceneenglish.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B55),
    onPrimary = Color.White,
    secondary = Color(0xFF3266A8),
    tertiary = Color(0xFF8A5A00),
    background = Color(0xFFFAFBF8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6EEE8),
    error = Color(0xFFB3261E)
)

@Composable
fun SceneEnglishTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
