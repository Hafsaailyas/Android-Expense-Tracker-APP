package com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components

// ui/screens/aiinsights/components/AiHeaderBanner.kt

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AiHeaderBanner(
    summary: String?,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for the AI orb
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.12f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val gradientColors = listOf(
        Color(0xFF6A11CB),
        Color(0xFF2575FC),
        Color(0xFF00C9FF)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(gradientColors),
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // AI orb
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(64.dp)
                    .background(
                        Brush.radialGradient(listOf(Color.White.copy(0.35f), Color.Transparent)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector       = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint              = Color.White,
                    modifier          = Modifier.size(32.dp)
                )
            }

            Text(
                text       = "AI Spending Advisor",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )

            if (summary != null) {
                Text(
                    text  = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}