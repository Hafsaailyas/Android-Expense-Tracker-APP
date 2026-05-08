package com.hafsaIlyas.expensetracker.ui.screens.dashboard
// ui/screens/dashboard/DashboardScreen.kt

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.navigation.Screen
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.components.*
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.dashboardUiState.collectAsState()
    var activeChart by remember { mutableStateOf(ChartType.DONUT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Dashboard", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddExpense.createRoute()) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add expense")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header card ───────────────────────────────────────────────────
            item {
                SpendingHeaderCard(
                    monthName        = uiState.currentMonthName,
                    currentTotal     = uiState.currentMonthTotal,
                    previousTotal    = uiState.previousMonthTotal,
                    percentageChange = uiState.percentageChange,
                    trendDirection   = uiState.trendDirection
                )
            }

            // ── Chart toggle ──────────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChartType.entries.forEach { type ->
                        FilterChip(
                            selected = activeChart == type,
                            onClick  = { activeChart = type },
                            label    = { Text(type.label) },
                            leadingIcon = if (activeChart == type) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            // ── Chart section ─────────────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = MaterialTheme.shapes.large,
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Category Breakdown",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (uiState.categoryBreakdown.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No expenses this month",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            AnimatedContent(
                                targetState = activeChart,
                                label       = "chart_switch",
                                transitionSpec = {
                                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                                }
                            ) { chartType ->
                                when (chartType) {
                                    ChartType.DONUT -> {
                                        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                                        Row(
                                            modifier            = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment   = Alignment.CenterVertically
                                        ) {
                                            CategoryDonutChart(
                                                categories  = uiState.categoryBreakdown,
                                                size        = 180.dp,
                                                strokeWidth = 28.dp,
                                                centerLabel = formatter.format(uiState.currentMonthTotal)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            // Legend
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                uiState.categoryBreakdown.take(5).forEach { share ->
                                                    LegendItem(share)
                                                }
                                            }
                                        }
                                    }
                                    ChartType.BAR -> {
                                        CategoryBarChart(
                                            categories = uiState.categoryBreakdown,
                                            modifier   = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Category quick-filter ─────────────────────────────────────────
            if (uiState.categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        "Filter by Category",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.selectedFilterCategory.isEmpty(),
                                onClick  = { viewModel.onDashboardCategoryFilter("") },
                                label    = { Text("All") }
                            )
                        }
                        items(uiState.categoryBreakdown) { share ->
                            FilterChip(
                                selected = uiState.selectedFilterCategory == share.category,
                                onClick  = { viewModel.onDashboardCategoryFilter(share.category) },
                                label    = { Text(share.category) }
                            )
                        }
                    }
                }
            }

            // ── Recent transactions ───────────────────────────────────────────
            if (uiState.recentExpenses.isNotEmpty()) {
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Transactions",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = { navController.navigate(Screen.ExpenseList.route) }
                        ) {
                            Text("See all")
                        }
                    }
                }
                items(uiState.recentExpenses) { item ->
                    RecentExpenseRow(item)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Supporting composables ────────────────────────────────────────────────────

@Composable
private fun LegendItem(share: CategoryShare) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape    = MaterialTheme.shapes.extraSmall,
            color    = Color(share.color)
        ) {}
        Text(
            text  = "${share.category.take(8)} ${share.percentage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecentExpenseRow(item: RecentExpenseItem) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(item.category.take(2), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column {
                Text(
                    item.category.drop(2).trim(),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    item.formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            formatter.format(item.amount),
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}

private enum class ChartType(val label: String) {
    DONUT("Donut"),
    BAR("Bar")
}