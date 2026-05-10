package com.hafsaIlyas.expensetracker.ui.components

// ui/components/GlassCard.kt
// Glassmorphism-style cards — subtle frosted look using layered backgrounds + border

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A glass-style card container.
 *
 * On Android, true backdrop blur requires RenderEffect (API 31+).
 * This achieves the effect visually using a semi-transparent fill + gradient border.
 *
 * @param backgroundBrush  Gradient used for the card fill (pass primary gradient for hero cards)
 * @param borderBrush      Gradient for the 1dp border stroke
 * @param cornerRadius     Shape rounding
 */
@Composable
fun GlassCard(
    modifier        : Modifier  = Modifier,
    backgroundBrush : Brush     = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f)
        )
    ),
    borderBrush     : Brush     = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.05f)
        )
    ),
    cornerRadius    : Dp        = 20.dp,
    content         : @Composable BoxScope.() -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width  = 1.dp,
                brush  = borderBrush,
                shape  = shape
            )
    ) {
        content()
    }
}

/**
 * Dark-tinted glass card — for use on dark backgrounds or gradient hero areas.
 */
@Composable
fun DarkGlassCard(
    modifier     : Modifier = Modifier,
    cornerRadius : Dp       = 20.dp,
    content      : @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier        = modifier,
        backgroundBrush = Brush.linearGradient(
            listOf(
                Color(0x33FFFFFF),   // 20% white
                Color(0x1AFFFFFF)    // 10% white
            )
        ),
        borderBrush = Brush.linearGradient(
            listOf(
                Color(0x66FFFFFF),
                Color(0x11FFFFFF)
            )
        ),
        cornerRadius = cornerRadius,
        content      = content
    )
}

/**
 * Gradient-border card — premium look for key dashboard cards.
 */
@Composable
fun GradientBorderCard(
    modifier     : Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        Color(0xFF0D7377),
        Color(0xFF14A085),
        Color(0xFF7B61FF)
    ),
    cornerRadius : Dp = 16.dp,
    content      : @Composable ColumnScope.() -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width  = 1.5.dp,
                brush  = Brush.linearGradient(gradientColors),
                shape  = shape
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content  = content
        )
    }
}