package com.hafsaIlyas.expensetracker.ui.screens.expenselist

// ui/screens/expenselist/ExpenseListScreen.kt
// Redesigned to match HTML "History" screen exactly:
//   • Large "History" heading + white search bar with search icon
//   • Horizontal category filter chips (All, Food, Transport, …)
//   • Expense cards: 44dp icon tile + name/category text + right-aligned amount/date
//   • Swipe-to-delete with red background + trash icon, then in-list undo snackbar
//   • Sticky footer bar: "N expenses · month" on left, total amount on right
//   • Full dark / light mode via MaterialTheme tokens
//   • Staggered card entry animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import com.hafsaIlyas.expensetracker.ui.components.ExpenseListSkeleton
import com.hafsaIlyas.expensetracker.ui.navigation.Screen
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ── Category filter options (mirror HTML chips) ───────────────────────────────

private val FILTER_CATEGORIES = listOf(
    "All",
    "🍔 Food",
    "🚕 Transport",
    "🛒 Shopping",
    "💊 Health",
    "🎮 Fun",
    "✈️ Travel",
    "🏠 Home",
    "📚 Education",
    "💡 Bills",
    "🎁 Gifts",
    "🐾 Pets",
    "➕ Other"
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsState()
    val pendingDelete by viewModel.pendingDeletion.collectAsState()

    // Month label for footer
    val monthLabel = remember {
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
    }

    // Currency formatter
    val currencyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    DisposableEffect(Unit) {
        onDispose { viewModel.commitPendingDelete() }
    }

    // Handle undo via snackbar (standard M3 snackbar host kept for accessibility)
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(pendingDelete) {
        if (pendingDelete != null) {
            val label = pendingDelete!!.category.drop(2).trim()
            val result = snackbarHost.showSnackbar(
                message = "$label expense deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
            else viewModel.commitPendingDelete()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) { data ->
            // Dark pill snackbar matching HTML .undo-snackbar
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    data.visuals.actionLabel?.let { label ->
                        TextButton(onClick = { data.performAction() }) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFF9D56E) // gold accent
                            )
                        }
                    }
                }
            }
        }},
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Top header ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "History" heading
                Text(
                    "History",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight(800),
                    letterSpacing = (-0.8).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Search bar — white card with magnifier icon
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )
            }

            // ── Category filter chips ─────────────────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FILTER_CATEGORIES) { chip ->
                    val isAll = chip == "All"
                    val isSelected = if (isAll) uiState.selectedCategory.isEmpty()
                    else uiState.selectedCategory == chip

                    CategoryFilterChip(
                        label = chip,
                        selected = isSelected,
                        onClick = {
                            viewModel.onListCategoryFilterChange(if (isAll) "" else chip)
                        }
                    )
                }
            }

            // ── Body ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading -> ExpenseListSkeleton()

                    uiState.filteredExpenses.isEmpty() -> EmptyState(
                        isFiltered = uiState.searchQuery.isNotEmpty() ||
                                uiState.selectedCategory.isNotEmpty(),
                        modifier = Modifier.fillMaxSize()
                    )

                    else -> ExpenseList(
                        expenses = uiState.filteredExpenses,
                        onDelete = viewModel::requestDelete
                    )
                }
            }

            // ── Sticky footer ─────────────────────────────────────────────────
            // Matches HTML .list-footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${uiState.filteredExpenses.size} expenses · $monthLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currencyFmt.format(uiState.totalSpending),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight(800),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────
// Matches HTML .search-bar: white rounded container, search icon + grey placeholder

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
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
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Search, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (query.isEmpty()) {
                    Text(
                        "Search expenses…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close, "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ── Category filter chip ──────────────────────────────────────────────────────
// Matches HTML .filter-chip / .filter-chip.active

@Composable
private fun CategoryFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ── Expense list ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseList(
    expenses: List<Expense>,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(expenses, key = { _, e -> e.id }) { idx, expense ->
            StaggeredSwipeCard(index = idx, expense = expense, onDelete = { onDelete(expense) })
        }
        // Extra space for FAB
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Staggered entry + swipe wrapper ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaggeredSwipeCard(index: Int, expense: Expense, onDelete: () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 55L)
        visible.value = true
    }
    val alpha by animateFloatAsState(
        if (visible.value) 1f else 0f,
        tween(280),
        label = "alpha_$index"
    )
    val offsetY by animateFloatAsState(
        if (visible.value) 0f else 14f,
        tween(280, easing = EaseOutCubic),
        label = "offsetY_$index"
    )

    Box(
        modifier = Modifier
            .alpha(alpha)
            .offset(y = offsetY.dp)
    ) {
        SwipeToDismissExpenseCard(expense = expense, onDelete = onDelete)
    }
}

// ── Swipe-to-dismiss expense card ─────────────────────────────────────────────
// Red background + trash icon revealed on swipe-left, matching HTML swipe-bg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissExpenseCard(expense: Expense, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { if (it == EndToStart) { onDelete(); true } else false }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val progress = dismissState.progress
            val bgAlpha = (progress * 2.5f).coerceIn(0f, 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFC62828).copy(alpha = bgAlpha)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete, "Delete",
                    tint = Color.White.copy(alpha = bgAlpha),
                    modifier = Modifier
                        .padding(end = 22.dp)
                        .size(22.dp)
                )
            }
        }
    ) {
        ExpenseCard(expense)
    }
}

// ── Expense card ──────────────────────────────────────────────────────────────
// Matches HTML .expense-card: white surface, 44dp icon tile, name+category, right amount+date

@Composable
private fun ExpenseCard(expense: Expense, modifier: Modifier = Modifier) {
    val currencyFmt = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val dateStr = remember(expense.date) {
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
        val yesterday = today - 86_400_000L
        when {
            expense.date >= today -> "Today"
            expense.date >= yesterday -> "Yesterday"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(expense.date))
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon tile — 44dp, rounded 14dp, tinted bg
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(categoryBgColor(expense.category)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    expense.category.take(2),
                    fontSize = 22.sp
                )
            }

            // Name + category label
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = expense.note.ifBlank { expense.category.drop(2).trim() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = expense.category.drop(2).trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Amount + date (right-aligned)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = currencyFmt.format(expense.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight(800),
                    color = Color(0xFFC62828), // danger red matching HTML .exp-amt
                    fontSize = 16.sp
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ── Category background color mapping ────────────────────────────────────────
// Matches the pastel backgrounds from the HTML (.exp-icon style="background:#EEF6FA" etc.)

@Composable
private fun categoryBgColor(category: String): Color {
    return when {
        category.startsWith("🍔") -> Color(0xFFEEF6FA)
        category.startsWith("🚕") || category.startsWith("🚌") -> Color(0xFFE8F5E9)
        category.startsWith("🛒") || category.startsWith("🛍️") -> Color(0xFFFFF8E1)
        category.startsWith("💊") -> Color(0xFFFDE8E8)
        category.startsWith("🎮") -> Color(0xFFEDE8FD)
        category.startsWith("✈️") -> Color(0xFFE8F0FD)
        category.startsWith("🏠") -> Color(0xFFFFF3E0)
        category.startsWith("📚") -> Color(0xFFE8F5E9)
        category.startsWith("💡") -> Color(0xFFFFFDE7)
        category.startsWith("🎁") -> Color(0xFFFCE4EC)
        category.startsWith("🐾") -> Color(0xFFF3E5F5)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(isFiltered: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isFiltered) Icons.Default.Search else Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (isFiltered) "No results found" else "No expenses yet!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isFiltered)
                "Try adjusting your search or clearing filters."
            else
                "Tap the + button to add your first expense.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}