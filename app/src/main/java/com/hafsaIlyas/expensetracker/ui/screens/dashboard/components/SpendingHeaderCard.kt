package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/SpendingHeaderCard.kt
// CHANGES vs original:
//   • Accepts BudgetStatus + remainingBudget so the card reacts to all four states
//   • HeroBudgetRing now colour-coded by status (Normal/Warning/HighAlert/OverBudget)
//   • Added BudgetStatusChip — a small pill below the ring showing human-readable label
//   • Added "Remaining: X" / "Over by: X" sub-label beneath the trend chip row
//   • Ring shows "over" label in red when OVER_BUDGET
//   • If no budget is set (monthlyBudget == 0) the budget section is hidden (unchanged behaviour)
//   • "Set a budget" button shown when budget == 0, clickable to navigate to settings
//   • All animations use spring (DampingRatioNoBouncy, StiffnessLow) — unchanged

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.hafsaIlyas.expensetracker.ui.viewmodel.BudgetStatus

// ── Brand colours (local to this file) ───────────────────────────────────────
private val Primary   = Color(0xFF1A5F7A)
private val Secondary = Color(0xFF2C7865)
private val Gold      = Color(0xFFF9D56E)
private val White     = Color.White

// Budget status colours
private val RingNormal    = Gold                    // 0–74 % — gold (original)
private val RingWarning   = Color(0xFFFFB347)       // 75–89 % — amber
private val RingHighAlert = Color(0xFFE65100)       // 90–99 % — orange/deep amber
private val RingOverBudget= Color(0xFFEF5350)       // ≥ 100 % — red

// ─────────────────────────────────────────────────────────────────────────────
// Public composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SpendingHeaderCard(
    monthName        : String,
    currentTotal     : Double,
    previousTotal    : Double,
    percentageChange : Double,
    trendDirection   : TrendDirection,
    monthlyBudget    : Double       = 0.0,
    budgetPercentage : Float        = 0f,
    remainingBudget  : Double       = 0.0,
    isOverBudget     : Boolean      = false,
    budgetStatus     : BudgetStatus = BudgetStatus.NO_BUDGET,
    formatAmount     : ((Double) -> String)? = null,
    onSetBudgetClick : () -> Unit = {},
    modifier         : Modifier     = Modifier
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

                when {
                    // Budget is set — show the ring
                    monthlyBudget > 0.0 -> {
                        HeroBudgetRing(
                            spent           = currentTotal,
                            budget          = monthlyBudget,
                            budgetPercentage= budgetPercentage,
                            isOverBudget    = isOverBudget,
                            budgetStatus    = budgetStatus
                        )
                    }
                    // No budget — show a clickable nudge button
                    else -> {
                        NoBudgetNudge(onSetBudgetClick = onSetBudgetClick)
                    }
                }
            }

            // ── remaining / over-budget sub-label ────────────────────────
            if (monthlyBudget > 0.0) {
                Spacer(Modifier.height(10.dp))
                BudgetSubLabel(
                    isOverBudget    = isOverBudget,
                    remainingBudget = remainingBudget,
                    budgetStatus    = budgetStatus,
                    formatAmount    = formatAmount
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private sub-composables
// ─────────────────────────────────────────────────────────────────────────────

/** Gold pill trend chip — unchanged from original. */
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

/**
 * Budget ring — 60×60 canvas arc coloured by [BudgetStatus].
 *
 * Replaces the original [HeroBudgetRing] which hardcoded gold/amber/red based
 * on raw fraction. Now status is pre-computed by [ExpenseViewModel].
 */
@Composable
private fun HeroBudgetRing(
    spent           : Double,
    budget          : Double,
    budgetPercentage: Float,
    isOverBudget    : Boolean,
    budgetStatus    : BudgetStatus
) {
    // Clamp to [0,1] for the ring sweep (over-budget fills the ring completely)
    val fraction    = budgetPercentage.coerceIn(0f, 1f)

    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animFraction.animateTo(
            fraction,
            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
        )
    }

    val ringColor = when (budgetStatus) {
        BudgetStatus.NORMAL     -> RingNormal
        BudgetStatus.WARNING    -> RingWarning
        BudgetStatus.HIGH_ALERT -> RingHighAlert
        BudgetStatus.OVER_BUDGET-> RingOverBudget
        BudgetStatus.NO_BUDGET  -> Gold          // fallback (shouldn't reach here)
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
            // Progress arc
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
                "${(budgetPercentage * 100).toInt()}%",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = White,
                fontSize   = 11.sp
            )
            Text(
                if (isOverBudget) "over" else "used",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = if (isOverBudget) RingOverBudget else White.copy(alpha = 0.55f)
            )
        }
    }
}

/**
 * Row with a status chip + remaining/over-budget amount.
 * Positioned below the trend chip / ring row.
 */
@Composable
private fun BudgetSubLabel(
    isOverBudget    : Boolean,
    remainingBudget : Double,
    budgetStatus    : BudgetStatus,
    formatAmount    : ((Double) -> String)?
) {
    val absAmount = kotlin.math.abs(remainingBudget)
    val amountStr = formatAmount?.invoke(absAmount)
        ?: java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault()).format(absAmount)

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Status chip
        BudgetStatusChip(budgetStatus = budgetStatus)

        // Remaining / over-budget amount
        Text(
            text = if (isOverBudget) "Over by $amountStr" else "Remaining: $amountStr",
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = when (budgetStatus) {
                BudgetStatus.OVER_BUDGET -> RingOverBudget
                BudgetStatus.HIGH_ALERT  -> RingHighAlert
                BudgetStatus.WARNING     -> RingWarning
                else                     -> White.copy(alpha = 0.85f)
            },
            fontSize = 11.sp
        )
    }
}

/**
 * Pill chip whose label + colour reflects the current [BudgetStatus].
 *
 * | Status      | Label              | Colour            |
 * |-------------|--------------------|-------------------|
 * | NORMAL      | Budget OK ✓        | white/translucent |
 * | WARNING     | Approaching limit  | amber             |
 * | HIGH_ALERT  | Near limit!        | orange            |
 * | OVER_BUDGET | Over budget!       | red               |
 */
@Composable
private fun BudgetStatusChip(budgetStatus: BudgetStatus) {
    val (label, chipBg, textColor) = when (budgetStatus) {
        BudgetStatus.NORMAL      -> Triple("Budget OK ✓",        White.copy(alpha = 0.18f), White)
        BudgetStatus.WARNING     -> Triple("Approaching limit",   RingWarning.copy(alpha = 0.25f), RingWarning)
        BudgetStatus.HIGH_ALERT  -> Triple("Near limit!",         RingHighAlert.copy(alpha = 0.25f), RingHighAlert)
        BudgetStatus.OVER_BUDGET -> Triple("Over budget!",        RingOverBudget.copy(alpha = 0.25f), RingOverBudget)
        BudgetStatus.NO_BUDGET   -> Triple("",                    Color.Transparent, Color.Transparent)
    }

    if (label.isBlank()) return

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = chipBg
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = textColor,
            fontSize   = 10.sp,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

/**
 * Shown when no monthly budget is set — clickable button that invites the user to set one.
 * When clicked, calls onSetBudgetClick to navigate to Settings screen.
 */
@Composable
private fun NoBudgetNudge(
    onSetBudgetClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = White.copy(alpha = 0.12f),
        modifier = Modifier.clickable { onSetBudgetClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = "Set budget",
                tint = Gold.copy(alpha = 0.8f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                "Set budget",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = White.copy(alpha = 0.65f),
                fontSize = 10.sp
            )
        }
    }
}