package com.example.callfilter

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF00164E),
    secondary = Color(0xFF5A5D72),
    background = Color(0xFFFBF8FF),
    surface = Color(0xFFFBF8FF),
    surfaceVariant = Color(0xFFE3E1EC),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7C4FF),
    onPrimary = Color(0xFF0A2B8F),
    primaryContainer = Color(0xFF283C9A),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC3C5DD),
    background = Color(0xFF121318),
    surface = Color(0xFF121318),
    surfaceVariant = Color(0xFF45464F),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

/** Colores "permitido / correcto", que Material no trae de serie. */
object Semantic {
    @Composable fun successContainer(): Color =
        if (isSystemInDarkTheme()) Color(0xFF0F3D1E) else Color(0xFFD7F5DD)

    @Composable fun onSuccessContainer(): Color =
        if (isSystemInDarkTheme()) Color(0xFFD7F5DD) else Color(0xFF062712)
}

@Composable
fun CallFilterAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}