package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/CategoryPieChart.kt

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.CategoryShare
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategoryDonutChart(
    categories: List<CategoryShare>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 32.dp,
    centerLabel: String = ""
) {
    // Animate each segment sweeping in
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue    = 1f,
            animationSpec  = tween(durationMillis = 900, easing = EaseOutCubic)
        )
    }

    val progress by animProgress.asState()
    val gapDegrees = 2f
    val totalGap = gapDegrees * categories.size

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(this.size.width, this.size.height) - strokePx
            val topLeft  = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)
            val arcSize  = Size(diameter, diameter)

            var startAngle = -90f

            categories.forEach { share ->
                val sweep = (share.percentage / 100f) * (360f - totalGap) * progress

                // Draw segment
                drawArc(
                    color       = Color(share.color),
                    startAngle  = startAngle,
                    sweepAngle  = sweep,
                    useCenter   = false,
                    topLeft     = topLeft,
                    size        = arcSize,
                    style       = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                startAngle += sweep + gapDegrees
            }

            // Background track
            if (categories.isEmpty()) {
                drawArc(
                    color      = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = strokePx)
                )
            }
        }

        // Center text
        if (centerLabel.isNotBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "Total",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = centerLabel,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}