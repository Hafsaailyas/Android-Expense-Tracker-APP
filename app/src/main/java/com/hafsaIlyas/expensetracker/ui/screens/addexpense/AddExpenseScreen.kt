package com.hafsaIlyas.expensetracker.ui.screens.addexpense

// ui/screens/addexpense/AddExpenseScreen.kt

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel

// ── Constants ─────────────────────────────────────────────────────────────────

private val CATEGORIES = listOf(
    "🍔 Food",
    "🚌 Transport",
    "🏠 Rent",
    "🛍️ Shopping",
    "💊 Health",
    "🎮 Entertainment",
    "📚 Education",
    "⚡ Utilities",
    "✈️ Travel",
    "📦 Other"
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.addUiState.collectAsState()

    // Navigate back once save completes
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.resetAddForm()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Amount ────────────────────────────────────────────────────────
            SectionLabel("Amount")
            OutlinedTextField(
                value          = uiState.amount,
                onValueChange  = viewModel::onAmountChange,
                modifier       = Modifier.fillMaxWidth(),
                placeholder    = { Text("0.00") },
                prefix         = { Text("$  ", fontWeight = FontWeight.Bold) },
                isError        = uiState.amountError != null,
                supportingText = {
                    uiState.amountError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true,
                shape           = MaterialTheme.shapes.medium
            )

            // ── Category chips ────────────────────────────────────────────────
            SectionLabel("Category")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                CATEGORIES.forEach { category ->
                    val selected = uiState.selectedCategory == category
                    FilterChip(
                        selected  = selected,
                        onClick   = { viewModel.onCategoryChange(category) },
                        label     = { Text(category) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Category error
            AnimatedVisibility(visible = uiState.categoryError != null) {
                Text(
                    text  = uiState.categoryError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // ── Note ──────────────────────────────────────────────────────────
            SectionLabel("Note  (optional)")
            OutlinedTextField(
                value         = uiState.note,
                onValueChange = viewModel::onNoteChange,
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder   = { Text("What was this for?") },
                maxLines      = 4,
                shape         = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save Button ───────────────────────────────────────────────────
            Button(
                onClick  = viewModel::saveExpense,
                enabled  = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = MaterialTheme.shapes.medium
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Expense", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}