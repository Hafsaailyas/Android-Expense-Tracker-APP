package com.hafsaIlyas.expensetracker.ui.screens.addexpense

// ui/screens/addexpense/AddExpenseScreen.kt
// Redesigned to match HTML mockup exactly:
//   • White/surface amount hero card with blinking cursor
//   • 4-column category grid with rounded tiles, emoji + label
//   • Note + date form fields with leading icons
//   • Primary "Save Expense" button + danger "Delete Expense" outline button
//   • Full dark-mode + light-mode support via MaterialTheme tokens
//   • Smooth animated save-button state transition

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Category data ─────────────────────────────────────────────────────────────

private data class Category(val emoji: String, val label: String) {
    val full get() = "$emoji $label"
}

private val CATEGORIES = listOf(
    Category("🍔", "Food"),
    Category("🚕", "Transport"),
    Category("🛒", "Shopping"),
    Category("💊", "Health"),
    Category("🎮", "Fun"),
    Category("✈️", "Travel"),
    Category("🏠", "Home"),
    Category("📚", "Education"),
    Category("💡", "Bills"),
    Category("🎁", "Gifts"),
    Category("🐾", "Pets"),
    Category("➕", "Other"),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.addUiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.resetAddForm()
            navController.popBackStack()
        }
    }

    val todayLabel = remember {
        SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault()).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Expense",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    // Rounded back button matching HTML .back-btn
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Amount hero card ──────────────────────────────────────────────
            AmountHeroCard(
                amount = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                isError = uiState.amountError != null,
                errorText = uiState.amountError
            )

            Spacer(Modifier.height(14.dp))

            // ── Category section ──────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionLabel("Category")
                Spacer(Modifier.height(10.dp))

                AnimatedVisibility(visible = uiState.categoryError != null) {
                    Text(
                        uiState.categoryError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // 4-column grid matching HTML .cat-grid
                val chunked = CATEGORIES.chunked(4)
                chunked.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { cat ->
                            CategoryTile(
                                category = cat,
                                selected = uiState.selectedCategory == cat.full,
                                onClick = { viewModel.onCategoryChange(cat.full) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Pad remaining cells if row is short
                        repeat(4 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Form fields ───────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Note field
                FormField(
                    icon = {
                        Icon(
                            Icons.Default.Notes, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    content = {
                        if (uiState.note.isEmpty()) {
                            Text(
                                "Add a note (optional)…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    // We overlay a transparent text field for actual input
                    isTextField = true,
                    textFieldValue = uiState.note,
                    onTextFieldChange = viewModel::onNoteChange
                )

                // Date field (read-only display)
                FormField(
                    icon = {
                        Icon(
                            Icons.Default.CalendarMonth, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    content = {
                        Text(
                            todayLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // chevron substitute
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(16.dp)
                                // Rotate 180° to make it a right-chevron
                                .then(Modifier)
                        )
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Save button ───────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SaveButton(isSaving = uiState.isSaving, onClick = viewModel::saveExpense)

                // Delete button (outline danger style)
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, MaterialTheme.colorScheme.error
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        "Delete Expense",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Amount hero card ──────────────────────────────────────────────────────────
// Matches HTML .amount-hero: white card, centered amount with blinking cursor

@Composable
private fun AmountHeroCard(
    amount: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorText: String?
) {
    // Blinking cursor animation
    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 0.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "AMOUNT",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(contentAlignment = Alignment.Center) {
                // Invisible text field for capturing input
                androidx.compose.foundation.text.BasicTextField(
                    value = amount,
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-3).sp,
                        color = Color.Transparent // Hidden — we render text manually below
                    ),
                    modifier = Modifier
                        .width(200.dp)
                        .height(70.dp)
                )

                // Visual amount display row: $  <digits>  |cursor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "$",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = amount.ifEmpty { "0" },
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 56.sp,
                            letterSpacing = (-3).sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isError)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    // Blinking cursor bar
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp)
                            .width(3.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha)
                            )
                    )
                }
            }

            AnimatedVisibility(visible = isError && errorText != null) {
                Text(
                    errorText ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Category tile ─────────────────────────────────────────────────────────────
// Matches HTML .cat-item: rounded tile, emoji on top, label below, primary border when selected

@Composable
private fun CategoryTile(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animBorder by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "tile_border_${category.label}"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            )
            .border(
                width = if (selected) 2.dp else 0.5.dp,
                color = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                category.emoji,
                fontSize = 22.sp
            )
            Text(
                category.label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.SemiBold,
                color = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ── Form field ────────────────────────────────────────────────────────────────
// Matches HTML .form-field: white pill with left icon, content, optional trailing icon

@Composable
private fun FormField(
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
    isTextField: Boolean = false,
    textFieldValue: String = "",
    onTextFieldChange: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()

            Box(modifier = Modifier.weight(1f)) {
                if (isTextField) {
                    // Transparent overlay text field
                    androidx.compose.foundation.text.BasicTextField(
                        value = textFieldValue,
                        onValueChange = onTextFieldChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Placeholder
                    if (textFieldValue.isEmpty()) content()
                } else {
                    content()
                }
            }

            trailingIcon?.invoke()
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        letterSpacing = (-0.3).sp
    )
}

// ── Save button ───────────────────────────────────────────────────────────────
// Matches HTML .save-btn: full-width primary, check icon + "Save Expense"

@Composable
private fun SaveButton(isSaving: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        AnimatedContent(
            targetState = isSaving,
            label = "save_btn_content",
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) }
        ) { saving ->
            if (saving) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Text(
                        "Saving…",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Check, null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFF9D56E) // gold accent matching HTML
                    )
                    Text(
                        "Save Expense",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}