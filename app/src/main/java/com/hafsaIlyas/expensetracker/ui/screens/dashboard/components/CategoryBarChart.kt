package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/CategoryBarChart.kt
// Polished animated bar chart with gradient bars, amount labels, and spring entry

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
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.CategoryShare
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategoryBarChart(
    categories : List<CategoryShare>,
    modifier   : Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    val progress  = animProgress.value
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        categories.forEach { share ->
            CategoryBarRow(
                share     = share,
                progress  = progress,
                formatter = formatter
            )
        }
    }
}

@Composable
private fun CategoryBarRow(
    share    : CategoryShare,
    progress : Float,
    formatter: NumberFormat
) {
    val barColor = Color(share.color)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Category emoji + name
            Text(
                text       = share.category,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = formatter.format(share.amount),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = barColor
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = barColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text     = "${share.percentage.toInt()}%",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = barColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            // Gradient progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (share.percentage / 100f) * progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(barColor.copy(alpha = 0.75f), barColor)
                        )
                    )
            )
        }
    }
}