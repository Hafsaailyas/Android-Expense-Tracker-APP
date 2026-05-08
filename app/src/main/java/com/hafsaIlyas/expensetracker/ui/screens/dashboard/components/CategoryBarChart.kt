package com.hafsaIlyas.expensetracker.ui.screens.dashboard.components

// ui/screens/dashboard/components/CategoryBarChart.kt

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.CategoryShare
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategoryBarChart(
    categories: List<CategoryShare>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(800, easing = EaseOutCubic))
    }

    val progress by animProgress.asState()
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
    share: CategoryShare,
    progress: Float,
    formatter: NumberFormat
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text      = share.category,
                style     = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color     = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = formatter.format(share.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(share.color)
                )
                Text(
                    text  = "${share.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (share.percentage / 100f) * progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(share.color))
            )
        }
    }
}