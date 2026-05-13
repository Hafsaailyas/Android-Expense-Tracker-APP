package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/SpendingHeaderCard.kt
// Standalone hero card kept for reuse — redesigned to match HTML .hero-card exactly:
//   • Primary→Secondary linear gradient (matches --primary #1A5F7A → --secondary #2C7865)
//   • Decorative circle glow top-right (hero-card::before)
//   • "TOTAL SPENT" uppercase label, animated counter, gold trend chip
//   • Budget ring (60×60) with gold arc + % label in centre
//   • Full dark/light mode support via Color constants

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.ui.components.AnimatedCurrencyCounter
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.TrendDirection

private val Primary   = Color(0xFF1A5F7A)
private val Secondary = Color(0xFF2C7865)
private val Gold      = Color(0xFFF9D56E)
private val White     = Color.White

@Composable
fun SpendingHeaderCard(
    monthName       : String,
    currentTotal    : Double,
    previousTotal   : Double,
    percentageChange: Double,
    trendDirection  : TrendDirection,
    monthlyBudget   : Double  = 0.0,
    modifier        : Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Primary, Secondary),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative glow circle — hero-card::before
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 30.dp, y = (-30).dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(White.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(modifier = Modifier.padding(20.dp)) {

            // "TOTAL SPENT" label
            Text(
                "TOTAL SPENT",
                style      = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                fontWeight = FontWeight.SemiBold,
                color      = White.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(6.dp))

            // Animated currency counter
            AnimatedCurrencyCounter(
                targetValue = currentTotal,
                style       = MaterialTheme.typography.displaySmall.copy(
                    fontWeight    = FontWeight(800),
                    letterSpacing = (-2).sp,
                    fontSize      = 40.sp
                ),
                color = White
            )

            Spacer(Modifier.height(10.dp))

            // Bottom row: trend chip + budget ring
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                HeroTrendChip(direction = trendDirection, percentageChange = percentageChange)

                if (monthlyBudget > 0.0) {
                    HeroBudgetRing(spent = currentTotal, budget = monthlyBudget)
                }
            }
        }
    }
}

// Gold pill trend chip
@Composable
private fun HeroTrendChip(direction: TrendDirection, percentageChange: Double) {
    val (icon, text) = when (direction) {
        TrendDirection.UP      -> Icons.Default.TrendingUp   to "+${percentageChange}% vs last month"
        TrendDirection.DOWN    -> Icons.Default.TrendingDown to "-${percentageChange}% vs last month"
        TrendDirection.NEUTRAL -> Icons.Default.TrendingFlat to "±0% vs last month"
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, null, tint = Gold, modifier = Modifier.size(13.dp))
            Text(
                text,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = Gold
            )
        }
    }
}

// Budget ring — 60×60 canvas ring with gold arc + % + "used"
@Composable
private fun HeroBudgetRing(spent: Double, budget: Double) {
    val fraction     = (spent / budget).coerceIn(0.0, 1.0).toFloat()
    val overBudget   = spent > budget

    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animFraction.animateTo(
            fraction,
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
        )
    }

    val ringColor = when {
        fraction >= 1f   -> Color(0xFFEF5350)
        fraction >= 0.8f -> Color(0xFFFFB347)
        else             -> Gold
    }

    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(60.dp)) {
            val strokePx = 6.dp.toPx()
            val stroke   = Stroke(width = strokePx, cap = StrokeCap.Round)
            val inset    = strokePx / 2
            val arcSize  = androidx.compose.ui.geometry.Size(
                size.width - inset * 2, size.height - inset * 2
            )
            val topLeft  = Offset(inset, inset)

            // Track
            drawArc(
                color      = White.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = stroke
            )
            // Progress
            drawArc(
                color      = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animFraction.value,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(fraction * 100).toInt()}%",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = White,
                fontSize   = 11.sp
            )
            Text(
                if (overBudget) "over" else "used",
                style    = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color    = White.copy(alpha = 0.55f)
            )
        }
    }
}