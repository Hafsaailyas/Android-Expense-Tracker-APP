package com.hafsaIlyas.expensetracker.ui.screens.splash

// ui/screens/splash/SplashScreen.kt
// All colors sourced from ui/theme/Color.kt — zero hardcoded values

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hafsaIlyas.expensetracker.ui.theme.GoldAccent             // #F9D56E — rings, dots, pill
import com.hafsaIlyas.expensetracker.ui.theme.GradientSplash         // [#0A1112, #0F2030, #1A5F7A]
import com.hafsaIlyas.expensetracker.ui.theme.PrimaryContainerDark   // #00455C — deep teal blob
import com.hafsaIlyas.expensetracker.ui.theme.PrimaryLight           // #1A5F7A — orb shell, rings
import com.hafsaIlyas.expensetracker.ui.theme.SecondaryLight         // #2C7865 — orb shell mid
import com.hafsaIlyas.expensetracker.ui.theme.SurfaceContainerLow_D  // #0F2030 — orb shell dark base
import com.hafsaIlyas.expensetracker.ui.theme.TertiaryLight          // #2C3A6E — bottom-right blob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// ── White alpha helpers — pure overlay values, no theme equivalent ────────────
private val White85 = Color.White.copy(alpha = 0.85f)  // app title
private val White55 = Color.White.copy(alpha = 0.55f)  // tagline — HTML rgba(255,255,255,0.55)
private val White15 = Color.White.copy(alpha = 0.15f)  // version label + glass highlight

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // ── Animatables ───────────────────────────────────────────────────────────
    val logoScale    = remember { Animatable(0f) }
    val logoAlpha    = remember { Animatable(0f) }
    val ring1Scale   = remember { Animatable(0f) }
    val ring2Scale   = remember { Animatable(0f) }
    val ring1Alpha   = remember { Animatable(0f) }
    val ring2Alpha   = remember { Animatable(0f) }
    val titleAlpha   = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(30f) }
    val tagAlpha     = remember { Animatable(0f) }
    val tagOffsetY   = remember { Animatable(20f) }
    val pillAlpha    = remember { Animatable(0f) }
    val dotsAlpha    = remember { Animatable(0f) }
    val exitAlpha    = remember { Animatable(1f) }
    val arcRotation  = remember { Animatable(0f) }
    val arcRotation2 = remember { Animatable(0f) }
    val shimmerX     = remember { Animatable(-200f) }

    LaunchedEffect(Unit) {
        // Continuous arc rotations
        launch {
            arcRotation.animateTo(
                360f,
                infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart)
            )
        }
        launch {
            arcRotation2.animateTo(
                -360f,
                infiniteRepeatable(tween(4800, easing = LinearEasing), RepeatMode.Restart)
            )
        }

        delay(80)

        // Phase 1 — rings expand
        coroutineScope {
            launch { ring1Scale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)) }
            launch { ring1Alpha.animateTo(1f, tween(500)) }
        }
        coroutineScope {
            launch { ring2Scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessVeryLow)) }
            launch { ring2Alpha.animateTo(0.5f, tween(700)) }
        }

        // Phase 2 — logo pops in
        delay(80)
        coroutineScope {
            launch { logoScale.animateTo(1f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)) }
            launch { logoAlpha.animateTo(1f, tween(400)) }
        }

        // Phase 3 — title slides up
        delay(120)
        coroutineScope {
            launch { titleAlpha.animateTo(1f, tween(450, easing = EaseOutCubic)) }
            launch { titleOffsetY.animateTo(0f, tween(450, easing = EaseOutCubic)) }
        }

        // Phase 4 — tagline
        delay(100)
        coroutineScope {
            launch { tagAlpha.animateTo(1f, tween(400, easing = EaseOutCubic)) }
            launch { tagOffsetY.animateTo(0f, tween(400, easing = EaseOutCubic)) }
        }

        // Phase 5 — pill + dots
        delay(80)
        launch { pillAlpha.animateTo(1f, tween(350)) }
        launch { dotsAlpha.animateTo(1f, tween(350)) }

        // Phase 6 — shimmer sweep
        delay(200)
        shimmerX.animateTo(200f, tween(680, easing = EaseInOutCubic))

        // Hold
        delay(750)

        // Phase 7 — fade out
        exitAlpha.animateTo(0f, tween(420, easing = EaseInCubic))
        onSplashComplete()
    }

    // Continuous glow pulse on orb
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.22f,
        targetValue   = 0.52f,
        animationSpec = infiniteRepeatable(tween(1300, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow_alpha"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue  = 0.94f,
        targetValue   = 1.09f,
        animationSpec = infiniteRepeatable(tween(1300, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow_scale"
    )

    // ── Root box ──────────────────────────────────────────────────────────────
    // GradientSplash = [BackgroundDark #0A1112, SurfaceContainerLow_D #0F2030, PrimaryLight #1A5F7A]
    // Matches HTML: linear-gradient(160deg, #0A1112 0%, #0f2030 60%, #1A5F7A 100%)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha.value)
            .background(Brush.verticalGradient(GradientSplash)),
        contentAlignment = Alignment.Center
    ) {

        // ── Background glow blobs ─────────────────────────────────────────────
        // Top-left: deep teal glow — PrimaryContainerDark (#00455C)
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-60).dp, y = (-120).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryContainerDark.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        // Bottom-right: AI navy glow — TertiaryLight (#2C3A6E)
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 80.dp, y = 110.dp)
                .blur(70.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TertiaryLight.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        // ── Rotating dashed arc rings ─────────────────────────────────────────
        // Outer: GoldAccent — HTML .splash-ring border: rgba(249,213,110,0.2)
        ArcRingCanvas(
            rotation    = arcRotation.value,
            ringColor   = GoldAccent.copy(alpha = 0.20f),
            size        = 310.dp,
            strokeWidth = 1.2.dp,
            dashCount   = 20
        )
        // Inner: PrimaryLight teal — second concentric ring
        ArcRingCanvas(
            rotation    = arcRotation2.value,
            ringColor   = PrimaryLight.copy(alpha = 0.18f),
            size        = 260.dp,
            strokeWidth = 1.dp,
            dashCount   = 14
        )

        // ── Outer glow ring (animated scale-in) ───────────────────────────────
        Box(
            modifier = Modifier
                .scale(ring2Scale.value)
                .alpha(ring2Alpha.value)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        // ── Inner glow ring ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .scale(ring1Scale.value)
                .alpha(ring1Alpha.value)
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryLight.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )

        // ── Main content column ───────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // Logo orb
            Box(
                modifier         = Modifier.size(136.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing glow halo — PrimaryLight teal (#1A5F7A)
                Box(
                    modifier = Modifier
                        .scale(glowScale)
                        .size(136.dp)
                        .blur(28.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(PrimaryLight.copy(alpha = glowAlpha), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                // Orbiting particle dots — GoldAccent, HTML .splash-dot #F9D56E
                OrbParticles(
                    modifier = Modifier
                        .size(136.dp)
                        .alpha(ring1Alpha.value * logoAlpha.value),
                    rotation = arcRotation.value
                )

                // Orb shell
                // HTML .splash-logo { background: #1A5F7A } — PrimaryLight at top
                // Gradient fades through SecondaryLight emerald to SurfaceContainerLow_D dark
                Box(
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    PrimaryLight,           // #1A5F7A — HTML logo bg
                                    SecondaryLight,         // #2C7865 — emerald mid-tone
                                    SurfaceContainerLow_D,  // #0F2030 — dark base
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Glass top highlight
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(White15, Color.Transparent),
                                    center = Offset(62f, 24f),
                                    radius = 130f
                                )
                            )
                    )
                    // Shimmer sweep
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.28f),
                                        Color.Transparent
                                    ),
                                    start = Offset(shimmerX.value - 80f, 0f),
                                    end   = Offset(shimmerX.value + 80f, 180f)
                                )
                            )
                    )
                    // Wallet icon — white, HTML logo letter is also white
                    Icon(
                        imageVector        = Icons.Default.AccountBalanceWallet,
                        contentDescription = "SpendSense",
                        modifier           = Modifier.size(48.dp),
                        tint               = Color.White
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // App name — White85, HTML title color: #fff
            Text(
                text     = "SpendSense",
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffsetY.value.dp),
                style    = MaterialTheme.typography.headlineLarge.copy(
                    fontSize      = 38.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp
                ),
                color = White85
            )

            Spacer(Modifier.height(8.dp))

            // Tagline — White55, HTML .splash-tag color: rgba(255,255,255,0.55)
            Text(
                text      = "Smart spending, smarter life",
                modifier  = Modifier
                    .alpha(tagAlpha.value)
                    .offset(y = tagOffsetY.value.dp),
                style     = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.3.sp),
                color     = White55,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // AI-Powered badge pill
            // Pill bg: PrimaryLight teal + GoldAccent tint
            // Dot + text: GoldAccent — HTML .splash-logo-text / .splash-dot color #F9D56E
            Box(
                modifier = Modifier
                    .alpha(pillAlpha.value)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                PrimaryLight.copy(alpha = 0.22f),
                                GoldAccent.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)           // #F9D56E
                    )
                    Text(
                        text  = "AI-Powered  ·  Finance Tracker",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                        color = GoldAccent.copy(alpha = 0.90f) // #F9D56E
                    )
                }
            }
        }

        // ── Bottom loading dots + version ─────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .alpha(dotsAlpha.value)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LoadingDots()
            Spacer(Modifier.height(14.dp))
            Text(
                text  = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = White15
            )
        }
    }
}

// ── Rotating dashed arc ring (Canvas) ────────────────────────────────────────

@Composable
private fun ArcRingCanvas(
    rotation    : Float,
    ringColor   : Color,
    size        : Dp,
    strokeWidth : Dp,
    dashCount   : Int
) {
    Canvas(modifier = Modifier.size(size)) {
        val sw       = strokeWidth.toPx()
        val radius   = (this.size.minDimension / 2f) - sw
        val center   = Offset(this.size.width / 2f, this.size.height / 2f)
        val arcSize  = Size(radius * 2f, radius * 2f)
        val topLeft  = Offset(center.x - radius, center.y - radius)
        val sweepPer = 360f / dashCount
        val dashFrac = 0.62f

        rotate(rotation, pivot = center) {
            repeat(dashCount) { i ->
                drawArc(
                    color      = ringColor,
                    startAngle = i * sweepPer,
                    sweepAngle = sweepPer * dashFrac,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// ── Orbiting particle dots (Canvas) ──────────────────────────────────────────
// GoldAccent (#F9D56E) — matches HTML .splash-dot { background: #F9D56E }

@Composable
private fun OrbParticles(modifier: Modifier = Modifier, rotation: Float) {
    val particles = remember {
        listOf(
            Triple(52f, 4.5f, 0.90f),
            Triple(56f, 3.0f, 0.55f),
            Triple(48f, 3.5f, 0.70f),
            Triple(54f, 2.5f, 0.40f),
        )
    }
    Canvas(modifier = modifier) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        particles.forEachIndexed { i, (orbitR, dotR, alpha) ->
            val angleDeg = rotation + i * 90f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val pos = Offset(
                x = center.x + orbitR * cos(angleRad).toFloat(),
                y = center.y + orbitR * sin(angleRad).toFloat()
            )
            drawCircle(
                color  = GoldAccent.copy(alpha = alpha),  // #F9D56E
                radius = dotR,
                center = pos
            )
        }
    }
}

// ── Wave loading dots ─────────────────────────────────────────────────────────
// GoldAccent (#F9D56E) — matches HTML .splash-dot active/inactive states

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val dotScale by transition.animateFloat(
                initialValue  = 0.5f,
                targetValue   = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(480, delayMillis = i * 160, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$i"
            )
            val dotAlpha by transition.animateFloat(
                initialValue  = 0.40f,  // HTML .splash-dot opacity: 0.4 (inactive)
                targetValue   = 1.0f,   // HTML .splash-dot.active opacity: 1
                animationSpec = infiniteRepeatable(
                    animation  = tween(480, delayMillis = i * 160, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_alpha_$i"
            )
            Box(
                modifier = Modifier
                    .scale(dotScale)
                    .alpha(dotAlpha)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GoldAccent)  // #F9D56E — no extra alpha, driven by dotAlpha above
            )
        }
    }
}