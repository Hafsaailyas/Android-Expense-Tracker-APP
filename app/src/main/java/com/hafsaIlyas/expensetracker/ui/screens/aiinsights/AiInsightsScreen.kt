package com.hafsaIlyas.expensetracker.ui.screens.aiinsights

// ui/screens/aiinsights/AiInsightsScreen.kt

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    navController: NavController,
    viewModel: AiInsightViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsState()
    val thinkingLabel by viewModel.thinkingLabel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF7C4DFF)
                        )
                        Text("AI Insights", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // Refresh button (only when not loading)
                    if (uiState !is AiInsightUiState.Loading) {
                        IconButton(onClick = { viewModel.generateInsights() }) {
                            Icon(Icons.Default.Refresh, "Refresh insights")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState   = uiState,
            label         = "ai_screen_state",
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) }
        ) { state ->
            when (state) {
                is AiInsightUiState.Idle    -> Unit // brief — immediately moves to Loading
                is AiInsightUiState.Loading -> LoadingView(thinkingLabel, Modifier.padding(padding))
                is AiInsightUiState.Error   -> ErrorView(
                    message   = state.message,
                    onRetry   = { viewModel.generateInsights() },
                    modifier  = Modifier.padding(padding)
                )
                is AiInsightUiState.Success -> SuccessView(
                    state    = state,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

// ── Loading ────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingView(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")
        val dotAlpha by infiniteTransition.animateFloat(
            initialValue  = 0.2f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label         = "alpha"
        )

        // AI orb — pulsing gradient icon
        Surface(
            shape  = MaterialTheme.shapes.extraLarge,
            color  = Color(0xFF7C4DFF).copy(alpha = 0.12f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint     = Color(0xFF7C4DFF).copy(alpha = dotAlpha),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        LinearProgressIndicator(
            modifier = Modifier.width(200.dp),
            color    = Color(0xFF7C4DFF),
            trackColor = Color(0xFF7C4DFF).copy(alpha = 0.2f)
        )

        Spacer(Modifier.height(16.dp))

        // Animated cycling label
        AnimatedContent(
            targetState   = label,
            label         = "thinking",
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { -it / 2 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically { it / 2 })
            }
        ) { text ->
            Text(
                text      = text,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Error ──────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint     = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text("Couldn't Generate Insights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(message,
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

// ── Success ────────────────────────────────────────────────────────────────────

@Composable
private fun SuccessView(state: AiInsightUiState.Success, modifier: Modifier = Modifier) {
    val dateStr = remember(state.generatedAt) {
        SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            .format(Date(state.generatedAt))
    }

    LazyColumn(
        modifier        = modifier.fillMaxSize(),
        contentPadding  = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Gradient header banner ────────────────────────────────────────────
        item {
            AiHeaderBanner(summary = state.summary)
        }

        // ── Generated-at timestamp ────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Generated $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // ── Section title ─────────────────────────────────────────────────────
        item {
            Text(
                "Your Insights",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // ── Insight cards with staggered entrance ─────────────────────────────
        itemsIndexed(state.insights) { idx, insight ->
            InsightCard(
                insight        = insight,
                animationDelay = idx * 120   // staggered slide-in
            )
        }

        // ── Disclaimer ────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Info,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Text(
                        "Insights are generated from your recorded transactions and are " +
                                "for informational purposes only. Always consult a financial advisor " +
                                "for major financial decisions.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}