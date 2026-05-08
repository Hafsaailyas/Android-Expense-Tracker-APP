package com.hafsaIlyas.expensetracker.ui.screens.expenselist
// ui/screens/expenselist/ExpenseListScreen.kt

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import com.hafsaIlyas.expensetracker.ui.navigation.Screen
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val CATEGORIES = listOf(
    "🍔 Food", "🚌 Transport", "🏠 Rent", "🛍️ Shopping",
    "💊 Health", "🎮 Entertainment", "📚 Education",
    "⚡ Utilities", "✈️ Travel", "📦 Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState       by viewModel.listUiState.collectAsState()
    val pendingDelete by viewModel.pendingDeletion.collectAsState()
    val snackbarHost  = remember { SnackbarHostState() }
    val scope         = rememberCoroutineScope()

    // Flush any pending delete when leaving the screen
    DisposableEffect(Unit) {
        onDispose { viewModel.commitPendingDelete() }
    }

    // Show Snackbar whenever there's a pending deletion
    LaunchedEffect(pendingDelete) {
        if (pendingDelete != null) {
            val category = pendingDelete!!.category.drop(2).trim()
            val result = snackbarHost.showSnackbar(
                message     = "$category expense deleted",
                actionLabel = "Undo",
                duration    = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                // Snackbar timed out or was dismissed — commit the delete
                viewModel.commitPendingDelete()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Expenses",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${uiState.filteredExpenses.size} of ${uiState.allExpenses.size} entries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Search bar ────────────────────────────────────────────────
                OutlinedTextField(
                    value         = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Search by note or category...") },
                    leadingIcon   = { Icon(Icons.Default.Search, null) },
                    trailingIcon  = {
                        AnimatedVisibility(uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape      = MaterialTheme.shapes.extraLarge
                )

                // ── Category filter chips ─────────────────────────────────────
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory.isEmpty(),
                            onClick  = { viewModel.onListCategoryFilterChange("") },
                            label    = { Text("All") }
                        )
                    }
                    items(CATEGORIES) { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick  = { viewModel.onListCategoryFilterChange(cat) },
                            label    = { Text(cat) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.createRoute()) },
                icon    = { Icon(Icons.Default.Add, "Add") },
                text    = { Text("Add Expense") }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.filteredExpenses.isEmpty() -> EmptyState(
                isFiltered = uiState.searchQuery.isNotEmpty() || uiState.selectedCategory.isNotEmpty(),
                modifier   = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )

            else -> ExpenseList(
                expenses  = uiState.filteredExpenses,
                onDelete  = { expense -> viewModel.requestDelete(expense) },
                modifier  = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun EmptyState(isFiltered: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (isFiltered) Icons.Default.SearchOff else Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isFiltered) "No results found" else "No expenses yet!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isFiltered) "Try adjusting your search or filters."
            else "Tap the button below to add your first expense.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 48.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseList(
    expenses: List<Expense>,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            SwipeToDismissExpenseCard(
                expense  = expense,
                onDelete = { onDelete(expense) }
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissExpenseCard(expense: Expense, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { if (it == EndToStart) { onDelete(); true } else false }
    )
    SwipeToDismissBox(
        state                       = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent           = {
            val color by animateColorAsState(
                if (dismissState.targetValue == EndToStart) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                label = "swipe_bg"
            )
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors   = CardDefaults.cardColors(containerColor = color),
                    shape    = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                        Icon(
                            Icons.Default.Delete, "Delete",
                            tint     = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
            }
        }
    ) { ExpenseCard(expense) }
}

@Composable
private fun ExpenseCard(expense: Expense) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val dateStr   = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape    = MaterialTheme.shapes.small,
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(expense.category.take(2), style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    expense.category.drop(2).trim(),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        expense.note,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Text(
                formatter.format(expense.amount),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}