package com.yomi.mangaflow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.yomi.mangaflow.data.local.ThemePreference

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B4EFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEFF),
    onPrimaryContainer = Color(0xFF1E0060),
    secondary = Color(0xFF00BCD4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC4F0F4),
    onSecondaryContainer = Color(0xFF00363D),
    tertiary = Color(0xFF7B4BFF),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFBDFF),
    onPrimary = Color(0xFF360096),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFE8DEFF),
    secondary = Color(0xFF4FD8EB),
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFFC4F0F4),
    tertiary = Color(0xFFCBB2FF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
)

private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF5D4037),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7CCC8),
    onPrimaryContainer = Color(0xFF3E2723),
    secondary = Color(0xFF795548),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBCAAA4),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = Color(0xFF8D6E63),
    background = Color(0xFFF5E6C8),
    onBackground = Color(0xFF3E2723),
    surface = Color(0xFFF5E6C8),
    onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFE8DCC8),
    onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFF8D6E63),
)

@Composable
fun AppTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = when (themePreference) {
        ThemePreference.LIGHT -> LightColorScheme
        ThemePreference.DARK -> DarkColorScheme
        ThemePreference.SEPIA -> SepiaColorScheme
        ThemePreference.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (isDark) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                themePreference == ThemePreference.LIGHT ||
                (themePreference == ThemePreference.SYSTEM && !isDark) ||
                themePreference == ThemePreference.SEPIA
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
