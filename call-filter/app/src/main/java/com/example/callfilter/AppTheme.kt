package com.example.callfilter

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.view.WindowCompat

/** Tema elegido por el usuario. Se guarda en preferencias locales. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Persistencia mínima del tema; no se toca ninguna preferencia del filtro. */
class ThemePreferences(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("callfilter.appearance", Context.MODE_PRIVATE)

    fun load(): ThemeMode = runCatching {
        ThemeMode.valueOf(prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name).orEmpty())
    }.getOrDefault(ThemeMode.SYSTEM)

    fun save(mode: ThemeMode) {
        prefs.edit { putString(KEY_MODE, mode.name) }
    }

    private companion object {
        const val KEY_MODE = "theme_mode"
    }
}

/**
 * Tokens propios que Material 3 no cubre: superficies y colores semánticos
 * (correcto, aviso, bloqueo).
 *
 * Estilo minimalista: neutros fríos, un único acento índigo y color solo con significado.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSunken: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val danger: Color,
    val onDanger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val borderStrong: Color
)

private val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0A0A0B),
    surface = Color(0xFF111113),
    surfaceElevated = Color(0xFF18181B),
    surfaceSunken = Color(0xFF070708),
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF0F1033),
    secondary = Color(0xFF94A3B8),
    success = Color(0xFF4CC38A),
    onSuccess = Color(0xFF04210F),
    successContainer = Color(0xFF12261B),
    onSuccessContainer = Color(0xFFBBEBD0),
    warning = Color(0xFFE0A93B),
    onWarning = Color(0xFF271B02),
    warningContainer = Color(0xFF2A2210),
    onWarningContainer = Color(0xFFF3DDA8),
    danger = Color(0xFFF0707A),
    onDanger = Color(0xFF2A0A0D),
    dangerContainer = Color(0xFF2B1517),
    onDangerContainer = Color(0xFFF8CDD0),
    textPrimary = Color(0xFFF4F4F5),
    textSecondary = Color(0xFFA1A1AA),
    border = Color(0xFF27272A),
    borderStrong = Color(0xFF3F3F46)
)

private val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF7F7F8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFF0F0F2),
    primary = Color(0xFF4338CA),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF475569),
    success = Color(0xFF15803D),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFEAF6EE),
    onSuccessContainer = Color(0xFF14532D),
    warning = Color(0xFFB45309),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFBF1E2),
    onWarningContainer = Color(0xFF78350F),
    danger = Color(0xFFB91C1C),
    onDanger = Color(0xFFFFFFFF),
    dangerContainer = Color(0xFFFBEAEA),
    onDangerContainer = Color(0xFF7F1D1D),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF6B7280),
    border = Color(0xFFE5E7EB),
    borderStrong = Color(0xFFD1D5DB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF0F1033),
    primaryContainer = Color(0xFF23264F),
    onPrimaryContainer = Color(0xFFE0E3FF),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0B1220),
    secondaryContainer = Color(0xFF1E2430),
    onSecondaryContainer = Color(0xFFD5DCE8),
    tertiary = Color(0xFFA5B4FC),
    onTertiary = Color(0xFF111436),
    tertiaryContainer = Color(0xFF26294F),
    onTertiaryContainer = Color(0xFFE0E4FF),
    error = Color(0xFFF0707A),
    onError = Color(0xFF2A0A0D),
    errorContainer = Color(0xFF2B1517),
    onErrorContainer = Color(0xFFF8CDD0),
    background = Color(0xFF0A0A0B),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF111113),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF18181B),
    onSurfaceVariant = Color(0xFFA1A1AA),
    surfaceDim = Color(0xFF0A0A0B),
    surfaceBright = Color(0xFF232326),
    surfaceContainerLowest = Color(0xFF070708),
    surfaceContainerLow = Color(0xFF111113),
    surfaceContainer = Color(0xFF141416),
    surfaceContainerHigh = Color(0xFF18181B),
    surfaceContainerHighest = Color(0xFF212124),
    outline = Color(0xFF71717A),
    outlineVariant = Color(0xFF27272A),
    inverseSurface = Color(0xFFF4F4F5),
    inverseOnSurface = Color(0xFF18181B),
    inversePrimary = Color(0xFF4338CA),
    scrim = Color(0xFF000000)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4338CA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8E9FB),
    onPrimaryContainer = Color(0xFF1B1766),
    secondary = Color(0xFF475569),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEBEDF1),
    onSecondaryContainer = Color(0xFF1F2937),
    tertiary = Color(0xFF3730A3),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE5E7FB),
    onTertiaryContainer = Color(0xFF1E1B5C),
    error = Color(0xFFB91C1C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBEAEA),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF7F7F8),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF0F0F2),
    onSurfaceVariant = Color(0xFF6B7280),
    surfaceDim = Color(0xFFEDEDF0),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFAFB),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF4F4F6),
    surfaceContainerHighest = Color(0xFFEDEDF0),
    outline = Color(0xFF9CA3AF),
    outlineVariant = Color(0xFFE5E7EB),
    inverseSurface = Color(0xFF111827),
    inverseOnSurface = Color(0xFFF4F4F5),
    inversePrimary = Color(0xFFA5ACFB),
    scrim = Color(0xFF000000)
)

/** Esquinas contenidas: se ven más serias que los radios grandes. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

/** Pesos más ligeros y menos tracking: jerarquía por tamaño y color, no por negrita. */
private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp
    )
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/** Acceso corto a los tokens de la identidad CallFilter. */
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

/** Respeta la preferencia del sistema de reducir movimiento. */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) > 0f
    }
}

@Composable
fun CallFilterAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val scheme = if (dark) DarkColors else LightColors
    val colors = if (dark) DarkAppColors else LightAppColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            view.context.findActivity()?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = AppShapes,
            typography = AppTypography,
            content = content
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}