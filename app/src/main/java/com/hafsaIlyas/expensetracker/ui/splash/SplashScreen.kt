package com.hafsaIlyas.expensetracker.ui.splash

// ui/splash/SplashScreen.kt

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.ui.theme.SplashBottom
import com.hafsaIlyas.expensetracker.ui.theme.SplashTop
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen splash shown once on cold launch.
 *
 * Animation timeline (total ≈ 1 800 ms):
 *   0 ms        — content starts invisible / scaled down
 *   0–800 ms    — logo + title fade in and scale up  (EaseOutBack easing)
 *   800–1 500 ms— hold
 *   1 500 ms    — [onSplashComplete] callback fires → caller navigates away
 *
 * @param onSplashComplete Lambda invoked when the splash should be dismissed.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // ── Animation state ─────────────────────────────────────────────────
    val logoScale = remember { Animatable(0.72f) }
    val alpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo entrance — 800 ms
        val entranceSpec: AnimationSpec<Float> = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing,
        )

        // Launch alpha and scale animations in parallel using coroutineScope
        coroutineScope {
            listOf(
                launch { alpha.animateTo(1f, entranceSpec) },
                launch { logoScale.animateTo(1f, entranceSpec) },
            ).forEach { it.join() }
        }

        // Staggered subtitle fade (200 ms after logo settles)
        subtitleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = LinearEasing)
        )

        // Hold, then navigate
        delay(500L)
        onSplashComplete()
    }

    // ── UI ────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SplashTop, SplashBottom),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ── Logo icon ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(alpha.value)
                    .background(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(28.dp),
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Expense Tracker logo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── App name ────────────────────────────────────────────────
            Text(
                text = "Expense Tracker",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                modifier = Modifier
                    .alpha(alpha.value)
                    .scale(logoScale.value),
            )

            Spacer(Modifier.height(8.dp))

            // ── Tagline ─────────────────────────────────────────────────
            Text(
                text = "Smart spending. Clear insights.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.75f),
                    letterSpacing = 0.25.sp,
                ),
                modifier = Modifier.alpha(subtitleAlpha.value),
            )
        }

        // ── Loading indicator at bottom ──────────────────────────────────
        LinearProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .alpha(subtitleAlpha.value)
                .padding(horizontal = 48.dp, vertical = 48.dp)
                .height(2.dp),
            color = Color.White.copy(alpha = 0.6f),
            trackColor = Color.White.copy(alpha = 0.2f),
        )
    }
}