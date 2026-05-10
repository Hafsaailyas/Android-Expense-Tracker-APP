package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/SpendingHeaderCard.kt
// Premium gradient hero card with animated counter, trend chip, and optional budget ring

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.ui.components.AnimatedCurrencyCounter
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.TrendDirection
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SpendingHeaderCard(
    monthName        : String,
    currentTotal     : Double,
    previousTotal    : Double,
    percentageChange : Double,
    trendDirection   : TrendDirection,
    monthlyBudget    : Double          = 0.0,   // 0 = not set
    modifier         : Modifier        = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    // Gradient — deep teal to emerald
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF063E42), Color(0xFF0D7377), Color(0xFF0FA890))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBrush)
    ) {
        // Decorative circle glow top-right
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 80.dp, y = (-50).dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(0.10f), Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // ── Left: amount + trend ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text  = monthName,
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.8.sp),
                    color = Color.White.copy(alpha = 0.65f)
                )

                // Animated currency counter
                AnimatedCurrencyCounter(
                    targetValue = currentTotal,
                    style       = MaterialTheme.typography.displaySmall.copy(
                        fontWeight   = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )

                Spacer(Modifier.height(2.dp))

                // Trend chip
                TrendChip(
                    direction        = trendDirection,
                    percentageChange = percentageChange
                )

                if (previousTotal > 0.0) {
                    Text(
                        text  = "Last month  ${formatter.format(previousTotal)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }

            // ── Right: budget ring (only if budget is set) ────────────────────
            if (monthlyBudget > 0.0) {
                BudgetRing(
                    spent  = currentTotal,
                    budget = monthlyBudget
                )
            }
        }
    }
}

// ── Trend chip ────────────────────────────────────────────────────────────────

@Composable
private fun TrendChip(direction: TrendDirection, percentageChange: Double) {
    val (icon, chipColor, chipText) = when (direction) {
        TrendDirection.UP -> Triple(
            Icons.Default.TrendingUp,
            Color(0xFFEF5350),
            "+${percentageChange}% vs last month"
        )
        TrendDirection.DOWN -> Triple(
            Icons.Default.TrendingDown,
            Color(0xFF4CAF50),
            "-${percentageChange}% vs last month"
        )
        TrendDirection.NEUTRAL -> Triple(
            Icons.Default.TrendingFlat,
            Color.White.copy(0.45f),
            "Same as last month"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = chipColor.copy(alpha = 0.18f)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = chipColor, modifier = Modifier.size(14.dp))
            Text(chipText, style = MaterialTheme.typography.labelSmall, color = chipColor)
        }
    }
}

// ── Budget ring ───────────────────────────────────────────────────────────────

@Composable
private fun BudgetRing(spent: Double, budget: Double) {
    val fraction   = (spent / budget).coerceIn(0.0, 1.0).toFloat()
    val overBudget = spent > budget

    val animatedFraction = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animatedFraction.animateTo(
            fraction,
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    val ringColor = when {
        fraction >= 1f  -> Color(0xFFEF5350)
        fraction >= 0.8f -> Color(0xFFFFB347)
        else            -> Color(0xFF4CAF50)
    }

    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(72.dp)) {
            val stroke   = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            val inset    = stroke.width / 2
            val arcSize  = androidx.compose.ui.geometry.Size(
                size.width - inset * 2, size.height - inset * 2
            )
            val topLeft  = androidx.compose.ui.geometry.Offset(inset, inset)

            // Track
            drawArc(
                color      = Color.White.copy(0.15f),
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
                sweepAngle = 360f * animatedFraction.value,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text  = if (overBudget) "over" else "used",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = Color.White.copy(0.55f)
            )
        }
    }
}