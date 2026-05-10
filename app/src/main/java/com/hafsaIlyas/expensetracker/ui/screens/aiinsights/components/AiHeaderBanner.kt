package com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components

// ui/screens/aiinsights/components/AiHeaderBanner.kt
// Matches the HTML AI Insights banner exactly:
//   gradient: #1A5F7A → #2C3A6E  |  orb: gold #F9D56E  |  pulse animation
//   Extended to top edge with safe area padding
//   FULLY RESPONSIVE - adapts to screen size, orientation, and device
//   Includes refresh button in the banner
//   COVERS STATUS BAR - edge-to-edge design
//   INCREASED HEIGHT - banner height increased, text size保持不变

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand palette (matches HTML :root variables) ──────────────────────────────
private val AiBannerGradientStart = Color(0xFF1A5F7A)   // --primary
private val AiBannerGradientEnd   = Color(0xFF2C3A6E)   // matches .ai-banner gradient end
private val AiGold                = Color(0xFFF9D56E)   // --gold
private val AiOrbBg               = Color(0x26F9D56E)   // rgba(249,213,110,0.15)
private val AiOrbBorder           = Color(0x80F9D56E)   // rgba(249,213,110,0.5)

@Composable
fun AiHeaderBanner(
    summary: String?,
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isTablet = screenWidth >= 600.dp

    // Get status bar height to add padding for content (but banner extends under status bar)
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    // Fallback for devices where WindowInsets might not be available
    val safeStatusBarHeight = if (statusBarHeight > 0.dp) {
        statusBarHeight
    } else {
        when {
            isTablet -> 32.dp
            isLandscape -> 16.dp
            screenHeight > 800.dp -> 28.dp
            else -> 24.dp
        }
    }

    // ── Responsive Sizes - Text sizes remain original ──────────────────────────
    // Orb and icon sizes remain the same
    val orbSize = when {
        isTablet -> 56.dp
        isLandscape -> 36.dp
        screenWidth > 400.dp -> 48.dp
        else -> 40.dp
    }

    val iconSize = when {
        isTablet -> 35.dp
        isLandscape -> 25.dp
        screenWidth > 400.dp -> 31.dp
        else -> 27.dp
    }

    val refreshButtonSize = when {
        isTablet -> 49.dp
        isLandscape -> 39.dp
        else -> 43.dp
    }

    val refreshIconSize = when {
        isTablet -> 29.dp
        isLandscape -> 23.dp
        else -> 25.dp
    }

    // ── TEXT SIZES - Keep original sizes (not scaled) ─────────────────────────
    val titleFontSize = when {
        isTablet -> 33.sp
        isLandscape -> 23.sp
        screenWidth > 400.dp -> 29.sp
        else -> 25.sp
    }

    val subtitleFontSize = when {
        isTablet -> 19.sp
        isLandscape -> 15.sp
        else -> 16.sp
    }

    val summaryFontSize = when {
        isTablet -> 17.sp
        isLandscape -> 13.sp
        screenWidth > 400.dp -> 16.sp
        else -> 14.sp
    }

    // ── SPACING - Increased for taller banner (these create the extra height) ──
    val bannerPaddingVertical = when {
        isTablet -> 58.dp  // Increased from 48.dp (50% taller)
        isLandscape -> 34.dp  // Increased from 30.dp (60% taller)
        else -> 50.dp  // Increased from 40.dp (60% taller)
    }

    val bannerPaddingHorizontal = when {
        isTablet -> 32.dp
        screenWidth > 400.dp -> 24.dp
        else -> 16.dp
    }

    val contentSpacing = when {
        isTablet -> 28.dp  // Slightly increased for better proportion
        isLandscape -> 16.dp  // Increased from 12.dp
        else -> 20.dp  // Increased from 16.dp
    }

    val orbTitleSpacing = when {
        isTablet -> 24.dp  // Increased from 20.dp
        isLandscape -> 16.dp  // Increased from 12.dp
        else -> 18.dp  // Increased from 14.dp
    }

    val titleColumnSpacing = when {
        isTablet -> 12.dp  // Increased from 8.dp
        else -> 8.dp  // Increased from 4.dp
    }

// Increased status bar spacer for more height
    val adjustedStatusBarHeight = (safeStatusBarHeight * 1.4f).coerceAtMost(56.dp)  // Increased from 1.3f
    // ── Animation durations ──────────────────────────────────────────────────
    val pulseDuration = if (isTablet) 1300 else 1100
    val glowDuration = if (isTablet) 2500 else 2200

    // ── Orb Pulse Animation ──────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            tween(pulseDuration, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            tween(glowDuration, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Refresh button rotation animation when refreshing
    val rotateAnimation = rememberInfiniteTransition(label = "refresh_rotate")
    val rotation by rotateAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_rotation"
    )

    // ── Responsive Layout - Banner extends to top (covers status bar) ─────────
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AiBannerGradientStart, AiBannerGradientEnd)
                )
            )
            .padding(
                top = 0.dp,
                bottom = bannerPaddingVertical,
                start = bannerPaddingHorizontal,
                end = bannerPaddingHorizontal
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            // ── Status bar spacer (increased for taller banner) ────────────────
            Spacer(modifier = Modifier.height(adjustedStatusBarHeight))

            // ── Top Row: Orb + Title + Refresh Button ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Orb + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(orbTitleSpacing)
                ) {
                    // ── Pulsing Gold Orb (same size) ───────────────────────────
                    Box(
                        modifier = Modifier
                            .scale(orbScale)
                            .size(orbSize)
                            .offset(x = 8.dp, y = (-8).dp)  // 👈 Add x offset for right movement (positive = right, negative = left)
                            .clip(CircleShape)
                            .background(AiOrbBg)
                            .then(
                                Modifier.background(
                                    AiGold.copy(alpha = glowAlpha),
                                    CircleShape
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(orbSize),
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                width = (orbSize / 22).coerceAtLeast(2.dp),
                                color = AiOrbBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "AI Advisor",
                                    tint = AiGold,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }
                    }

                    // ── Title Block (same text sizes) ──────────────────────────
                    Column(
                        verticalArrangement = if (isLandscape) {
                            Arrangement.Center
                        } else {
                            Arrangement.spacedBy(titleColumnSpacing)
                        }
                    ) {
                        Text(
                            text = "AI Insights",
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = titleFontSize * 1.2
                        )
                        if (!isLandscape || screenWidth > 500.dp) {
                            Text(
                                text = "Updated just now",
                                fontSize = subtitleFontSize,
                                color = Color.White.copy(alpha = 0.50f),
                                lineHeight = subtitleFontSize * 1.3
                            )
                        }
                    }
                }

                // ── Refresh Button (Right side) ───────────────────────────────
                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .size(refreshButtonSize)
                        .offset(x = (-20).dp, y = (-16).dp)  // 👈 Add this to move the button

                        .then(
                            if (isRefreshing) {
                                Modifier.graphicsLayer(rotationZ = rotation)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh insights",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(refreshIconSize)
                    )
                }
            }

            // ── Summary Line (same text size) ─────────────────────────────────
            if (summary != null) {
                Text(
                    text = summary,
                    fontSize = summaryFontSize,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Start,  // Changed to Start alignment
                    lineHeight = summaryFontSize * 1.5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (orbSize + orbTitleSpacing)),  // This aligns with title text
                    maxLines = if (isLandscape) 2 else 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}