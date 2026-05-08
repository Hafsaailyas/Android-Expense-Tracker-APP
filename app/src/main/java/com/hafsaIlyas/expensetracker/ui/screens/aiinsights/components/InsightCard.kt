package com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components

// ui/screens/aiinsights/components/InsightCard.kt

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hafsaIlyas.expensetracker.data.ai.Insight
import com.hafsaIlyas.expensetracker.data.ai.InsightType
import com.hafsaIlyas.expensetracker.data.ai.Severity

@Composable
fun InsightCard(
    insight: Insight,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    val (containerColor, borderColor, iconTint, icon) = insightVisuals(insight)

    AnimatedVisibility(
        visible       = visible,
        enter         = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        modifier      = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, MaterialTheme.shapes.large),
            shape    = MaterialTheme.shapes.large,
            colors   = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = iconTint.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null,
                                tint = iconTint, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(
                        text       = insight.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Body — typewriter only for first card
                Text(
                    text  = insight.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Optional CTA
                insight.actionLabel?.let { label ->
                    TextButton(
                        onClick = { /* TODO: deep-link */ },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(label, color = iconTint, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, null,
                            tint = iconTint, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ── Visual config per severity ─────────────────────────────────────────────────

private data class InsightVisuals(
    val containerColor: Color,
    val borderColor: Color,
    val iconTint: Color,
    val icon: ImageVector
)

@Composable
private fun insightVisuals(insight: Insight): InsightVisuals {
    val cs = MaterialTheme.colorScheme
    return when (insight.severity) {
        Severity.CRITICAL -> InsightVisuals(
            containerColor = Color(0xFFFFF0F0),
            borderColor    = Color(0xFFEF9A9A),
            iconTint       = Color(0xFFC62828),
            icon           = Icons.Default.Warning
        )
        Severity.WARNING -> InsightVisuals(
            containerColor = Color(0xFFFFFBE6),
            borderColor    = Color(0xFFFFCC80),
            iconTint       = Color(0xFFE65100),
            icon           = when (insight.type) {
                InsightType.BUDGET_FORECAST -> Icons.Default.TrendingUp
                else -> Icons.Default.NotificationsActive
            }
        )
        Severity.POSITIVE -> InsightVisuals(
            containerColor = Color(0xFFE8F5E9),
            borderColor    = Color(0xFFA5D6A7),
            iconTint       = Color(0xFF2E7D32),
            icon           = Icons.Default.CheckCircle
        )
        Severity.INFO -> InsightVisuals(
            containerColor = cs.surfaceContainerLow,
            borderColor    = cs.outlineVariant,
            iconTint       = cs.primary,
            icon           = when (insight.type) {
                InsightType.SAVING_SUGGESTION        -> Icons.Default.Savings
                InsightType.BUDGET_FORECAST          -> Icons.Default.CalendarMonth
                InsightType.CATEGORY_TIP             -> Icons.Default.PieChart
                InsightType.POSITIVE_REINFORCEMENT   -> Icons.Default.Star
                else                                 -> Icons.Default.Lightbulb
            }
        )
    }
}