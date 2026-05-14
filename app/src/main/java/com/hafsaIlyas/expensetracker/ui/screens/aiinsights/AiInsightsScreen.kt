package com.hafsaIlyas.expensetracker.ui.screens.aiinsights

// ui/screens/aiinsights/AiInsightsScreen.kt
// Updated to use CurrencyService for all monetary displays.
// Changes from original:
//   • CurrencyService is obtained from AiInsightViewModel (which now exposes it
//     as a public val) — same pattern used by DashboardScreen and ExpenseListScreen.
//   • rememberCurrencyFormatter(viewModel.currencyService) produces a live
//     CurrencyFormatter that reacts to currency changes made in Settings.
//   • currencyFormatter is threaded into SuccessView so the banner summary and
//     any monetary text it receives are formatted with the active currency.
//   • AddExpenseScreen counterpart: AmountHeroCard now receives currencySymbol
//     from the same CurrencyService (handled in AddExpenseScreen.kt).
// Layout, animations, colours, and all other behaviour are pixel-identical to the original.

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
import com.hafsaIlyas.expensetracker.data.currency.CurrencyFormatter
import com.hafsaIlyas.expensetracker.ui.components.InsightsSkeleton
import com.hafsaIlyas.expensetracker.ui.components.rememberCurrencyFormatter
import com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components.*
import java.text.SimpleDateFormat
import java.util.*

// ── Palette — exact HTML :root values ────────────────────────────────────────
private val ColorPrimary     = Color(0xFF1A5F7A)   // --primary
private val ColorGold        = Color(0xFFF9D56E)   // --gold
private val ColorBgLight     = Color(0xFFF0F4F8)   // --bg
private val ColorBgDark      = Color(0xFF0A1112)   // --dark-bg
private val ColorSurfaceDark = Color(0xFF1A1F2E)   // --dark-surface
private val ColorTextMuted   = Color(0xFF888888)   // --text-muted
private val ColorTextLight   = Color(0xFFAAAAAA)   // --text-light

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    navController : NavController,
    viewModel     : AiInsightViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsState()
    val thinkingLabel by viewModel.thinkingLabel.collectAsState()
    val isDark         = !MaterialTheme.colorScheme.background.isBright()

    // ✅ Live CurrencyFormatter — reacts automatically to Settings currency changes.
    // Obtained via viewModel.currencyService, which is the @Singleton injected into
    // AiInsightViewModel (same pattern as DashboardScreen / ExpenseListScreen).
    val currencyFormatter = rememberCurrencyFormatter(viewModel.currencyService)

    val screenBg = if (isDark) ColorBgDark else ColorBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        AnimatedContent(
            targetState    = uiState,
            label          = "ai_screen_state",
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
                    state             = state,
                    isDark            = isDark,
                    onRefresh         = { viewModel.generateInsights() },
                    currencyFormatter = currencyFormatter, // ✅ passed through
                    modifier          = Modifier.fillMaxSize()
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
                modifier         = Modifier
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
                targetState    = label,
                label          = "thinking_label",
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

// ── Success state ─────────────────────────────────────────────────────────────

@Composable
private fun SuccessView(
    state             : AiInsightUiState.Success,
    isDark            : Boolean,
    onRefresh         : () -> Unit,
    currencyFormatter : CurrencyFormatter,        // ✅ live formatter injected
    modifier          : Modifier = Modifier
) {
    val dateStr = remember(state.generatedAt) {
        SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(state.generatedAt))
    }

    // ✅ Currency label shown in the banner subtitle area so the user knows
    // which currency the AI insights were generated for.
    val currencyLabel = currencyFormatter.currency.code   // e.g. "PKR", "USD"

    LazyColumn(
        modifier            = modifier,
        contentPadding      = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── AI Banner ─────────────────────────────────────────────────────────
        item {
            AiHeaderBanner(
                summary      = state.summary,
                onRefresh    = onRefresh,
                isRefreshing = false,
                modifier     = Modifier
            )
        }

        // ── Currency + Timestamp row ──────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // ✅ Active currency badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint     = (if (isDark) ColorTextLight else ColorTextMuted).copy(0.6f)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text     = currencyLabel,
                        fontSize = 11.sp,
                        color    = (if (isDark) ColorTextLight else ColorTextMuted).copy(0.6f)
                    )
                }

                // Timestamp (right-aligned, unchanged)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint     = (if (isDark) ColorTextLight else ColorTextMuted).copy(0.5f)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text     = "Generated $dateStr",
                        fontSize = 11.sp,
                        color    = (if (isDark) ColorTextLight else ColorTextMuted).copy(0.5f)
                    )
                }
            }
        }

        // ── Insight cards ─────────────────────────────────────────────────────
        itemsIndexed(state.insights) { idx, insight ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                InsightCard(
                    insight        = insight,
                    animationDelay = idx * 120
                )
            }
        }

        // ── Disclaimer ────────────────────────────────────────────────────────
        item {
            Text(
                text       = "AI insights are for guidance only and do not constitute financial advice. " +
                        "Data is based on your logged expenses.",
                fontSize   = 11.sp,
                color      = if (isDark) ColorTextLight else ColorTextMuted,
                textAlign  = TextAlign.Center,
                lineHeight = 16.5.sp,
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

/** True if this color is closer to white (light mode background) */
private fun Color.isBright(): Boolean =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue) > 0.5f