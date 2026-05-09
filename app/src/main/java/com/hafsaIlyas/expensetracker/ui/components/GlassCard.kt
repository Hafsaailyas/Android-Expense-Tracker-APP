package com.hafsaIlyas.expensetracker.ui.components

// ui/components/GlassCard.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A card with a glassmorphism aesthetic:
 *  - Gradient overlay using [gradientColors] (default: teal brand gradient)
 *  - Semi-transparent frosted surface via [backgroundAlpha]
 *  - Subtle 1dp border with white/light tint for the "glass edge" effect
 *  - Optional soft shadow via [elevation]
 *
 * Compose does not support true backdrop-blur on Android (requires RenderEffect API 31+
 * and SurfaceView compositing tricks), so we approximate the effect with layered gradients
 * and translucency. On API 31+ you can extend this composable to apply RenderEffect if needed.
 *
 * Usage:
 * ```
 * GlassCard(modifier = Modifier.fillMaxWidth()) {
 *     Text("Hello glass!")
 * }
 * ```
 */
@Composable
fun GlassCard(
    modifier         : Modifier    = Modifier,
    gradientColors   : List<Color>? = null,    // null → use brand primary gradient
    backgroundAlpha  : Float       = 0.18f,
    elevation        : Dp          = 8.dp,
    cornerRadius     : Dp          = 28.dp,
    content          : @Composable BoxScope.() -> Unit,
) {
    val shape       = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
    val primary     = MaterialTheme.colorScheme.primary
    val onPrimary   = MaterialTheme.colorScheme.onPrimary

    // Default brand gradient if none supplied
    val resolvedGradient = gradientColors ?: listOf(
        primary.copy(alpha = 0.85f),
        primary.copy(alpha = 0.55f),
        Color(0xFF0D5954).copy(alpha = 0.70f),    // deep teal terminal colour
    )

    Box(
        modifier = modifier
            .shadow(
                elevation    = elevation,
                shape        = shape,
                ambientColor = primary.copy(alpha = 0.25f),
                spotColor    = primary.copy(alpha = 0.35f),
            )
            .clip(shape)
            // Gradient background layer
            .background(
                brush = Brush.linearGradient(
                    colors = resolvedGradient,
                    start  = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end    = androidx.compose.ui.geometry.Offset(
                        Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY,
                    ),
                )
            )
            // Translucent white noise layer (glass shimmer)
            .background(
                color = Color.White.copy(alpha = backgroundAlpha)
            )
            // Glass border — top/left lighter, bottom/right subtler
            .border(
                width  = 1.dp,
                brush  = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.10f),
                    )
                ),
                shape  = shape,
            ),
        content = content,
    )
}

/**
 * Lighter variant for use on dark surfaces — uses surface color as base
 * so it blends with the current theme's surface instead of the primary.
 */
@Composable
fun SurfaceGlassCard(
    modifier        : Modifier = Modifier,
    backgroundAlpha : Float    = 0.08f,
    elevation       : Dp       = 4.dp,
    cornerRadius    : Dp       = 20.dp,
    content         : @Composable BoxScope.() -> Unit,
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary        = MaterialTheme.colorScheme.primary

    GlassCard(
        modifier        = modifier,
        gradientColors  = listOf(
            surfaceVariant.copy(alpha = 0.90f),
            surfaceVariant.copy(alpha = 0.70f),
            primary.copy(alpha = 0.08f),
        ),
        backgroundAlpha = backgroundAlpha,
        elevation       = elevation,
        cornerRadius    = cornerRadius,
        content         = content,
    )
}