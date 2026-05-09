package com.hafsaIlyas.expensetracker.ui.components

// ui/components/AnimatedCounter.kt

import androidx.compose.animation.core.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import java.util.Locale

/**
 * Animates a [Double] value counting up from 0 (or from the previous value) to [targetValue].
 * Renders the result as a currency string using [NumberFormat].
 *
 * @param targetValue   The number to count toward.
 * @param durationMs    Animation duration in milliseconds (default 900).
 * @param prefix        Optional prefix rendered before the number (e.g. "$").
 * @param style         [TextStyle] forwarded to the inner [Text].
 * @param color         Text color (defaults to [Color.Unspecified] so it inherits).
 * @param fontWeight    Font weight override.
 * @param modifier      Standard [Modifier].
 * @param locale        Locale used for number formatting (default [Locale.US]).
 */
@Composable
fun AnimatedCounter(
    targetValue : Double,
    modifier    : Modifier    = Modifier,
    durationMs  : Int         = 900,
    prefix      : String      = "",
    style       : TextStyle   = LocalTextStyle.current,
    color       : Color       = Color.Unspecified,
    fontWeight  : FontWeight? = null,
    locale      : Locale      = Locale.US,
) {
    val formatter = remember(locale) { NumberFormat.getCurrencyInstance(locale) }

    // Animate the displayed value using a Float animatable (doubles via mapping)
    val animatedValue by animateFloatAsState(
        targetValue   = targetValue.toFloat(),
        animationSpec = tween(
            durationMillis = durationMs,
            easing         = FastOutSlowInEasing,
        ),
        label = "counter_$targetValue"
    )

    val displayText = remember(animatedValue) {
        "$prefix${formatter.format(animatedValue.toDouble())}"
    }

    Text(
        text       = displayText,
        modifier   = modifier,
        style      = style,
        color      = color,
        fontWeight = fontWeight,
    )
}

/**
 * Variant that animates a plain integer (e.g. transaction count).
 */
@Composable
fun AnimatedIntCounter(
    targetValue : Int,
    modifier    : Modifier    = Modifier,
    durationMs  : Int         = 600,
    suffix      : String      = "",
    style       : TextStyle   = LocalTextStyle.current,
    color       : Color       = Color.Unspecified,
    fontWeight  : FontWeight? = null,
) {
    val animatedValue by animateFloatAsState(
        targetValue   = targetValue.toFloat(),
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
        label         = "int_counter"
    )

    Text(
        text       = "${animatedValue.toInt()}$suffix",
        modifier   = modifier,
        style      = style,
        color      = color,
        fontWeight = fontWeight,
    )
}