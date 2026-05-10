package com.hafsaIlyas.expensetracker.ui.screens.addexpense

// ui/screens/addexpense/AddExpenseScreen.kt
// Polished add-expense screen — gradient amount display, grid category selector, animated save

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel

private val CATEGORIES = listOf(
    "🍔 Food", "🚌 Transport", "🏠 Rent", "🛍️ Shopping",
    "💊 Health", "🎮 Entertainment", "📚 Education",
    "⚡ Utilities", "✈️ Travel", "📦 Other"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    navController : NavController,
    viewModel     : ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.addUiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.resetAddForm()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Expense",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
        ) {

            // ── Hero amount display ───────────────────────────────────────────
            AmountHero(
                amount      = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                isError     = uiState.amountError != null,
                errorText   = uiState.amountError
            )

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Category grid ─────────────────────────────────────────────
                SectionHeader("Category")

                // Error
                AnimatedVisibility(visible = uiState.categoryError != null) {
                    Text(
                        uiState.categoryError ?: "",
                        color    = MaterialTheme.colorScheme.error,
                        style    = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                FlowRow(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIES.forEach { category ->
                        CategoryChip(
                            label    = category,
                            selected = uiState.selectedCategory == category,
                            onClick  = { viewModel.onCategoryChange(category) }
                        )
                    }
                }

                // ── Note field ────────────────────────────────────────────────
                SectionHeader("Note  (optional)")

                OutlinedTextField(
                    value         = uiState.note,
                    onValueChange = viewModel::onNoteChange,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder   = { Text("What was this expense for?") },
                    maxLines      = 4,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor   = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(4.dp))

                // ── Save button ───────────────────────────────────────────────
                SaveButton(
                    isSaving = uiState.isSaving,
                    onClick  = viewModel::saveExpense
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Hero amount section ───────────────────────────────────────────────────────

@Composable
private fun AmountHero(
    amount       : String,
    onValueChange: (String) -> Unit,
    isError      : Boolean,
    errorText    : String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Enter Amount",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Large amount input
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "$",
                    style      = MaterialTheme.typography.displayMedium.copy(fontSize = 36.sp),
                    fontWeight = FontWeight.Light,
                    color      = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(4.dp))
                // Transparent text field visually integrated into the hero
                BasicAmountField(
                    value         = amount,
                    onValueChange = onValueChange,
                    isError       = isError
                )
            }

            // Error text
            AnimatedVisibility(visible = isError && errorText != null) {
                Text(
                    errorText ?: "",
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BasicAmountField(
    value        : String,
    onValueChange: (String) -> Unit,
    isError      : Boolean
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.width(180.dp),
        placeholder   = {
            Text(
                "0.00",
                style      = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                textAlign  = TextAlign.Center
            )
        },
        textStyle     = MaterialTheme.typography.displaySmall.copy(
            fontSize   = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign  = TextAlign.Center,
            color      = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        ),
        isError         = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine      = true,
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor     = Color.Transparent,
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

// ── Category chip ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        if (selected) 1.05f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    Surface(
        onClick  = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (selected) Modifier.border(
                    1.5.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(10.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check, null,
                    modifier = Modifier.size(12.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

// ── Save button ───────────────────────────────────────────────────────────────

@Composable
private fun SaveButton(isSaving: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        if (isSaving) 0.97f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "save_scale"
    )

    Button(
        onClick  = onClick,
        enabled  = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        AnimatedContent(
            targetState   = isSaving,
            label         = "save_btn_state",
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) }
        ) { saving ->
            if (saving) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Text("Saving…", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                    Text(
                        "Save Expense",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}