package com.hafsaIlyas.expensetracker.ui.components

// ui/components/CategoryIcon.kt

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Category-to-color mapping.
 * Returns a brand-appropriate [Color] for each known category string.
 */
fun categoryColor(category: String): Color = when {
    category.contains("Food",          ignoreCase = true) -> Color(0xFFF97316) // Orange
    category.contains("Transport",     ignoreCase = true) -> Color(0xFF3B82F6) // Blue
    category.contains("Rent",          ignoreCase = true) -> Color(0xFF8B5CF6) // Violet
    category.contains("Shopping",      ignoreCase = true) -> Color(0xFFEC4899) // Pink
    category.contains("Health",        ignoreCase = true) -> Color(0xFF10B981) // Emerald
    category.contains("Entertainment", ignoreCase = true) -> Color(0xFFF59E0B) // Amber
    category.contains("Education",     ignoreCase = true) -> Color(0xFF06B6D4) // Cyan
    category.contains("Utilities",     ignoreCase = true) -> Color(0xFF64748B) // Slate
    category.contains("Travel",        ignoreCase = true) -> Color(0xFF0EA5E9) // Sky
    category.contains("Other",         ignoreCase = true) -> Color(0xFF6B7280) // Gray
    else                                                  -> Color(0xFF0F766E) // Teal fallback
}

/**
 * Extracts the emoji from the start of a category string like "🍔 Food".
 * Falls back to the first two characters if no emoji is found.
 */
fun categoryEmoji(category: String): String {
    // Emoji characters are typically 1–2 code points; take the first grapheme cluster
    val trimmed = category.trim()
    if (trimmed.isEmpty()) return "??"
    // Check for leading emoji (code point > 0xFFFF typically signals emoji)
    val cp = trimmed.codePointAt(0)
    return if (cp > 0xFFFF || cp in 0x2600..0x27BF || cp in 0x1F300..0x1FAFF) {
        trimmed.substring(0, trimmed.offsetByCodePoints(0, 1))
    } else {
        trimmed.take(2)
    }
}

/**
 * Rounded square avatar showing the category emoji on a tinted background.
 *
 * @param category     Full category string e.g. "🍔 Food"
 * @param size         Overall size of the avatar box
 * @param cornerRadius Corner radius of the rounded square
 * @param emojiSize    Font size of the emoji
 * @param selected     When true, pulses with a scale animation
 */
@Composable
fun CategoryIcon(
    category     : String,
    modifier     : Modifier = Modifier,
    size         : Dp       = 44.dp,
    cornerRadius : Dp       = 12.dp,
    emojiSize    : TextUnit = 20.sp,
    selected     : Boolean  = false,
) {
    val color  = categoryColor(category)
    val emoji  = categoryEmoji(category)

    // Pulse scale when selected
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "cat_icon_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text      = emoji,
            fontSize  = emojiSize,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Larger variant used in the 3-column category grid on AddExpense.
 */
@Composable
fun CategoryGridIcon(
    category : String,
    selected : Boolean  = false,
    modifier : Modifier = Modifier,
) {
    val color = categoryColor(category)
    val emoji = categoryEmoji(category)
    val label = category.drop(
        category.indexOfFirst { it == ' ' }.takeIf { it >= 0 }?.plus(1) ?: 0
    ).trim()

    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "grid_icon_scale"
    )

    Column(
        modifier            = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) color.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text     = emoji,
            fontSize = 28.sp,
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}