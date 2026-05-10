package com.hafsaIlyas.expensetracker.ui.screens.aiinsights.components

// ui/screens/aiinsights/components/TypewriterText.kt
// Unchanged — character-by-character animated text reveal

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text        : String,
    modifier    : Modifier  = Modifier,
    style       : TextStyle = LocalTextStyle.current,
    color       : Color     = Color.Unspecified,
    charDelayMs : Long      = 28L
) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        displayedText = ""
        text.forEachIndexed { idx, _ ->
            displayedText = text.substring(0, idx + 1)
            delay(charDelayMs)
        }
    }

    Text(text = displayedText, modifier = modifier, style = style, color = color)
}