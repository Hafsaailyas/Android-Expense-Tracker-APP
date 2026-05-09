package com.hafsaIlyas.expensetracker.ui.screens.dashboard

// ui/screens/dashboard/DashboardScreen.kt

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.components.CategoryIcon
import com.hafsaIlyas.expensetracker.ui.components.categoryColor
import com.hafsaIlyas.expensetracker.ui.navigation.Screen
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.components.*
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController : NavController,
    viewModel     : ExpenseViewModel = hiltViewModel()
) {
    val uiState     by viewModel.dashboardUiState.collectAsState()
    var activeChart by remember { mutableStateOf(ChartType.DONUT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = {
                    Column {
                        Text(
                            text       = "Overview",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text  = uiState.currentMonthName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.AddExpense.createRoute())
                    }) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add expense",
                                tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (uiState.currentMonthTotal == 0.0 && uiState.recentExpenses.isEmpty()) {
            DashboardEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddClick = { navController.navigate(Screen.AddExpense.createRoute()) }
            )
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // ── Spending hero card ────────────────────────────────────────
            item {
                SpendingHeaderCard(
                    monthName        = uiState.currentMonthName,
                    currentTotal     = uiState.currentMonthTotal,
                    previousTotal    = uiState.previousMonthTotal,
                    percentageChange = uiState.percentageChange,
                    trendDirection   = uiState.trendDirection,
                )
            }

            // ── Top 3 category quick-cards ────────────────────────────────
            if (uiState.categoryBreakdown.size >= 2) {
                item {
                    Text(
                        text  = "Top Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.categoryBreakdown.take(3)) { share ->
                            TopCategoryCard(share = share)
                        }
                    }
                }
            }

            // ── Chart type toggle ─────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text     = "Category Breakdown",
                        style    = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    ChartType.entries.forEach { type ->
                        FilterChip(
                            selected    = activeChart == type,
                            onClick     = { activeChart = type },
                            label       = { Text(type.label) },
                            leadingIcon = if (activeChart == type) {
                                { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                            } else null,
                        )
                    }
                }
            }

            // ── Chart ─────────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = MaterialTheme.shapes.large,
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    if (uiState.categoryBreakdown.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "No expenses this month",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        AnimatedContent(
                            targetState    = activeChart,
                            label          = "chart_switch",
                            transitionSpec = {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                            },
                        ) { chartType ->
                            when (chartType) {
                                ChartType.DONUT -> {
                                    val formatter =
                                        NumberFormat.getCurrencyInstance(Locale.US)
                                    Row(
                                        modifier              = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment     = Alignment.CenterVertically,
                                    ) {
                                        CategoryDonutChart(
                                            categories  = uiState.categoryBreakdown,
                                            size        = 180.dp,
                                            strokeWidth = 28.dp,
                                            centerLabel = formatter.format(uiState.currentMonthTotal),
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            uiState.categoryBreakdown.take(5)
                                                .forEach { share -> LegendItem(share) }
                                        }
                                    }
                                }
                                ChartType.BAR -> {
                                    Box(Modifier.padding(16.dp)) {
                                        CategoryBarChart(
                                            categories = uiState.categoryBreakdown,
                                            modifier   = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Category filter ───────────────────────────────────────────
            if (uiState.categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        "Filter",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.selectedFilterCategory.isEmpty(),
                                onClick  = { viewModel.onDashboardCategoryFilter("") },
                                label    = { Text("All") },
                            )
                        }
                        items(uiState.categoryBreakdown) { share ->
                            FilterChip(
                                selected = uiState.selectedFilterCategory == share.category,
                                onClick  = { viewModel.onDashboardCategoryFilter(share.category) },
                                label    = { Text(share.category) },
                            )
                        }
                    }
                }
            }

            // ── Recent transactions header ────────────────────────────────
            if (uiState.recentExpenses.isNotEmpty()) {
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Recent Transactions",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        TextButton(onClick = { navController.navigate(Screen.ExpenseList.route) }) {
                            Text("See all")
                        }
                    }
                }

                // ── Staggered fade-in for each row ────────────────────────
                itemsIndexed(uiState.recentExpenses) { index, item ->
                    StaggeredFadeItem(index = index) {
                        RecentExpenseRow(item = item)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Staggered fade wrapper ────────────────────────────────────────────────────

@Composable
private fun StaggeredFadeItem(
    index   : Int,
    content : @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible      = visible,
        enter        = fadeIn(tween(300)) + slideInVertically(tween(300)) { (it * 0.25f).toInt() },
    ) {
        content()
    }
}

// ── Top category horizontal card ──────────────────────────────────────────────

@Composable
private fun TopCategoryCard(share: CategoryShare) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val color     = categoryColor(share.category)

    Card(
        modifier = Modifier.width(140.dp),
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.10f),
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryIcon(
                category = share.category,
                size     = 40.dp,
            )
            Text(
                text  = share.category.drop(
                    share.category.indexOfFirst { it == ' ' }.takeIf { it >= 0 }?.plus(1) ?: 0
                ).trim(),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
            )
            Text(
                text  = formatter.format(share.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            // Thin progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = 0.20f)),
            ) {
                val animPct by animateFloatAsState(
                    targetValue   = (share.percentage / 100f).coerceIn(0f, 1f),
                    animationSpec = tween(700, easing = FastOutSlowInEasing),
                    label         = "cat_pct",
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animPct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(color),
                )
            }
            Text(
                text  = "${share.percentage.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Legend item ───────────────────────────────────────────────────────────────

@Composable
private fun LegendItem(share: CategoryShare) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(share.color)),
        )
        Text(
            text  = "${share.category.take(8)} ${share.percentage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Recent expense row ────────────────────────────────────────────────────────

@Composable
private fun RecentExpenseRow(item: RecentExpenseItem) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val color     = categoryColor(item.category)

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryIcon(category = item.category, size = 40.dp)
            Column {
                Text(
                    text = item.category.drop(
                        item.category.indexOfFirst { it == ' ' }.takeIf { it >= 0 }?.plus(1) ?: 0
                    ).trim(),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    item.formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text       = formatter.format(item.amount),
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color      = color,
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun DashboardEmptyState(
    modifier   : Modifier,
    onAddClick : () -> Unit,
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ) {
            Icon(
                imageVector        = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier
                    .padding(24.dp)
                    .size(64.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "No expenses yet",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap + to record your first expense and\nsee your spending breakdown here.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 48.dp),
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            shape   = MaterialTheme.shapes.medium,
        ) {
            Icon(Icons.Default.Add, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add First Expense")
        }
    }
}

// ── Chart type enum ───────────────────────────────────────────────────────────

private enum class ChartType(val label: String) { DONUT("Donut"), BAR("Bar") }