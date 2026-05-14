package com.hafsaIlyas.expensetracker.ui.components

// ui/components/AnimatedCounter.kt
// Smooth counting animation for currency / numeric values.
// AnimatedCurrencyCounter now accepts an optional CurrencyService so it reacts
// automatically when the user changes currency in Settings.

import androidx.compose.animation.core.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.hafsaIlyas.expensetracker.data.currency.CurrencyFormatter
import com.hafsaIlyas.expensetracker.data.currency.CurrencyService
import com.hafsaIlyas.expensetracker.data.currency.getByCode

/**
 * Animates from the previous value to [targetValue] using spring easing and
 * formats the result with the active currency via [currencyService].
 *
 * If [currencyService] is null (legacy call-sites), falls back to the supplied
 * [fallbackFormatter] — defaults to a plain US-dollar formatter so existing
 * previews / skeletons don't break.
 *
 * Example (preferred — currency-reactive):
 * ```kotlin
 * AnimatedCurrencyCounter(
 *     targetValue     = uiState.currentMonthTotal,
 *     currencyService = currencyService,
 *     style           = MaterialTheme.typography.displaySmall,
 *     color           = MaterialTheme.colorScheme.onPrimaryContainer
 * )
 * ```
 *
 * Example (legacy — fixed formatter):
 * ```kotlin
 * AnimatedCurrencyCounter(
 *     targetValue = total,
 *     fallbackFormatter = NumberFormat.getCurrencyInstance(Locale.US)
 * )
 * ```
 */
@Composable
fun AnimatedCurrencyCounter(
    targetValue       : Double,
    modifier          : Modifier             = Modifier,
    style             : TextStyle            = LocalTextStyle.current,
    color             : Color                = Color.Unspecified,
    currencyService   : CurrencyService?     = null,
    fallbackFormatter : java.text.NumberFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US),
    animSpec          : AnimationSpec<Float> = spring(
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

    // Resolve the correct formatter reactively
    val currencyFormatter: CurrencyFormatter? = if (currencyService != null) {
        rememberCurrencyFormatter(currencyService)
    } else null

    val displayText = if (currencyFormatter != null) {
        currencyFormatter.format(animatedValue.value.toDouble())
    } else {
        fallbackFormatter.format(animatedValue.value.toDouble())
    }

    Text(
        text     = displayText,
        modifier = modifier,
        style    = style,
        color    = color
    )
}

/**
 * Simpler integer counter — animates from 0 to [targetValue].
 * Unchanged from original.
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