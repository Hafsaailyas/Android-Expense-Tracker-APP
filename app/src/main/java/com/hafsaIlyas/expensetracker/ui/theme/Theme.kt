package com.hafsaIlyas.expensetracker.ui.theme
// ui/theme/Theme.kt
// ui/theme/Theme.kt

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme { SYSTEM, LIGHT, DARK }

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryLight,
    onPrimary        = OnPrimaryLight,
    surface          = SurfaceLight,
    background       = BackgroundLight,
    error            = ErrorLight,
)

private val DarkColorScheme = darkColorScheme(
    primary          = PrimaryDark,
    onPrimary        = OnPrimaryDark,
    surface          = SurfaceDark,
    background       = BackgroundDark,
    error            = ErrorDark,
)

@Composable
fun ExpenseTrackerTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = true,           // user-toggleable
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (appTheme) {
        AppTheme.LIGHT  -> false
        AppTheme.DARK   -> true
        AppTheme.SYSTEM -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context)
            else        dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else   -> LightColorScheme
    }

    // Sync status-bar appearance
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}