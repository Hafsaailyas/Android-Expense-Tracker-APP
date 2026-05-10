package com.hafsaIlyas.expensetracker.ui.components

// ui/components/AnimatedCounter.kt
// Smooth counting animation for currency / numeric values

import androidx.compose.animation.core.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import java.text.NumberFormat
import java.util.Locale

/**
 * Animates from [previousValue] to [targetValue] using spring easing.
 * Formats the result using [formatter] — defaults to US currency.
 *
 * Example:
 *   AnimatedCounter(
 *       targetValue  = uiState.currentMonthTotal,
 *       modifier     = Modifier,
 *       style        = MaterialTheme.typography.displaySmall,
 *       color        = MaterialTheme.colorScheme.onPrimaryContainer
 *   )
 */
@Composable
fun AnimatedCurrencyCounter(
    targetValue   : Double,
    modifier      : Modifier    = Modifier,
    style         : TextStyle   = LocalTextStyle.current,
    color         : Color       = Color.Unspecified,
    formatter     : NumberFormat = NumberFormat.getCurrencyInstance(Locale.US),
    animSpec      : AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessLow
    )
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue   = targetValue.toFloat(),
            animationSpec = animSpec
        )
    }

    Text(
        text     = formatter.format(animatedValue.value.toDouble()),
        modifier = modifier,
        style    = style,
        color    = color
    )
}

/**
 * Simpler integer counter — animates from 0 to [targetValue].
 */
@Composable
fun AnimatedIntCounter(
    targetValue   : Int,
    modifier      : Modifier  = Modifier,
    style         : TextStyle = LocalTextStyle.current,
    color         : Color     = Color.Unspecified,
    suffix        : String    = ""
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue   = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 700, easing = EaseOutCubic)
        )
    }

    Text(
        text     = "${animatedValue.value.toInt()}$suffix",
        modifier = modifier,
        style    = style,
        color    = color
    )
}