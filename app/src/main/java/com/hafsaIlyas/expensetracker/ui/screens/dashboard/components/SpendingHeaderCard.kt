package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/SpendingHeaderCard.kt

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.ui.components.AnimatedCounter
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.TrendDirection
import com.hafsaIlyas.expensetracker.ui.theme.SplashBottom
import com.hafsaIlyas.expensetracker.ui.theme.SplashTop
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun SpendingHeaderCard(
    monthName        : String,
    currentTotal     : Double,
    previousTotal    : Double,
    percentageChange : Double,
    trendDirection   : TrendDirection,
    modifier         : Modifier = Modifier,
) {
    // Glassmorphism via layered gradients + translucent overlay
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SplashTop, SplashBottom),
                    start  = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end    = androidx.compose.ui.geometry.Offset(
                        Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY
                    ),
                )
            )
            // frosted glass shimmer
            .background(Color.White.copy(alpha = 0.10f)),
    ) {
        // Decorative circles in background for depth
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 180.dp, y = (-40).dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-30).dp, y = 80.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier            = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Month label
            Text(
                text  = monthName.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    color         = Color.White.copy(alpha = 0.70f),
                )
            )

            Spacer(Modifier.height(2.dp))

            // ── Animated spending total ───────────────────────────────────
            AnimatedCounter(
                targetValue = currentTotal,
                durationMs  = 900,
                style       = MaterialTheme.typography.displayMedium,
                fontWeight  = FontWeight.ExtraBold,
                color       = Color.White,
            )

            Spacer(Modifier.height(4.dp))

            // ── Trend chip ────────────────────────────────────────────────
            TrendChip(
                direction        = trendDirection,
                percentageChange = percentageChange,
            )

            // ── Previous month ────────────────────────────────────────────
            if (previousTotal > 0.0) {
                val formatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
                Text(
                    text  = "Last month: ${formatter.format(previousTotal)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.50f),
                    )
                )
            }
        }
    }
}

@Composable
private fun TrendChip(
    direction        : TrendDirection,
    percentageChange : Double,
) {
    val (icon, chipBg, chipFg, label) = when (direction) {
        TrendDirection.UP -> Quad(
            Icons.Default.TrendingUp,
            Color(0xFFDC2626).copy(alpha = 0.25f),
            Color(0xFFFCA5A5),
            "+${percentageChange.absoluteValue.format1dp()}% vs last month",
        )
        TrendDirection.DOWN -> Quad(
            Icons.Default.TrendingDown,
            Color(0xFF059669).copy(alpha = 0.25f),
            Color(0xFF6EE7B7),
            "-${percentageChange.absoluteValue.format1dp()}% vs last month",
        )
        TrendDirection.NEUTRAL -> Quad(
            Icons.Default.TrendingFlat,
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.70f),
            "Same as last month",
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = chipBg,
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = chipFg,
                modifier           = Modifier.size(14.dp),
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = chipFg,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Tiny helpers ──────────────────────────────────────────────────────────────

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

private fun Double.format1dp() = "%.1f".format(this)