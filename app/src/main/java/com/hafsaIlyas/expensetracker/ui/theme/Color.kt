package com.hafsaIlyas.expensetracker.ui.theme

// ui/theme/Color.kt

import androidx.compose.ui.graphics.Color

// ─── Primary Palette — Deep Teal ───────────────────────────────────────────
val PrimaryLight        = Color(0xFF0F766E)   // Deep Teal 600
val PrimaryContainerL   = Color(0xFFCCFBF1)   // Teal 100
val OnPrimaryLight      = Color(0xFFFFFFFF)
val OnPrimaryContainerL = Color(0xFF042F2E)   // Teal 950

val PrimaryDark         = Color(0xFF5EEAD4)   // Teal 300
val PrimaryContainerD   = Color(0xFF0D4F4A)   // Teal 800
val OnPrimaryDark       = Color(0xFF002422)
val OnPrimaryContainerD = Color(0xFFCCFBF1)

// ─── Secondary Palette — Indigo Accent ─────────────────────────────────────
val SecondaryLight      = Color(0xFF4F46E5)   // Indigo 600
val SecondaryDark       = Color(0xFFA5B4FC)   // Indigo 300

// ─── Surface & Background ──────────────────────────────────────────────────
val SurfaceLight        = Color(0xFFF0FDFA)   // Teal-tinted white
val SurfaceVariantLight = Color(0xFFE6F7F5)
val BackgroundLight     = Color(0xFFFFFFFF)

val SurfaceDark         = Color(0xFF0D1B1A)   // Very dark teal
val SurfaceVariantDark  = Color(0xFF1A2E2C)
val BackgroundDark      = Color(0xFF0A1312)

// ─── Error ─────────────────────────────────────────────────────────────────
val ErrorLight          = Color(0xFFBA1A1A)
val ErrorDark           = Color(0xFFFFB4AB)

// ─── Semantic: Success ──────────────────────────────────────────────────────
val SuccessLight        = Color(0xFF2E7D32)   // Green 800
val SuccessContainerL   = Color(0xFFDCFCE7)   // Green 100
val OnSuccessLight      = Color(0xFFFFFFFF)
val OnSuccessContainerL = Color(0xFF052E16)

val SuccessDark         = Color(0xFF86EFAC)   // Green 300
val SuccessContainerD   = Color(0xFF14532D)   // Green 900
val OnSuccessDark       = Color(0xFF052E16)
val OnSuccessContainerD = Color(0xFFDCFCE7)

// ─── Semantic: Warning ──────────────────────────────────────────────────────
val WarningLight        = Color(0xFFB45309)   // Amber 700
val WarningContainerL   = Color(0xFFFEF3C7)   // Amber 100
val OnWarningLight      = Color(0xFFFFFFFF)
val OnWarningContainerL = Color(0xFF451A03)

val WarningDark         = Color(0xFFFCD34D)   // Amber 300
val WarningContainerD   = Color(0xFF78350F)   // Amber 900
val OnWarningDark       = Color(0xFF3B1A00)
val OnWarningContainerD = Color(0xFFFEF3C7)

// ─── Semantic: Info ─────────────────────────────────────────────────────────
val InfoLight           = Color(0xFF1D4ED8)   // Blue 700
val InfoContainerL      = Color(0xFFDBEAFE)   // Blue 100
val OnInfoLight         = Color(0xFFFFFFFF)
val OnInfoContainerL    = Color(0xFF1E3A5F)

val InfoDark            = Color(0xFF93C5FD)   // Blue 300
val InfoContainerD      = Color(0xFF1E3A5F)   // Blue 900
val OnInfoDark          = Color(0xFF0C1B33)
val OnInfoContainerD    = Color(0xFFDBEAFE)

// ─── Transaction Semantic Colors ───────────────────────────────────────────
val IncomeGreen         = Color(0xFF059669)   // Emerald 600 — brighter than before
val IncomeGreenDark     = Color(0xFF34D399)   // Emerald 400
val ExpenseRed          = Color(0xFFDC2626)   // Red 600
val ExpenseRedDark      = Color(0xFFF87171)   // Red 400
val NeutralGray         = Color(0xFF64748B)   // Slate 500

// ─── Gradient Stop Definitions ─────────────────────────────────────────────
// Use these in Brush.linearGradient / verticalGradient calls in composables.
//
//  Header gradient (light): PrimaryLight → PrimaryGradientEnd
val PrimaryGradientEnd  = Color(0xFF0D5954)   // Slightly deeper teal

//  Header gradient (dark):  PrimaryDark → PrimaryGradientEndDark
val PrimaryGradientEndD = Color(0xFF0D4F4A)

//  Card shimmer overlay: fully transparent → SurfaceOverlay
val SurfaceOverlay      = Color(0x0C0F766E)   // 5% teal tint

//  Splash screen gradient stops
val SplashTop           = Color(0xFF0F766E)   // PrimaryLight
val SplashBottom        = Color(0xFF042F2E)   // Dark teal