package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/CategoryPieChart.kt
// Redesigned to match HTML donut chart exactly:
//   • Arcs use the exact 5-color palette from the HTML (0xFF1A5F7A, 0xFF2C7865, etc.)
//   • strokeWidth 18dp, 3° gap between segments, round caps
//   • Center label: formatted total amount on one line (mirrors HTML <text> element)
//   • Spring entry animation from 0 → 1

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.CategoryShare

@Composable
fun CategoryDonutChart(
    categories  : List<CategoryShare>,
    modifier    : Modifier = Modifier,
    size        : Dp       = 100.dp,
    strokeWidth : Dp       = 18.dp,
    centerLabel : String   = ""
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue   = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness    = Spring.StiffnessLow
            )
        )
    }

    val progress   = animProgress.value
    val gapDegrees = 3f
    val totalGap   = gapDegrees * categories.size.coerceAtLeast(1)
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {

        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(this.size.width, this.size.height) - strokePx
            val topLeft  = Offset(
                (this.size.width  - diameter) / 2f,
                (this.size.height - diameter) / 2f
            )
            val arcSize  = Size(diameter, diameter)
            val stroke   = Stroke(width = strokePx, cap = StrokeCap.Round)

            // Background track
            drawArc(
                color      = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = strokePx)
            )

            // Segments
            var startAngle = -90f
            categories.forEach { share ->
                val sweep = (share.percentage / 100f) * (360f - totalGap) * progress
                drawArc(
                    color      = Color(share.color),
                    startAngle = startAngle,
                    sweepAngle = sweep.coerceAtLeast(0.1f),
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = stroke
                )
                startAngle += sweep + gapDegrees
            }
        }

        // Center label — matches HTML <text> element inside the SVG
        if (centerLabel.isNotBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = centerLabel,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight(700),
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}