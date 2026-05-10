package com.hafsaIlyas.expensetracker.ui.theme

// ui/theme/Theme.kt
// Full Material 3 color scheme wiring — light + dark
// Both schemes derived from the SpendSense HTML design system

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme { SYSTEM, LIGHT, DARK }

// ── Light color scheme ────────────────────────────────────────────────────────
// Mirrors HTML dashboard / expense list / settings / onboarding screens
private val LightColorScheme = lightColorScheme(
    primary                  = PrimaryLight,             // #1A5F7A — teal
    onPrimary                = OnPrimaryLight,            // #FFFFFF
    primaryContainer         = PrimaryContainerLight,     // light teal tint
    onPrimaryContainer       = OnPrimaryContainerLight,

    secondary                = SecondaryLight,            // #2C7865 — emerald
    onSecondary              = OnSecondaryLight,
    secondaryContainer       = SecondaryContainerLight,
    onSecondaryContainer     = OnSecondaryContainerLight,

    tertiary                 = TertiaryLight,             // #2C3A6E — AI navy
    onTertiary               = OnTertiaryLight,
    tertiaryContainer        = TertiaryContainerLight,
    onTertiaryContainer      = OnTertiaryContainerLight,

    surface                  = SurfaceLight,              // #FFFFFF — cards
    surfaceVariant           = SurfaceVariantLight,       // #EDF2F7 — search/picker bg
    onSurface                = OnSurfaceLight,            // #1A1F2E — dark body text
    onSurfaceVariant         = OnSurfaceVariantLight,     // #888888 — muted text

    background               = BackgroundLight,           // #F0F4F8 — screen bg
    onBackground             = OnSurfaceLight,

    outline                  = OutlineLight,              // #DDDDDD — chip/field borders
    outlineVariant           = OutlineVariantLight,       // #E8E8E8 — dividers

    error                    = ErrorLight,                // #C62828 — expense red
    errorContainer           = ErrorContainerLight,

    surfaceContainerLowest   = SurfaceContainerLowest,
    surfaceContainerLow      = SurfaceContainerLow_L,
    surfaceContainer         = SurfaceContainer_L,
    surfaceContainerHigh     = SurfaceContainerHigh_L,
    surfaceContainerHighest  = SurfaceContainerHighest,
)

// ── Dark color scheme ─────────────────────────────────────────────────────────
// Mirrors HTML splash screen palette: #0A1112 → #0F2030 → #1A5F7A
private val DarkColorScheme = darkColorScheme(
    primary                  = PrimaryDark,              // #4DAFCF — bright teal on dark
    onPrimary                = OnPrimaryDark,
    primaryContainer         = PrimaryContainerDark,     // deep teal container
    onPrimaryContainer       = OnPrimaryContainerDark,

    secondary                = SecondaryDark,            // #4DB89A — bright emerald on dark
    onSecondary              = OnSecondaryDark,
    secondaryContainer       = SecondaryContainerDark,
    onSecondaryContainer     = OnSecondaryContainerDark,

    tertiary                 = TertiaryDark,             // #9AAEFF — soft indigo
    onTertiary               = OnTertiaryDark,
    tertiaryContainer        = TertiaryContainerDark,
    onTertiaryContainer      = OnTertiaryContainerDark,

    surface                  = SurfaceDark,              // #0C1415 — dark card
    surfaceVariant           = SurfaceVariantDark,       // #1A2728 — subtle card on dark
    onSurface                = OnSurfaceDark,            // #DCE8EA — light text on dark
    onSurfaceVariant         = OnSurfaceVariantDark,     // #8FAAAD — muted text on dark

    background               = BackgroundDark,           // #0A1112 — HTML splash top bg
    onBackground             = OnSurfaceDark,

    outline                  = OutlineDark,              // #2A3540 — HTML phone dark border
    outlineVariant           = OutlineVariantDark,       // #1E2C2E — subtle dividers

    error                    = ErrorDark,                // #FFB4AB
    errorContainer           = ErrorContainerDark,       // #93000A

    surfaceContainerLowest   = SurfaceContainerLowest_D, // #080F10
    surfaceContainerLow      = SurfaceContainerLow_D,    // #0F2030 — HTML splash mid bg
    surfaceContainer         = SurfaceContainer_D,       // #182223
    surfaceContainerHigh     = SurfaceContainerHigh_D,   // #1A2728
    surfaceContainerHighest  = SurfaceContainerHighest_D,// #222D2E
)

@Composable
fun ExpenseTrackerTheme(
    appTheme     : AppTheme = AppTheme.SYSTEM,
    dynamicColor : Boolean  = true,
    content      : @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (appTheme) {
        AppTheme.LIGHT  -> false
        AppTheme.DARK   -> true
        AppTheme.SYSTEM -> systemDark
    }

    val colorScheme = when {
        // Dynamic color (Android 12+) — follows system wallpaper
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else   -> LightColorScheme
    }

    // Sync status-bar icon tint with current theme
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
        shapes      = AppShapes,
        content     = content
    )
}