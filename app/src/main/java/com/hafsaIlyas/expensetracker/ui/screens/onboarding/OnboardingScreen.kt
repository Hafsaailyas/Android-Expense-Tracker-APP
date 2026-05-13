package com.hafsaIlyas.expensetracker.ui.screens.onboarding

// ui/screens/onboarding/OnboardingScreen.kt
//
// Pixel-exact port of the HTML onboarding screen:
//   • 3 slides: "Track every penny" / "AI-powered insights" / "Set budget goals"
//   • Illustration area: tinted gradient bg + accent dots + large centered icon
//   • Content area: 24px/800 title, 14px/regular body
//   • Progress indicators: inactive 8×8 pill → active 24×8 pill, spring animation
//   • CTA button: primary teal → primary→secondary gradient on final slide
//   • "Skip for now" link — hidden on last slide
//
// Dark-mode palette (mirrors AiInsightsScreen / InsightCard / SettingsScreen):
//   bg #0A1112   title #FFFFFF   body #AAAAAA
//   dot-inactive #2A2F3E   skip #666C7A

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ── Brand palette ──────────────────────────────────────────────────────────────
private val ColPrimary   = Color(0xFF1A5F7A)
private val ColSecondary = Color(0xFF2C7865)
private val ColGold      = Color(0xFFF9D56E)

// ── Light-mode tokens ──────────────────────────────────────────────────────────
private val ColBgLight      = Color(0xFFFFFFFF)
private val ColTitleLight   = Color(0xFF1A1F2E)
private val ColBodyLight    = Color(0xFF888888)
private val ColDotL         = Color(0xFFE0E7EF)
private val ColSkipLight    = Color(0xFFAAAAAA)

// ── Dark-mode tokens ───────────────────────────────────────────────────────────
private val ColBgDark       = Color(0xFF0A1112)
private val ColTitleDark    = Color(0xFFFFFFFF)
private val ColBodyDark     = Color(0xFFAAAAAA)
private val ColDotD         = Color(0xFF2A2F3E)
private val ColSkipDark     = Color(0xFF666C7A)

// ── Slide model ────────────────────────────────────────────────────────────────

private data class Slide(
    val icon         : ImageVector,
    val iconTint     : Color,
    val illoGradient : List<Color>,   // light-mode illustration bg
    val title        : String,
    val body         : String,
    // accent dots
    val topDot       : Pair<Color, Int>,   // color, size dp
    val topDotEnd    : Boolean,            // true → offset from end side
    val botDot       : Pair<Color, Int>,
    val botDotEnd    : Boolean
)

private val SLIDES = listOf(
    Slide(
        icon = Icons.Default.Receipt,
        iconTint = ColPrimary,
        illoGradient = listOf(Color(0xFFEEF6FA), Color(0xFFFFFFFF)),
        title = "Track every penny",
        body  = "Log expenses in seconds. Add categories, notes, and dates — all in one tap. No friction, just clarity.",
        topDot = ColGold to 14,      topDotEnd = true,
        botDot = ColSecondary to 9,  botDotEnd = false
    ),
    Slide(
        icon = Icons.Default.AutoAwesome,
        iconTint = ColSecondary,
        illoGradient = listOf(Color(0xFFEEF9F6), Color(0xFFFFFFFF)),
        title = "AI-powered insights",
        body  = "Get personalised advice. Our AI spots spending patterns and gives you actionable guidance every month.",
        topDot = ColPrimary to 12,  topDotEnd = false,
        botDot = ColGold to 10,     botDotEnd = true
    ),
    Slide(
        icon = Icons.Default.TrackChanges,
        iconTint = ColGold,
        illoGradient = listOf(Color(0xFFFFF8E6), Color(0xFFFFFFFF)),
        title = "Set budget goals",
        body  = "Define a monthly budget and track your progress. Stay in control every day, not just at month end.",
        topDot = ColPrimary to 11,   topDotEnd = true,
        botDot = ColSecondary to 8,  botDotEnd = false
    )
)

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val isDark     = !MaterialTheme.colorScheme.background.isBright()
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    val scope      = rememberCoroutineScope()
    val isLast     = pagerState.currentPage == SLIDES.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) ColBgDark else ColBgLight)
    ) {
        // Pager
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            SlideContent(slide = SLIDES[page], isDark = isDark)
        }

        // Controls — indicators + button + skip
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .background(if (isDark) ColBgDark else ColBgLight)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            PagerIndicators(
                count       = SLIDES.size,
                currentPage = pagerState.currentPage,
                isDark      = isDark
            )

            Spacer(Modifier.height(20.dp))

            CtaButton(isLast = isLast) {
                if (isLast) {
                    onFinished()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page          = pagerState.currentPage + 1,
                            animationSpec = tween(380, easing = EaseInOutCubic)
                        )
                    }
                }
            }

            if (!isLast) {
                TextButton(
                    onClick  = onFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text      = "Skip for now",
                        fontSize  = 13.sp,
                        color     = if (isDark) ColSkipDark else ColSkipLight,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Hold height so the layout doesn't jump on last slide
                Spacer(Modifier.height(44.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Slide ──────────────────────────────────────────────────────────────────────

@Composable
private fun SlideContent(slide: Slide, isDark: Boolean) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Illustration area — upper ~55% of slide
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.15f)
                .background(
                    if (isDark)
                        Brush.verticalGradient(listOf(ColBgDark, slide.iconTint.copy(0.09f)))
                    else
                        Brush.verticalGradient(slide.illoGradient)
                )
        ) {
            // Top accent dot
            Box(
                modifier = Modifier
                    .size(slide.topDot.second.dp)
                    .align(if (slide.topDotEnd) Alignment.TopEnd else Alignment.TopStart)
                    .padding(
                        start = if (!slide.topDotEnd) 50.dp else 0.dp,
                        end   = if (slide.topDotEnd) 50.dp else 0.dp,
                        top   = 30.dp
                    )
                    .clip(CircleShape)
                    .background(slide.topDot.first.copy(alpha = if (isDark) 0.35f else 1f))
            )

            // Bottom accent dot
            Box(
                modifier = Modifier
                    .size(slide.botDot.second.dp)
                    .align(if (slide.botDotEnd) Alignment.BottomEnd else Alignment.BottomStart)
                    .padding(
                        start  = if (!slide.botDotEnd) 40.dp else 0.dp,
                        end    = if (slide.botDotEnd) 45.dp else 0.dp,
                        bottom = 38.dp
                    )
                    .clip(CircleShape)
                    .background(slide.botDot.first.copy(alpha = if (isDark) 0.35f else 1f))
            )

            // Central icon circle — .onb-circle
            Box(
                modifier         = Modifier
                    .size(140.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(slide.iconTint.copy(alpha = if (isDark) 0.12f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = slide.icon,
                    contentDescription = null,
                    tint               = if (isDark) slide.iconTint.copy(alpha = 0.9f) else slide.iconTint,
                    modifier           = Modifier.size(64.dp)
                )
            }
        }

        // Text area — lower ~45% of slide
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (isDark) ColBgDark else ColBgLight)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text          = slide.title,
                fontSize      = 24.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp,
                color         = if (isDark) ColTitleDark else ColTitleLight,
                textAlign     = TextAlign.Center,
                lineHeight    = 30.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text       = slide.body,
                fontSize   = 14.sp,
                color      = if (isDark) ColBodyDark else ColBodyLight,
                textAlign  = TextAlign.Center,
                lineHeight = (14 * 1.6f).sp
            )
        }
    }
}

// ── Progress indicators ────────────────────────────────────────────────────────

@Composable
private fun PagerIndicators(count: Int, currentPage: Int, isDark: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val active = i == currentPage

            val width by animateDpAsState(
                targetValue   = if (active) 24.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                ),
                label = "dot_w_$i"
            )
            val color by animateColorAsState(
                targetValue   = if (active) ColPrimary else if (isDark) ColDotD else ColDotL,
                animationSpec = tween(250),
                label         = "dot_c_$i"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )

            if (i < count - 1) Spacer(Modifier.width(6.dp))
        }
    }
}

// ── CTA button ─────────────────────────────────────────────────────────────────

@Composable
private fun CtaButton(isLast: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    if (isLast) listOf(ColPrimary, ColSecondary)
                    else listOf(ColPrimary, ColPrimary)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text       = if (isLast) "Get Started" else "Next",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
            Icon(
                imageVector        = if (isLast) Icons.Default.Check else Icons.Default.ArrowForward,
                contentDescription = null,
                tint               = ColGold,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ── Luminance helper ───────────────────────────────────────────────────────────

private fun Color.isBright(): Boolean =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue) > 0.5f