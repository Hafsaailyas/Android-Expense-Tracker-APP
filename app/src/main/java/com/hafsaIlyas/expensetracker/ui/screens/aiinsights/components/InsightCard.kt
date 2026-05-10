package com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components

// ui/screens/aiinsights/components/InsightCard.kt
// Mirrors .ai-card in HTML exactly:
//   - 5px left colored accent bar (.ai-accent)
//   - Severity colors: CRITICAL #C62828 | WARNING #E65100 | POSITIVE #2E7D32 | INFO #1A5F7A
//   - Light-mode container: white #FFFFFF  |  border: 0.5px rgba(26,95,122,0.1)
//   - Dark-mode container: #1A1F2E  |  same border tinted
//   - CTA row with icon — matches .ai-cta
//   - Staggered entrance animation

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.data.ai.Insight
import com.hafsaIlyas.expensetracker.data.ai.InsightType
import com.hafsaIlyas.expensetracker.data.ai.Severity

// ── Severity accent colors — exact HTML values ────────────────────────────────
private val AccentCritical = Color(0xFFC62828)   // --danger
private val AccentWarning  = Color(0xFFE65100)   // --warning
private val AccentPositive = Color(0xFF2E7D32)   // --success
private val AccentInfo     = Color(0xFF1A5F7A)   // --primary

// ── Light-mode container colors — matches HTML card backgrounds ───────────────
private val ContainerLight           = Color(0xFFFFFFFF)
private val ContainerBorder          = Color(0xFF1A5F7A).copy(alpha = 0.10f)  // rgba(26,95,122,0.1)

// ── Dark-mode container colors ────────────────────────────────────────────────
private val ContainerDark            = Color(0xFF1A1F2E)   // --dark-surface
private val ContainerDarkBorder      = Color(0xFF1A5F7A).copy(alpha = 0.18f)

@Composable
fun InsightCard(
    insight        : Insight,
    animationDelay : Int      = 0,
    modifier       : Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    val isDark    = !MaterialTheme.colorScheme.background.isBright()
    val visuals   = insightVisuals(insight, isDark)

    AnimatedVisibility(
        visible  = visible,
        enter    = fadeIn(tween(350)) + slideInVertically(tween(350, easing = EaseOutCubic)) { it / 2 },
        modifier = modifier
    ) {
        // Outer card shell — .ai-card: white bg + 0.5px border
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) ContainerDark else ContainerLight)
                .border(
                    width = 0.5.dp,
                    color = if (isDark) ContainerDarkBorder else ContainerBorder,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // ── Left accent bar — .ai-accent (5px wide) ──────────────────────
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(visuals.accentColor)
            )

            // ── Card body — .ai-card-body ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Header row — .ai-card-top
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = visuals.icon,
                        contentDescription = null,
                        tint               = visuals.accentColor,
                        modifier           = Modifier.size(18.dp)
                    )
                    Text(
                        text       = insight.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isDark) Color.White else Color(0xFF1A1F2E),
                        lineHeight = 18.sp
                    )
                }

                // Body text — .ai-card-body p
                Text(
                    text       = insight.body,
                    fontSize   = 13.sp,
                    color      = if (isDark) Color(0xFFAAAAAA) else Color(0xFF555555),
                    lineHeight = 19.5.sp
                )

                // CTA button — .ai-cta (only if actionLabel present)
                insight.actionLabel?.let { label ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier              = Modifier.padding(top = 1.dp)
                    ) {
                        Text(
                            text       = label,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = visuals.accentColor
                        )
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint               = visuals.accentColor,
                            modifier           = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Visual config — exact colors from HTML ────────────────────────────────────

private data class InsightVisuals(
    val accentColor : Color,
    val icon        : ImageVector
)

private fun insightVisuals(insight: Insight, isDark: Boolean): InsightVisuals =
    when (insight.severity) {
        Severity.CRITICAL -> InsightVisuals(
            accentColor = AccentCritical,
            icon        = Icons.Default.Warning
        )
        Severity.WARNING  -> InsightVisuals(
            accentColor = AccentWarning,
            icon        = when (insight.type) {
                InsightType.BUDGET_FORECAST -> Icons.Default.TrendingUp
                else                        -> Icons.Default.NotificationsActive
            }
        )
        Severity.POSITIVE -> InsightVisuals(
            accentColor = AccentPositive,
            icon        = Icons.Default.CheckCircle
        )
        Severity.INFO     -> InsightVisuals(
            accentColor = AccentInfo,
            icon        = when (insight.type) {
                InsightType.SAVING_SUGGESTION      -> Icons.Default.Savings
                InsightType.BUDGET_FORECAST        -> Icons.Default.CalendarMonth
                InsightType.CATEGORY_TIP           -> Icons.Default.PieChart
                InsightType.POSITIVE_REINFORCEMENT -> Icons.Default.Star
                else                               -> Icons.Default.Lightbulb
            }
        )
    }

/** True if this color is closer to white (light mode background) */
private fun Color.isBright(): Boolean =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue) > 0.5f