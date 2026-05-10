package com.hafsaIlyas.expensetracker.ui.theme

// ui/theme/Color.kt
// Color palette derived from the SpendSense HTML design system
// Light mode  → HTML dashboard / expense / settings screens
// Dark mode   → HTML splash screen + darkened surface variants

import androidx.compose.ui.graphics.Color

// ── Light Mode Palette ────────────────────────────────────────────────────────
// Primary teal — HTML #1A5F7A (logo, nav active, buttons, borders, labels)
val PrimaryLight            = Color(0xFF1A5F7A)
val OnPrimaryLight          = Color(0xFFFFFFFF)
val PrimaryContainerLight   = Color(0xFFCDE8F2)   // light teal tint for containers
val OnPrimaryContainerLight = Color(0xFF00202D)   // near-black teal for text on container

// Secondary emerald — HTML #2C7865 (hero card gradient end, budget fill end)
val SecondaryLight            = Color(0xFF2C7865)
val OnSecondaryLight          = Color(0xFFFFFFFF)
val SecondaryContainerLight   = Color(0xFFB8EAD9)
val OnSecondaryContainerLight = Color(0xFF002920)

// Tertiary — HTML AI banner deep navy #2C3A6E (AI gradient second stop)
val TertiaryLight           = Color(0xFF2C3A6E)
val OnTertiaryLight         = Color(0xFFFFFFFF)
val TertiaryContainerLight  = Color(0xFFDDE2FF)
val OnTertiaryContainerLight= Color(0xFF060F3E)

// Surfaces — HTML light bg #F0F4F8, cards #FFFFFF
val SurfaceLight            = Color(0xFFFFFFFF)   // card background
val SurfaceVariantLight     = Color(0xFFEDF2F7)   // search bar, segmented picker bg
val BackgroundLight         = Color(0xFFF0F4F8)   // HTML .dash-bg / .list-bg etc.
val OnSurfaceLight          = Color(0xFF1A1F2E)   // HTML dark text #1A1F2E
val OnSurfaceVariantLight   = Color(0xFF888888)   // HTML muted text #888
val OutlineLight            = Color(0xFFDDDDDD)   // HTML chip/field borders #ddd
val OutlineVariantLight     = Color(0xFFE8E8E8)   // HTML divider lines #e8e8e8

// Error — HTML expense red #C62828
val ErrorLight              = Color(0xFFC62828)
val ErrorContainerLight     = Color(0xFFFFDAD6)

// Surface container scale (cards, bottom nav, footer)
val SurfaceContainerLowest  = Color(0xFFFFFFFF)   // pure white cards
val SurfaceContainerLow_L   = Color(0xFFF0F4F8)   // HTML background
val SurfaceContainer_L      = Color(0xFFEAEFF3)
val SurfaceContainerHigh_L  = Color(0xFFE4EAF0)
val SurfaceContainerHighest = Color(0xFFDDE4EB)

// ── Dark Mode Palette ─────────────────────────────────────────────────────────
// Derived from HTML splash: #0A1112 (bg top) → #0F2030 (bg mid) → #1A5F7A (bg bottom)
// Primary on dark — brighter teal so it's readable on dark bg
val PrimaryDark             = Color(0xFF4DAFCF)   // lighter teal readable on #0A1112
val OnPrimaryDark           = Color(0xFF00202D)
val PrimaryContainerDark    = Color(0xFF00455C)   // deep teal container
val OnPrimaryContainerDark  = Color(0xFFCDE8F2)

// Secondary on dark — brighter emerald
val SecondaryDark           = Color(0xFF4DB89A)
val OnSecondaryDark         = Color(0xFF002920)
val SecondaryContainerDark  = Color(0xFF1A4D3E)
val OnSecondaryContainerDark= Color(0xFFB8EAD9)

// Tertiary on dark — softened navy/indigo (AI accent)
val TertiaryDark            = Color(0xFF9AAEFF)
val OnTertiaryDark          = Color(0xFF060F3E)
val TertiaryContainerDark   = Color(0xFF1A2456)
val OnTertiaryContainerDark = Color(0xFFDDE2FF)

// Dark surfaces — HTML splash bg scale
val SurfaceDark             = Color(0xFF0C1415)   // slightly above pure black
val SurfaceVariantDark      = Color(0xFF1A2728)   // subtle card on dark
val BackgroundDark          = Color(0xFF0A1112)   // HTML splash top: #0A1112
val OnSurfaceDark           = Color(0xFFDCE8EA)   // near-white for text on dark
val OnSurfaceVariantDark    = Color(0xFF8FAAAD)   // muted text on dark
val OutlineDark             = Color(0xFF2A3540)   // HTML phone dark border #2a3540
val OutlineVariantDark      = Color(0xFF1E2C2E)   // subtle dividers on dark

// Error on dark
val ErrorDark               = Color(0xFFFFB4AB)
val ErrorContainerDark      = Color(0xFF93000A)

// Dark surface container scale
val SurfaceContainerLowest_D  = Color(0xFF080F10)
val SurfaceContainerLow_D     = Color(0xFF0F2030)   // HTML splash bg mid: #0F2030
val SurfaceContainer_D        = Color(0xFF182223)
val SurfaceContainerHigh_D    = Color(0xFF1A2728)
val SurfaceContainerHighest_D = Color(0xFF222D2E)

// ── Gold accent ───────────────────────────────────────────────────────────────
// HTML #F9D56E — logo border, nav FAB icon, AI orb, pill text, loading dots
// Not a Material3 role; referenced directly by splash + any screen using gold
val GoldAccent              = Color(0xFFF9D56E)
val GoldAccentDim           = Color(0xFFF9D56E).copy(alpha = 0.40f)  // dimmed dots

// ── Semantic status ───────────────────────────────────────────────────────────
val IncomeGreen             = Color(0xFF2C7865)   // HTML secondary / budget fill
val ExpenseRed              = Color(0xFFC62828)   // HTML .txn-amt / .exp-amt
val WarnAmber               = Color(0xFFE65100)   // HTML warn/about heart #E65100
val NeutralGray             = Color(0xFF607D8B)

// ── Chart / category colors ───────────────────────────────────────────────────
// Matches HTML donut chart segments exactly
val ChartTeal               = Color(0xFF1A5F7A)   // Food  — HTML primary
val ChartEmerald            = Color(0xFF2C7865)   // Transport — HTML secondary
val ChartGold               = Color(0xFFF9D56E)   // Shopping — HTML gold accent
val ChartCoral              = Color(0xFFE76F51)   // Health — HTML chart coral
val ChartSageGreen          = Color(0xFF6B9F7A)   // Other — HTML chart sage

// Extended palette for more categories
val ChartIndigo             = Color(0xFF2C3A6E)   // AI navy
val ChartAmber              = Color(0xFFFFB347)
val ChartBlue               = Color(0xFF2196F3)
val ChartPink               = Color(0xFFF06292)
val ChartLime               = Color(0xFF8BC34A)

// Chart color list for programmatic use (first 5 match HTML donut exactly)
val ChartColors = listOf(
    ChartTeal, ChartEmerald, ChartGold, ChartCoral, ChartSageGreen,
    ChartIndigo, ChartAmber, ChartBlue, ChartPink, ChartLime
)

// ── Gradient presets — taken directly from HTML ───────────────────────────────
// HTML hero card: linear-gradient(135deg, #1A5F7A 0%, #2C7865 100%)
val GradientHero            = listOf(Color(0xFF1A5F7A), Color(0xFF2C7865))

// HTML splash bg: linear-gradient(160deg, #0A1112 0%, #0F2030 60%, #1A5F7A 100%)
val GradientSplash          = listOf(Color(0xFF0A1112), Color(0xFF0F2030), Color(0xFF1A5F7A))

// HTML AI banner: linear-gradient(135deg, #1A5F7A 0%, #2C3A6E 100%)
val GradientAI              = listOf(Color(0xFF1A5F7A), Color(0xFF2C3A6E))

// HTML about strip: linear-gradient(90deg, #1A5F7A, #2C7865, #F9D56E)
val GradientAbout           = listOf(Color(0xFF1A5F7A), Color(0xFF2C7865), Color(0xFFF9D56E))

// HTML onboarding CTA last step: linear-gradient(90deg, #1A5F7A, #2C7865)
val GradientCTA             = listOf(Color(0xFF1A5F7A), Color(0xFF2C7865))

// HTML budget progress bar: linear-gradient(90deg, #1A5F7A, #2C7865)
val GradientBudget          = listOf(Color(0xFF1A5F7A), Color(0xFF2C7865))