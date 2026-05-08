package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/SpendingHeaderCard.kt

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.TrendDirection
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SpendingHeaderCard(
    monthName: String,
    currentTotal: Double,
    previousTotal: Double,
    percentageChange: Double,
    trendDirection: TrendDirection,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.extraLarge,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = monthName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            AnimatedContent(targetState = currentTotal, label = "total_anim") { total ->
                Text(
                    text       = formatter.format(total),
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Trend row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val (icon, trendColor, trendText) = when (trendDirection) {
                    TrendDirection.UP -> Triple(
                        Icons.Default.TrendingUp,
                        Color(0xFFEF5350),
                        "+${percentageChange}% vs last month"
                    )
                    TrendDirection.DOWN -> Triple(
                        Icons.Default.TrendingDown,
                        Color(0xFF66BB6A),
                        "-${percentageChange}% vs last month"
                    )
                    TrendDirection.NEUTRAL -> Triple(
                        Icons.Default.TrendingFlat,
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        "Same as last month"
                    )
                }

                Icon(
                    imageVector       = icon,
                    contentDescription = null,
                    tint              = trendColor,
                    modifier          = Modifier.size(18.dp)
                )
                Text(
                    text  = trendText,
                    style = MaterialTheme.typography.bodySmall,
                    color = trendColor,
                    fontWeight = FontWeight.Medium
                )
            }

            if (previousTotal > 0.0) {
                Text(
                    text  = "Last month: ${formatter.format(previousTotal)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
        }
    }
}