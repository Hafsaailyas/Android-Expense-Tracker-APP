package com.hafsaIlyas.expensetracker.ui.screens.expenselist

// ui/screens/expenselist/ExpenseListScreen.kt
// Modern expense list — skeleton loading, colorful swipe-delete, polished empty states,
// animated search bar, staggered card entry

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import com.hafsaIlyas.expensetracker.ui.components.ExpenseListSkeleton
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
                message     = "$category expense removed",
                actionLabel = "Undo",
                duration    = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
            else viewModel.commitPendingDelete()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick           = { navController.navigate(Screen.AddExpense.createRoute()) },
                icon              = { Icon(Icons.Default.Add, "Add") },
                text              = { Text("Add Expense") },
                containerColor    = MaterialTheme.colorScheme.primary,
                contentColor      = MaterialTheme.colorScheme.onPrimary,
                expanded          = true
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top header with search + filters ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Expenses",
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
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
                    placeholder   = {
                        Text(
                            "Search expenses…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon  = {
                        Icon(
                            Icons.Default.Search, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = uiState.searchQuery.isNotEmpty(),
                            enter   = scaleIn() + fadeIn(),
                            exit    = scaleOut() + fadeOut()
                        ) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape      = RoundedCornerShape(14.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor   = MaterialTheme.colorScheme.primary
                    )
                )

                // ── Category filter chips ─────────────────────────────────────
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding        = PaddingValues(end = 8.dp)
                ) {
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

            // ── Body ──────────────────────────────────────────────────────────
            when {
                uiState.isLoading -> ExpenseListSkeleton()

                uiState.filteredExpenses.isEmpty() -> EmptyState(
                    isFiltered = uiState.searchQuery.isNotEmpty() || uiState.selectedCategory.isNotEmpty(),
                    modifier   = Modifier.fillMaxSize()
                )

                else -> ExpenseList(
                    expenses = uiState.filteredExpenses,
                    onDelete = viewModel::requestDelete
                )
            }
        }
    }
}

// ── Empty state with illustration ─────────────────────────────────────────────

@Composable
private fun EmptyState(isFiltered: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Illustrated icon container
        Surface(
            modifier = Modifier.size(96.dp),
            shape    = RoundedCornerShape(28.dp),
            color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = if (isFiltered) Icons.Default.SearchOff else Icons.Default.Receipt,
                    contentDescription = null,
                    modifier           = Modifier.size(48.dp),
                    tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text       = if (isFiltered) "No results found" else "No expenses yet!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = if (isFiltered)
                "Try adjusting your search or clearing the filters."
            else
                "Tap the button below to add your very first expense.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ── Expense list with staggered animation ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseList(
    expenses : List<Expense>,
    onDelete : (Expense) -> Unit,
    modifier : Modifier = Modifier
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(expenses, key = { _, e -> e.id }) { idx, expense ->
            StaggeredSwipeCard(index = idx, expense = expense, onDelete = { onDelete(expense) })
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaggeredSwipeCard(index: Int, expense: Expense, onDelete: () -> Unit) {
    // Staggered entry
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible.value = true
    }
    val alpha by animateFloatAsState(
        if (visible.value) 1f else 0f,
        tween(300),
        label = "card_alpha_$index"
    )
    val offsetY by animateFloatAsState(
        if (visible.value) 0f else 16f,
        tween(300, easing = EaseOutCubic),
        label = "card_y_$index"
    )

    Box(
        modifier = Modifier
            .alpha(alpha)
            .offset(y = offsetY.dp)
    ) {
        SwipeToDismissExpenseCard(expense = expense, onDelete = onDelete)
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
            // Colorful swipe background
            val progress = dismissState.progress
            val bgAlpha  = (progress * 2f).coerceIn(0f, 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF5252).copy(alpha = bgAlpha * 0.1f),
                                Color(0xFFFF1744).copy(alpha = bgAlpha * 0.9f)
                            ),
                            startX = Float.POSITIVE_INFINITY,
                            endX   = 0f
                        )
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    modifier            = Modifier.padding(end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, "Delete",
                        tint     = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    ) {
        ExpenseCard(expense)
    }
}

@Composable
private fun ExpenseCard(expense: Expense) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val dateStr   = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date))

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Emoji icon
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(expense.category.take(2), style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.width(14.dp))

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Text(
                formatter.format(expense.amount),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}