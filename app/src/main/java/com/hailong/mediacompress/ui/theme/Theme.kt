package com.hailong.mediacompress.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryBlue = Color(0xFF1877F2)
val BackgroundLight = Color(0xFFF8F9FA)
val TextDark = Color(0xFF1A1A1A)
val TextGrey = Color(0xFF8E8E93)
val SuccessGreen = Color(0xFF4CAF50)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    secondary = Color(0xFFE3F2FD),
    onSecondary = PrimaryBlue
)

@Composable
fun MediaCompressTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        // 暂时只支持亮色主题以匹配设计稿
        LightColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
