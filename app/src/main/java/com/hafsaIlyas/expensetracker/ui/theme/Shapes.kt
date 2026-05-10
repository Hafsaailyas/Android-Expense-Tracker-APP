package com.hafsaIlyas.expensetracker.ui.theme

// ui/theme/Shapes.kt
// Custom shape tokens — pairs with Material 3 shapes system

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    // Chips, badges, small indicators
    extraSmall = RoundedCornerShape(4.dp),
    // Input fields, small cards
    small      = RoundedCornerShape(8.dp),
    // Standard cards, buttons
    medium     = RoundedCornerShape(12.dp),
    // Large cards, bottom sheets
    large      = RoundedCornerShape(16.dp),
    // Hero cards, dialogs, modals
    extraLarge = RoundedCornerShape(24.dp),
)

// Custom shapes not in M3 scale
val PillShape       = RoundedCornerShape(50)
val TopRoundedShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val CardShape       = RoundedCornerShape(16.dp)
val ChipShape       = RoundedCornerShape(8.dp)