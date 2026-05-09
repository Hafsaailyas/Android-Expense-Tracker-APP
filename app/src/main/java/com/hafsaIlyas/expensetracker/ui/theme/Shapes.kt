package com.hafsaIlyas.expensetracker.ui.theme

// ui/theme/Shapes.kt

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale for Expense Tracker.
 *
 * Material 3 Shapes slots map to our custom radii:
 *
 *  extraSmall  →  4 dp  — tooltips, small badges
 *  small       →  8 dp  — chips, tags, input fields
 *  medium      → 12 dp  — buttons, FAB, small cards
 *  large       → 20 dp  — regular cards, bottom sheets (top corners)
 *  extraLarge  → 28 dp  — hero/main cards, modal sheets
 *
 * Usage example:
 *   Card(shape = MaterialTheme.shapes.large) { … }
 *   Button(shape = MaterialTheme.shapes.medium) { … }
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// ─── Named aliases for call-site clarity ──────────────────────────────────
// Import these directly when you need a specific shape without going
// through MaterialTheme.shapes.

/** 28 dp — hero / summary cards (e.g. the total-balance card on Dashboard). */
val ShapeHeroCard     = RoundedCornerShape(28.dp)

/** 20 dp — standard transaction / category cards. */
val ShapeCard         = RoundedCornerShape(20.dp)

/** 12 dp — buttons, FABs, dialog action areas. */
val ShapeButton       = RoundedCornerShape(12.dp)

/** 8 dp  — filter chips, tags, small badges. */
val ShapeChip         = RoundedCornerShape(8.dp)

/** 4 dp  — tiny elements (tooltips, indicators). */
val ShapeSmall        = RoundedCornerShape(4.dp)

/** Full pill — used for toggle chips and some icon buttons. */
val ShapePill         = RoundedCornerShape(50)

/**
 * Top-only rounding for bottom sheets / nav drawers:
 * top corners are rounded, bottom corners are square.
 */
val ShapeBottomSheet  = RoundedCornerShape(
    topStart    = 28.dp,
    topEnd      = 28.dp,
    bottomStart = 0.dp,
    bottomEnd   = 0.dp,
)