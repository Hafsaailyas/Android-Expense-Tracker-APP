package com.hafsaIlyas.expensetracker.ui.components

// ui/components/ShimmerEffect.kt
// Reusable shimmer loading skeleton — use anywhere in place of CircularProgressIndicator

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Produces an animated shimmer brush.
 * Use inside any Box/Surface as the .background(...) modifier.
 */
@Composable
fun shimmerBrush(
    shimmerColor    : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    highlightColor  : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1000f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = listOf(shimmerColor, highlightColor, shimmerColor),
        start  = Offset(translateAnim - 300f, 0f),
        end    = Offset(translateAnim, 0f)
    )
}

// ── Pre-built skeleton shapes ─────────────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier    : Modifier = Modifier,
    cornerRadius: Dp       = 8.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Composable
fun ShimmerLine(
    modifier    : Modifier = Modifier,
    height      : Dp       = 14.dp,
    cornerRadius: Dp       = 4.dp
) {
    ShimmerBox(modifier = modifier.height(height), cornerRadius = cornerRadius)
}

// ── Dashboard skeleton ────────────────────────────────────────────────────────

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(160.dp), cornerRadius = 24.dp)
        // Chart toggle chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(modifier = Modifier.width(80.dp).height(32.dp), cornerRadius = 8.dp)
            ShimmerBox(modifier = Modifier.width(64.dp).height(32.dp), cornerRadius = 8.dp)
        }
        // Chart card
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(220.dp), cornerRadius = 16.dp)
        // Recent rows
        repeat(3) {
            ShimmerExpenseRow()
        }
    }
}

// ── Expense list skeleton ─────────────────────────────────────────────────────

@Composable
fun ExpenseListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(7) { ShimmerExpenseRow() }
    }
}

@Composable
fun ShimmerExpenseRow(modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(modifier = Modifier.size(44.dp), cornerRadius = 12.dp)
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShimmerLine(modifier = Modifier.fillMaxWidth(0.5f))
            ShimmerLine(modifier = Modifier.fillMaxWidth(0.35f), height = 10.dp)
        }
        ShimmerLine(modifier = Modifier.width(60.dp), height = 14.dp)
    }
}

// ── AI insights skeleton ──────────────────────────────────────────────────────

@Composable
fun InsightsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp), cornerRadius = 24.dp)
        Spacer(Modifier.height(4.dp))
        repeat(4) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(110.dp), cornerRadius = 16.dp)
        }
    }
}