package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/CategoryBarChart.kt
// Updated to accept an external CurrencyFormatter so the bar amounts react to currency changes.
// All layout / animation / visual code is identical to the original.

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.data.currency.CurrencyFormatter
import com.hafsaIlyas.expensetracker.data.currency.CurrencyService
import com.hafsaIlyas.expensetracker.ui.components.rememberCurrencyFormatter
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.CategoryShare

/**
 * Horizontal gradient bar chart for category breakdown.
 *
 * Pass either:
 *  - [formatter]       — a pre-built [CurrencyFormatter] (preferred when already available)
 *  - [currencyService] — will create the formatter internally via [rememberCurrencyFormatter]
 *
 * If neither is provided, falls back to plain US dollar formatting (backward-compat).
 */
@Composable
fun CategoryBarChart(
    categories      : List<CategoryShare>,
    modifier        : Modifier             = Modifier,
    formatter       : CurrencyFormatter?   = null,
    currencyService : CurrencyService?     = null
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    // Resolve formatter: explicit > service > US-dollar fallback
    val resolvedFormatter: CurrencyFormatter? = formatter
        ?: currencyService?.let { rememberCurrencyFormatter(it) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        categories.forEach { share ->
            BarRow(
                share     = share,
                progress  = animProgress.value,
                formatter = resolvedFormatter
            )
        }
    }
}

@Composable
private fun BarRow(
    share     : CategoryShare,
    progress  : Float,
    formatter : CurrencyFormatter?
) {
    val barColor = Color(share.color)

    val amountText = formatter?.format(share.amount)
        ?: java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).format(share.amount)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(share.category, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(amountText, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold, color = barColor)
                Surface(shape = RoundedCornerShape(4.dp), color = barColor.copy(alpha = 0.12f)) {
                    Text("${share.percentage.toInt()}%",
                        style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color    = barColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (share.percentage / 100f) * progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(colors = listOf(barColor.copy(alpha = 0.75f), barColor))
                    )
            )
        }
    }
}