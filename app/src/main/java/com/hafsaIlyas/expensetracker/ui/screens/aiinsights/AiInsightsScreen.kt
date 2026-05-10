package com.hafsaIlyas.expensetracker.ui.screens.aiinsights

// ui/screens/aiinsights/AiInsightsScreen.kt
// Layout mirrors the HTML AI Insights screen exactly:
//   - #screen-ai: background var(--bg) light / #0A1112 dark
//   - .ai-banner at top (no rounding, full-width gradient)
//   - .ai-cards-wrap: scrollable cards with 10dp gap
//   - .ai-disclaimer at bottom
//   - Refresh button is now INSIDE the AiHeaderBanner

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.components.InsightsSkeleton
import com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components.*
import java.text.SimpleDateFormat
import java.util.*

// ── Palette — exact HTML :root values ────────────────────────────────────────
private val ColorPrimary    = Color(0xFF1A5F7A)   // --primary
private val ColorGold       = Color(0xFFF9D56E)   // --gold
private val ColorBgLight    = Color(0xFFF0F4F8)   // --bg
private val ColorBgDark     = Color(0xFF0A1112)   // --dark-bg
private val ColorSurfaceDark= Color(0xFF1A1F2E)   // --dark-surface
private val ColorTextMuted  = Color(0xFF888888)   // --text-muted
private val ColorTextLight  = Color(0xFFAAAAAA)   // --text-light

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    navController : NavController,
    viewModel     : AiInsightViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsState()
    val thinkingLabel by viewModel.thinkingLabel.collectAsState()
    val isDark         = !MaterialTheme.colorScheme.background.isBright()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Get status bar height to apply proper padding
    val statusBarHeight = with(density) {
        configuration.screenHeightDp.dp * 0.05f
    }

    // Screen background — matches --bg (light) / --dark-bg (dark)
    val screenBg = if (isDark) ColorBgDark else ColorBgLight

    // No Scaffold - direct Box for full-screen content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // Main content area
        AnimatedContent(
            targetState   = uiState,
            label         = "ai_screen_state",
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) }
        ) { state ->
            when (state) {
                is AiInsightUiState.Idle    -> Unit
                is AiInsightUiState.Loading -> LoadingView(
                    label    = thinkingLabel,
                    isDark   = isDark,
                    modifier = Modifier.fillMaxSize()
                )
                is AiInsightUiState.Error   -> ErrorView(
                    message  = state.message,
                    onRetry  = viewModel::generateInsights,
                    isDark   = isDark,
                    modifier = Modifier.fillMaxSize()
                )
                is AiInsightUiState.Success -> SuccessView(
                    state    = state,
                    isDark   = isDark,
                    onRefresh = { viewModel.generateInsights() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ── Loading state ─────────────────────────────────────────────────────────────

@Composable
private fun LoadingView(
    label    : String,
    isDark   : Boolean,
    modifier : Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Pulsing orb — mirrors .ai-orb animation
            val pulse = rememberInfiniteTransition(label = "orb")
            val orbScale by pulse.animateFloat(
                initialValue  = 0.9f,
                targetValue   = 1.1f,
                animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse),
                label         = "orb_scale"
            )
            val glowAlpha by pulse.animateFloat(
                initialValue  = 0f,
                targetValue   = 0.12f,
                animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
                label         = "glow_alpha"
            )

            Box(
                modifier = Modifier
                    .scale(orbScale)
                    .size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape    = CircleShape,
                    color    = ColorGold.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint     = ColorGold.copy(alpha = 0.8f + glowAlpha),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                modifier   = Modifier
                    .width(180.dp)
                    .height(3.dp),
                color      = ColorPrimary,
                trackColor = ColorPrimary.copy(alpha = 0.15f)
            )

            AnimatedContent(
                targetState   = label,
                label         = "thinking_label",
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically { -it / 2 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically { it / 2 })
                }
            ) { text ->
                Text(
                    text      = text,
                    fontSize  = 14.sp,
                    color     = if (isDark) ColorTextLight else ColorTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        InsightsSkeleton(
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// ── Error state ───────────────────────────────────────────────────────────────

@Composable
private fun ErrorView(
    message  : String,
    onRetry  : () -> Unit,
    isDark   : Boolean,
    modifier : Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint     = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Couldn't Generate Insights",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            message,
            style     = MaterialTheme.typography.bodySmall,
            color     = if (isDark) ColorTextLight else ColorTextMuted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
        ) {
            Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

// ── Success state — full-screen with banner at top ───────────────────────────

@Composable
private fun SuccessView(
    state    : AiInsightUiState.Success,
    isDark   : Boolean,
    onRefresh: () -> Unit,
    modifier : Modifier = Modifier
) {
    val dateStr = remember(state.generatedAt) {
        SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(state.generatedAt))
    }

    LazyColumn(
        modifier            = modifier,
        contentPadding      = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── AI Banner with Refresh Button Inside ──────────────────────────────
        item {
            AiHeaderBanner(
                summary = state.summary,
                onRefresh = onRefresh,
                isRefreshing = false,
                modifier = Modifier
            )
        }

        // ── Timestamp ─────────────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule, null,
                    modifier = Modifier.size(11.dp),
                    tint     = (if (isDark) ColorTextLight else ColorTextMuted).copy(0.5f)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "Generated $dateStr",
                    fontSize = 11.sp,
                    color    = (if (isDark) ColorTextLight else ColorTextMuted).copy(0.5f)
                )
            }
        }

        // ── Insight cards — .ai-cards-wrap ────────────────────────────────────
        itemsIndexed(state.insights) { idx, insight ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                InsightCard(
                    insight        = insight,
                    animationDelay = idx * 120
                )
            }
        }

        // ── Disclaimer — .ai-disclaimer ───────────────────────────────────────
        item {
            Text(
                text      = "AI insights are for guidance only and do not constitute financial advice. " +
                        "Data is based on your logged expenses.",
                fontSize  = 11.sp,
                color     = if (isDark) ColorTextLight else ColorTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.5.sp,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

/** True if this color is closer to white (light mode background) */
private fun Color.isBright(): Boolean =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue) > 0.5f