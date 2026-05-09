package com.hafsaIlyas.expensetracker.ui.screens.expenselist

// ui/screens/expenselist/ExpenseListScreen.kt

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import com.hafsaIlyas.expensetracker.ui.components.CategoryIcon
import com.hafsaIlyas.expensetracker.ui.components.PullToRefreshContainer
import com.hafsaIlyas.expensetracker.ui.components.categoryColor
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
    navController : NavController,
    viewModel     : ExpenseViewModel = hiltViewModel()
) {
    val uiState       by viewModel.listUiState.collectAsState()
    val pendingDelete by viewModel.pendingDeletion.collectAsState()
    val snackbarHost  = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose { viewModel.commitPendingDelete() }
    }

    LaunchedEffect(pendingDelete) {
        if (pendingDelete != null) {
            val category = pendingDelete!!.category.drop(2).trim()
            val result = snackbarHost.showSnackbar(
                message     = "$category expense deleted",
                actionLabel = "Undo",
                duration    = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
            else viewModel.commitPendingDelete()
        }
    }

    val isFiltering = uiState.searchQuery.isNotEmpty() || uiState.selectedCategory.isNotEmpty()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Title row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Transactions",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        AnimatedContent(
                            targetState = uiState.filteredExpenses.size to uiState.allExpenses.size,
                            label       = "count_label",
                        ) { (filtered, total) ->
                            Text(
                                text  = if (isFiltering) "$filtered of $total entries"
                                else "$total entries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    // Active filter badge
                    AnimatedVisibility(visible = isFiltering) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Row(
                                modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    "Filtered",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                // Search bar
                OutlinedTextField(
                    value         = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Search notes or categories…") },
                    leadingIcon   = { Icon(Icons.Default.Search, null) },
                    trailingIcon  = {
                        AnimatedVisibility(uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape      = MaterialTheme.shapes.extraLarge,
                )

                // Category chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory.isEmpty(),
                            onClick  = { viewModel.onListCategoryFilterChange("") },
                            label    = { Text("All") },
                        )
                    }
                    items(CATEGORIES) { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick  = { viewModel.onListCategoryFilterChange(cat) },
                            label    = { Text(cat) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick      = { navController.navigate(Screen.AddExpense.createRoute()) },
                icon         = { Icon(Icons.Default.Add, "Add") },
                text         = { Text("Add Expense") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            uiState.filteredExpenses.isEmpty() -> EmptyState(
                isFiltered = isFiltering,
                onClearFilters = {
                    viewModel.onSearchQueryChange("")
                    viewModel.onListCategoryFilterChange("")
                },
                modifier = Modifier.fillMaxSize().padding(padding),
            )

            else -> PullToRefreshContainer(
                isRefreshing = uiState.isLoading,
                onRefresh    = { /* viewModel.refresh() — wire up if ViewModel supports it */ },
                modifier     = Modifier.padding(padding),
            ) {
                GroupedExpenseList(
                    expenses = uiState.filteredExpenses,
                    onDelete = { viewModel.requestDelete(it) },
                )
            }
        }
    }
}

// ── Grouped list ──────────────────────────────────────────────────────────────

@Composable
private fun GroupedExpenseList(
    expenses : List<Expense>,
    onDelete : (Expense) -> Unit,
) {
    // Group expenses by formatted date header
    val grouped = remember(expenses) {
        expenses.groupBy { expense ->
            val cal     = Calendar.getInstance().apply { timeInMillis = expense.date }
            val today   = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            when {
                isSameDay(cal, today)     -> "Today"
                isSameDay(cal, yesterday) -> "Yesterday"
                else                      -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    .format(Date(expense.date))
            }
        }
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        grouped.forEach { (dateLabel, dayExpenses) ->
            // Date section header
            item(key = "header_$dateLabel") {
                Text(
                    text     = dateLabel,
                    style    = MaterialTheme.typography.labelLarge,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }
            items(dayExpenses, key = { it.id }) { expense ->
                SwipeToDismissExpenseCard(
                    expense  = expense,
                    onDelete = { onDelete(expense) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

private fun isSameDay(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR)        == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

// ── Swipe-to-dismiss wrapper ──────────────────────────────────────────────────

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
            val progress = dismissState.progress
            val bgColor by animateColorAsState(
                targetValue = if (dismissState.targetValue == EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                label = "swipe_bg",
            )
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(bgColor),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(
                    modifier            = Modifier.padding(end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    ) {
        ExpenseCard(expense)
    }
}

// ── Expense card with left accent border ──────────────────────────────────────

@Composable
private fun ExpenseCard(expense: Expense) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val dateStr   = remember(expense.date) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))
    }
    val accentColor = categoryColor(expense.category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        topStart    = 12.dp,
                        bottomStart = 12.dp,
                    )
                )
                .background(accentColor)
                .align(Alignment.CenterVertically)
        )

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CategoryIcon(
                category = expense.category,
                size     = 44.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = expense.category.drop(
                        expense.category.indexOfFirst { it == ' ' }
                            .takeIf { it >= 0 }?.plus(1) ?: 0
                    ).trim(),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        expense.note,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text       = formatter.format(expense.amount),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = accentColor,
            )
        }
    }
}

// ── Empty / filtered state ────────────────────────────────────────────────────

@Composable
private fun EmptyState(
    isFiltered     : Boolean,
    onClearFilters : () -> Unit,
    modifier       : Modifier = Modifier,
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Icon(
                imageVector        = if (isFiltered) Icons.Default.SearchOff
                else Icons.Default.ReceiptLong,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier           = Modifier
                    .padding(20.dp)
                    .size(56.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text       = if (isFiltered) "No results found" else "No expenses yet!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = if (isFiltered)
                "Try adjusting your search or clearing the active filters."
            else "Your transactions will appear here.\nTap the button below to get started.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 40.dp),
        )
        if (isFiltered) {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onClearFilters,
                shape   = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.FilterListOff, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear Filters")
            }
        }
    }
}