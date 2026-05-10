package com.hafsaIlyas.expensetracker.ui.theme

// ui/theme/SpendSenseColors.kt
// Central color palette matching the HTML :root variables exactly.
// Use these throughout the app for consistency.
//
// HTML source:
//   --primary:      #1A5F7A
//   --secondary:    #2C7865
//   --gold:         #F9D56E
//   --bg:           #F0F4F8
//   --surface:      #FFFFFF
//   --dark-bg:      #0A1112
//   --dark-surface: #1A1F2E
//   --text-dark:    #1A1F2E
//   --text-muted:   #888888
//   --text-light:   #AAAAAA
//   --danger:       #C62828
//   --warning:      #E65100
//   --success:      #2E7D32
//   --border:       rgba(26,95,122,0.1)

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Raw tokens ────────────────────────────────────────────────────────────────

object SpendSenseTokens {
    val Primary       = Color(0xFF1A5F7A)
    val Secondary     = Color(0xFF2C7865)
    val Gold          = Color(0xFFF9D56E)

    // Backgrounds
    val BgLight       = Color(0xFFF0F4F8)
    val SurfaceLight  = Color(0xFFFFFFFF)
    val BgDark        = Color(0xFF0A1112)
    val SurfaceDark   = Color(0xFF1A1F2E)

    // Text
    val TextDark      = Color(0xFF1A1F2E)
    val TextMuted     = Color(0xFF888888)
    val TextLight     = Color(0xFFAAAAAA)

    // Semantic
    val Danger        = Color(0xFFC62828)
    val Warning       = Color(0xFFE65100)
    val Success       = Color(0xFF2E7D32)

    // Border
    val Border        = Color(0xFF1A5F7A).copy(alpha = 0.10f)
}

// ── Material3 light scheme ────────────────────────────────────────────────────

val SpendSenseLightColorScheme = lightColorScheme(
    primary                = SpendSenseTokens.Primary,
    onPrimary              = Color.White,
    primaryContainer       = Color(0xFFB3D8E8),
    onPrimaryContainer     = Color(0xFF001F29),

    secondary              = SpendSenseTokens.Secondary,
    onSecondary            = Color.White,
    secondaryContainer     = Color(0xFFB2DFDB),
    onSecondaryContainer   = Color(0xFF002019),

    tertiary               = SpendSenseTokens.Gold,
    onTertiary             = SpendSenseTokens.TextDark,

    background             = SpendSenseTokens.BgLight,
    onBackground           = SpendSenseTokens.TextDark,

    surface                = SpendSenseTokens.SurfaceLight,
    onSurface              = SpendSenseTokens.TextDark,
    surfaceVariant         = Color(0xFFE0EBF0),
    onSurfaceVariant       = SpendSenseTokens.TextMuted,
    surfaceContainerLow    = Color(0xFFF8FAFB),
    surfaceContainerHigh   = Color(0xFFE8EEF2),

    error                  = SpendSenseTokens.Danger,
    onError                = Color.White,
    errorContainer         = Color(0xFFFFDAD6),
    onErrorContainer       = Color(0xFF410002),

    outline                = SpendSenseTokens.Border,
    outlineVariant         = Color(0xFFD0DCE2),
)

// ── Material3 dark scheme ─────────────────────────────────────────────────────

val SpendSenseDarkColorScheme = darkColorScheme(
    primary                = Color(0xFF4DD8DF),   // matches splash teal accent in dark
    onPrimary              = Color(0xFF003740),
    primaryContainer       = SpendSenseTokens.Primary,
    onPrimaryContainer     = Color(0xFFB3D8E8),

    secondary              = Color(0xFF80CBC4),
    onSecondary            = Color(0xFF00332E),
    secondaryContainer     = SpendSenseTokens.Secondary,
    onSecondaryContainer   = Color(0xFFB2DFDB),

    tertiary               = SpendSenseTokens.Gold,
    onTertiary             = SpendSenseTokens.TextDark,

    background             = SpendSenseTokens.BgDark,      // #0A1112
    onBackground           = Color(0xFFDCE4E5),

    surface                = SpendSenseTokens.SurfaceDark,  // #1A1F2E
    onSurface              = Color(0xFFDCE4E5),
    surfaceVariant         = Color(0xFF263040),
    onSurfaceVariant       = SpendSenseTokens.TextLight,
    surfaceContainerLow    = Color(0xFF151B2A),
    surfaceContainerHigh   = Color(0xFF1E2535),

    error                  = Color(0xFFFFB4AB),
    onError                = Color(0xFF690005),
    errorContainer         = Color(0xFF93000A),
    onErrorContainer       = Color(0xFFFFDAD6),

    outline                = Color(0xFF1A5F7A).copy(alpha = 0.30f),
    outlineVariant         = Color(0xFF2A3A4A),
)