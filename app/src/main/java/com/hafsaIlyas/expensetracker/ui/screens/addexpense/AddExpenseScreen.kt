package com.hafsaIlyas.expensetracker.ui.screens.addexpense

// ui/screens/addexpense/AddExpenseScreen.kt

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.components.CategoryGridIcon
import com.hafsaIlyas.expensetracker.ui.components.categoryColor
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel

private val CATEGORIES = listOf(
    "🍔 Food",        "🚌 Transport",    "🏠 Rent",
    "🛍️ Shopping",    "💊 Health",       "🎮 Entertainment",
    "📚 Education",   "⚡ Utilities",    "✈️ Travel",
    "📦 Other",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController : NavController,
    viewModel     : ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.addUiState.collectAsState()

    // Navigate back after successful save
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
                        "New Expense",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Large amount input ────────────────────────────────────────
            AmountSection(
                value       = uiState.amount,
                onChange    = viewModel::onAmountChange,
                errorText   = uiState.amountError,
                selectedCat = uiState.selectedCategory,
            )

            Divider(
                modifier  = Modifier.padding(horizontal = 20.dp),
                thickness = 1.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            // ── Category grid ─────────────────────────────────────────────
            CategorySection(
                selected  = uiState.selectedCategory,
                onSelect  = viewModel::onCategoryChange,
                errorText = uiState.categoryError,
            )

            Divider(
                modifier  = Modifier.padding(horizontal = 20.dp),
                thickness = 1.dp,
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            // ── Note field ────────────────────────────────────────────────
            NoteSection(
                value    = uiState.note,
                onChange = viewModel::onNoteChange,
            )

            Spacer(Modifier.height(8.dp))

            // ── Save button ───────────────────────────────────────────────
            SaveButton(
                isSaving = uiState.isSaving,
                isSaved  = uiState.isSaved,
                onClick  = viewModel::saveExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Amount section ────────────────────────────────────────────────────────────

@Composable
private fun AmountSection(
    value       : String,
    onChange    : (String) -> Unit,
    errorText   : String?,
    selectedCat : String,
) {
    val accentColor = if (selectedCat.isNotEmpty()) categoryColor(selectedCat)
    else MaterialTheme.colorScheme.primary

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text  = "Amount",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        // Large currency display row
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // Currency prefix
            Text(
                text       = "$",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = accentColor.copy(alpha = 0.6f),
            )
            Spacer(Modifier.width(4.dp))
            // Transparent large text field
            BasicAmountField(
                value   = value,
                onChange = onChange,
                color   = accentColor,
            )
        }

        AnimatedVisibility(visible = errorText != null) {
            Text(
                text     = errorText ?: "",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun BasicAmountField(
    value    : String,
    onChange : (String) -> Unit,
    color    : Color,
) {
    // We use a styled BasicTextField for maximum flexibility on the amount display
    androidx.compose.foundation.text.BasicTextField(
        value         = value,
        onValueChange = onChange,
        textStyle     = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color      = color,
            textAlign  = TextAlign.Start,
            fontSize   = 48.sp,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine      = true,
        decorationBox   = { innerField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text  = "0.00",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color      = color.copy(alpha = 0.25f),
                            fontSize   = 48.sp,
                        )
                    )
                }
                innerField()
            }
        },
    )
}

// ── Category section ──────────────────────────────────────────────────────────

@Composable
private fun CategorySection(
    selected  : String,
    onSelect  : (String) -> Unit,
    errorText : String?,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text  = "Category",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // 3-column grid
        val rows = CATEGORIES.chunked(3)
        rows.forEach { rowItems ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { cat ->
                    CategoryGridIcon(
                        category = cat,
                        selected = selected == cat,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(cat) },
                    )
                }
                // Fill empty slots in last row
                repeat(3 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        AnimatedVisibility(visible = errorText != null) {
            Text(
                text  = errorText ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ── Note section ──────────────────────────────────────────────────────────────

@Composable
private fun NoteSection(
    value    : String,
    onChange : (String) -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text  = "Note  (optional)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onChange,
            modifier      = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder   = { Text("What was this for?") },
            maxLines      = 4,
            shape         = MaterialTheme.shapes.medium,
        )
    }
}

// ── Morphing save button ──────────────────────────────────────────────────────

@Composable
private fun SaveButton(
    isSaving : Boolean,
    isSaved  : Boolean,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
) {
    // Scale pulse on saved
    val scale by animateFloatAsState(
        targetValue   = if (isSaved) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "save_btn_scale",
    )

    Button(
        onClick  = onClick,
        enabled  = !isSaving && !isSaved,
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        shape    = MaterialTheme.shapes.medium,
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        AnimatedContent(
            targetState    = when {
                isSaving -> SaveState.SAVING
                isSaved  -> SaveState.SAVED
                else     -> SaveState.IDLE
            },
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(150))
            },
            label = "save_state",
        ) { state ->
            when (state) {
                SaveState.SAVING -> Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Text("Saving…", style = MaterialTheme.typography.labelLarge)
                }
                SaveState.SAVED  -> Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                    Text("Saved!", style = MaterialTheme.typography.labelLarge)
                }
                SaveState.IDLE   -> Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(20.dp))
                    Text(
                        "Save Expense",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private enum class SaveState { IDLE, SAVING, SAVED }