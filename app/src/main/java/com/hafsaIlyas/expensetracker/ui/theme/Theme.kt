package com.hafsaIlyas.expensetracker.ui.theme

// ui/theme/Theme.kt

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme { SYSTEM, LIGHT, DARK }

// ─── Extended semantic color holder ────────────────────────────────────────
data class SemanticColors(
    val success: Color,
    val successContainer: Color,
    val onSuccess: Color,
    val onSuccessContainer: Color,

    val warning: Color,
    val warningContainer: Color,
    val onWarning: Color,
    val onWarningContainer: Color,

    val info: Color,
    val infoContainer: Color,
    val onInfo: Color,
    val onInfoContainer: Color,

    val income: Color,
    val expense: Color,
    val neutral: Color,

    val primaryGradientStart: Color,
    val primaryGradientEnd: Color,
)

val LocalSemanticColors = staticCompositionLocalOf {
    SemanticColors(
        success             = SuccessLight,
        successContainer    = SuccessContainerL,
        onSuccess           = OnSuccessLight,
        onSuccessContainer  = OnSuccessContainerL,
        warning             = WarningLight,
        warningContainer    = WarningContainerL,
        onWarning           = OnWarningLight,
        onWarningContainer  = OnWarningContainerL,
        info                = InfoLight,
        infoContainer       = InfoContainerL,
        onInfo              = OnInfoLight,
        onInfoContainer     = OnInfoContainerL,
        income              = IncomeGreen,
        expense             = ExpenseRed,
        neutral             = NeutralGray,
        primaryGradientStart = PrimaryLight,
        primaryGradientEnd   = PrimaryGradientEnd,
    )
}

// ─── Convenience accessor ──────────────────────────────────────────────────
val MaterialTheme.semanticColors: SemanticColors
    @Composable get() = LocalSemanticColors.current

// ─── Material color schemes ────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary             = PrimaryLight,
    onPrimary           = OnPrimaryLight,
    primaryContainer    = PrimaryContainerL,
    onPrimaryContainer  = OnPrimaryContainerL,
    secondary           = SecondaryLight,
    surface             = SurfaceLight,
    surfaceVariant      = SurfaceVariantLight,
    background          = BackgroundLight,
    error               = ErrorLight,
)

private val DarkColorScheme = darkColorScheme(
    primary             = PrimaryDark,
    onPrimary           = OnPrimaryDark,
    primaryContainer    = PrimaryContainerD,
    onPrimaryContainer  = OnPrimaryContainerD,
    secondary           = SecondaryDark,
    surface             = SurfaceDark,
    surfaceVariant      = SurfaceVariantDark,
    background          = BackgroundDark,
    error               = ErrorDark,
)

// ─── Theme composable ──────────────────────────────────────────────────────
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
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (isDark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        isDark -> DarkColorScheme
        else   -> LightColorScheme
    }

    val semanticColors = if (isDark) {
        SemanticColors(
            success             = SuccessDark,
            successContainer    = SuccessContainerD,
            onSuccess           = OnSuccessDark,
            onSuccessContainer  = OnSuccessContainerD,
            warning             = WarningDark,
            warningContainer    = WarningContainerD,
            onWarning           = OnWarningDark,
            onWarningContainer  = OnWarningContainerD,
            info                = InfoDark,
            infoContainer       = InfoContainerD,
            onInfo              = OnInfoDark,
            onInfoContainer     = OnInfoContainerD,
            income              = IncomeGreenDark,
            expense             = ExpenseRedDark,
            neutral             = NeutralGray,
            primaryGradientStart = PrimaryDark,
            primaryGradientEnd   = PrimaryGradientEndD,
        )
    } else {
        SemanticColors(
            success             = SuccessLight,
            successContainer    = SuccessContainerL,
            onSuccess           = OnSuccessLight,
            onSuccessContainer  = OnSuccessContainerL,
            warning             = WarningLight,
            warningContainer    = WarningContainerL,
            onWarning           = OnWarningLight,
            onWarningContainer  = OnWarningContainerL,
            info                = InfoLight,
            infoContainer       = InfoContainerL,
            onInfo              = OnInfoLight,
            onInfoContainer     = OnInfoContainerL,
            income              = IncomeGreen,
            expense             = ExpenseRed,
            neutral             = NeutralGray,
            primaryGradientStart = PrimaryLight,
            primaryGradientEnd   = PrimaryGradientEnd,
        )
    }

    // Sync status-bar icon tint with brightness
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            shapes      = AppShapes,
            content     = content
        )
    }
}