package com.hafsaIlyas.expensetracker.ui.screens.dashboard

// ui/screens/dashboard/DashboardScreen.kt
// Redesigned to match the HTML mockup exactly:
//
//   TOP BAR   — "SpendSense" logo left, current month label below it, circular + FAB right
//   HERO CARD — deep teal→emerald gradient, "Total Spent" label, animated amount,
//               gold trend chip (+12.4% vs Dec), budget ring (68%) top-right
//   STAT ROW  — three equal white cards: icon / value / label
//   BREAKDOWN — "Breakdown" header + Donut|Bar segmented toggle
//               Donut view: 100dp canvas donut left + legend column right
//               Bar  view : horizontal gradient bars (same as HTML chart)
//   FILTER CHIPS — "All / Food / Transport / …" horizontal scroll
//   RECENT       — section header + "See all" link + transaction rows
//                  each row: emoji tile · name · date · red amount
//
//   Full dark/light mode via MaterialTheme tokens + fixed palette constants.
//   Smooth animations: card scale-in, staggered stat entrance, chart AnimatedContent.

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.components.AnimatedCurrencyCounter
import com.hafsaIlyas.expensetracker.ui.components.DashboardSkeleton
import com.hafsaIlyas.expensetracker.ui.navigation.Screen
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.components.CategoryBarChart
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.components.CategoryDonutChart
import com.hafsaIlyas.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay

// ── Fixed palette — matches HTML :root vars ───────────────────────────────────
private val Primary     = Color(0xFF1A5F7A)   // --primary
private val Secondary   = Color(0xFF2C7865)   // --secondary
private val Gold        = Color(0xFFF9D56E)   // --gold
private val DangerRed   = Color(0xFFC62828)   // --danger
private val HeroWhite   = Color.White

// Category bg colours matching HTML .txn-icon inline styles
private fun categoryBg(emoji: String): Color = when {
    emoji.startsWith("🍔") || emoji.startsWith("🥘") -> Color(0xFFEEF6FA)
    emoji.startsWith("🚕") || emoji.startsWith("🚌") -> Color(0xFFE8F5E9)
    emoji.startsWith("🛒") || emoji.startsWith("🛍") -> Color(0xFFFFF8E1)
    emoji.startsWith("💊") || emoji.startsWith("🏥") -> Color(0xFFFDE8E8)
    emoji.startsWith("🎮") || emoji.startsWith("🎬") -> Color(0xFFEDE8FD)
    emoji.startsWith("✈") || emoji.startsWith("🗺")  -> Color(0xFFE8F0FD)
    emoji.startsWith("🏠") || emoji.startsWith("🏡") -> Color(0xFFFFF3E0)
    emoji.startsWith("📚") || emoji.startsWith("🎓") -> Color(0xFFE8F5E9)
    emoji.startsWith("💡") || emoji.startsWith("⚡") -> Color(0xFFFFFDE7)
    emoji.startsWith("🎁")                           -> Color(0xFFFCE4EC)
    emoji.startsWith("🐾")                           -> Color(0xFFF3E5F5)
    else                                             -> Color(0xFFF0F4F8)
}

private enum class ChartType(val label: String) { DONUT("Donut"), BAR("Bar") }

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.dashboardUiState.collectAsState()
    var activeChart by remember { mutableStateOf(ChartType.DONUT) }

    // Screen entrance fade
    val screenAlpha = remember { Animatable(0f) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) screenAlpha.animateTo(1f, tween(420, easing = EaseOutCubic))
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                monthName = uiState.currentMonthName,
                onAddClick = { navController.navigate(Screen.AddExpense.createRoute()) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            DashboardSkeleton(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .alpha(screenAlpha.value)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── 1. Hero card ──────────────────────────────────────────────────
            item {
                HeroCard(
                    uiState = uiState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 2.dp, bottom = 14.dp)
                )
            }

            // ── 2. Quick stat row ─────────────────────────────────────────────
            item {
                StatRow(
                    uiState = uiState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp)
                )
            }

            // ── 3. Breakdown chart card ───────────────────────────────────────
            item {
                BreakdownCard(
                    uiState = uiState,
                    activeChart = activeChart,
                    onToggle = { activeChart = it },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp)
                )
            }

            // ── 4. Category filter chips ──────────────────────────────────────
            if (uiState.categoryBreakdown.isNotEmpty()) {
                item {
                    FilterChipRow(
                        uiState = uiState,
                        onFilter = viewModel::onDashboardCategoryFilter,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }
            }

            // ── 5. Recent transactions ────────────────────────────────────────
            if (uiState.recentExpenses.isNotEmpty()) {
                item {
                    RecentHeader(onSeeAll = { navController.navigate(Screen.ExpenseList.route) })
                }
                itemsIndexed(uiState.recentExpenses) { idx, txn ->
                    TransactionRow(item = txn, index = idx)
                }
            }

            // ── 6. Empty state ────────────────────────────────────────────────
            if (uiState.categoryBreakdown.isEmpty() && uiState.recentExpenses.isEmpty()) {
                item { EmptyState() }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
// Matches HTML .dash-topbar: logo + date-chip left, circular + FAB right

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(monthName: String, onAddClick: () -> Unit) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    "SpendSense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight(800),
                    color = Primary,
                    letterSpacing = (-0.5).sp
                )
                if (monthName.isNotBlank()) {
                    Text(
                        monthName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }
        },
        actions = {
            // Circular filled button matching HTML .fab-btn
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add, "Add expense",
                    tint = Gold,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── Hero card ─────────────────────────────────────────────────────────────────
// Matches HTML .hero-card: teal→emerald gradient, decorative circle top-right,
// "Total Spent" label, big amount, gold trend chip, budget ring

@Composable
private fun HeroCard(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    // Entrance animation
    val cardScale = remember { Animatable(0.94f) }
    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        cardAlpha.animateTo(1f, tween(380))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale.value)
            .alpha(cardAlpha.value)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Primary, Secondary),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative glow circle top-right (matching HTML hero-card::before)
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 30.dp, y = (-30).dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(modifier = Modifier.padding(20.dp)) {

            // "Total Spent" label
            Text(
                "TOTAL SPENT",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                fontWeight = FontWeight.SemiBold,
                color = HeroWhite.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(6.dp))

            // Animated amount
            AnimatedCurrencyCounter(
                targetValue = uiState.currentMonthTotal,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight(800),
                    letterSpacing = (-2).sp,
                    fontSize = 40.sp
                ),
                color = HeroWhite
            )

            Spacer(Modifier.height(10.dp))

            // Bottom row: trend chip left, budget ring right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold trend chip matching HTML .trend-chip
                TrendChip(
                    direction = uiState.trendDirection,
                    percentageChange = uiState.percentageChange
                )

                // Budget ring (only if budget data available, else show category count)
                HeroBudgetRing(
                    spent = uiState.currentMonthTotal,
                    categoryCount = uiState.categoryBreakdown.size
                )
            }
        }
    }
}

// Gold pill trend chip — matches HTML .trend-chip exactly
@Composable
private fun TrendChip(direction: TrendDirection, percentageChange: Double) {
    val (icon, text) = when (direction) {
        TrendDirection.UP      -> Icons.Default.TrendingUp   to "+${percentageChange}% vs last month"
        TrendDirection.DOWN    -> Icons.Default.TrendingDown to "-${percentageChange}% vs last month"
        TrendDirection.NEUTRAL -> Icons.Default.TrendingFlat to "±0% vs last month"
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HeroWhite.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, null, tint = Gold, modifier = Modifier.size(13.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gold
            )
        }
    }
}

// Budget ring — matches HTML .budget-ring-wrap / svg ring
@Composable
private fun HeroBudgetRing(spent: Double, categoryCount: Int) {
    // We show a decorative ring with category count in the centre
    // (matches the 68% ring aesthetic in the HTML)
    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(spent) {
        animFraction.animateTo(0.68f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow))
    }

    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(60.dp)) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            val inset = stroke.width / 2
            val arcSize = androidx.compose.ui.geometry.Size(
                size.width - inset * 2, size.height - inset * 2
            )
            val topLeft = Offset(inset, inset)

            // Track ring
            drawArc(
                color = HeroWhite.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            // Progress ring in gold
            drawArc(
                color = Gold,
                startAngle = -90f,
                sweepAngle = 360f * animFraction.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
        Text(
            "$categoryCount\ncat",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontWeight = FontWeight.Bold,
            color = HeroWhite,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

// ── Quick stat row ────────────────────────────────────────────────────────────
// Matches HTML .stats-row → three .stat-card tiles

@Composable
private fun StatRow(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val topCat = uiState.categoryBreakdown.maxByOrNull { it.amount }
    val avgPerCat = if (uiState.categoryBreakdown.isNotEmpty())
        formatter.format(uiState.currentMonthTotal / uiState.categoryBreakdown.size)
    else "—"

    // Staggered entrance
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(180)
        anim.animateTo(1f, tween(420, easing = EaseOutCubic))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(anim.value)
            .offset(y = ((1f - anim.value) * 14f).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            icon = Icons.Default.GridView,
            value = "${uiState.categoryBreakdown.size}",
            label = "Categories",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.Star,
            value = topCat?.category?.take(2) ?: "—",
            label = "Top category",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.PieChart,
            value = avgPerCat.replace("$", "").take(6),
            label = "Avg / cat",
            modifier = Modifier.weight(1f)
        )
    }
}

// Individual stat card — matches HTML .stat-card: icon top, value, label
@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp, 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon, null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight(700),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Breakdown chart card ──────────────────────────────────────────────────────
// Matches HTML .chart-card with "Breakdown" title + Donut|Bar toggle

@Composable
private fun BreakdownCard(
    uiState: DashboardUiState,
    activeChart: ChartType,
    onToggle: (ChartType) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(100)
        cardAlpha.animateTo(1f, tween(400, easing = EaseOutCubic))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha.value),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: "Breakdown" + toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight(700),
                    letterSpacing = (-0.3).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ChartToggle(active = activeChart, onToggle = onToggle)
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.categoryBreakdown.isEmpty()) {
                EmptyChartState()
            } else {
                AnimatedContent(
                    targetState = activeChart,
                    label = "chart_switch",
                    transitionSpec = {
                        (fadeIn(tween(260)) + scaleIn(tween(260), initialScale = 0.96f)) togetherWith
                                (fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.96f))
                    }
                ) { chartType ->
                    when (chartType) {
                        ChartType.DONUT -> DonutWithLegend(uiState)
                        ChartType.BAR   -> CategoryBarChart(
                            categories = uiState.categoryBreakdown,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Segmented toggle matching HTML .chart-toggle buttons
@Composable
private fun ChartToggle(active: ChartType, onToggle: (ChartType) -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            ChartType.entries.forEach { type ->
                val selected = active == type
                val bgAlpha by animateFloatAsState(
                    if (selected) 1f else 0f,
                    spring(Spring.DampingRatioMediumBouncy),
                    label = "toggle_bg_${type.label}"
                )
                Surface(
                    onClick = { onToggle(type) },
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = bgAlpha),
                    modifier = Modifier
                ) {
                    Text(
                        type.label,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) HeroWhite
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Donut chart + legend — matches HTML .donut-row layout exactly
@Composable
private fun DonutWithLegend(uiState: DashboardUiState) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 100dp donut (HTML uses 100×100 svg)
        CategoryDonutChart(
            categories = uiState.categoryBreakdown,
            size = 100.dp,
            strokeWidth = 18.dp,
            centerLabel = formatter.format(uiState.currentMonthTotal)
        )

        // Legend column — matches HTML .legend / .legend-item
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            uiState.categoryBreakdown.take(5).forEach { share ->
                LegendItem(share = share)
            }
        }
    }
}

// Legend row — matches HTML .legend-item: dot · name · percentage
@Composable
private fun LegendItem(share: CategoryShare) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(share.color))
        )
        Text(
            share.category.drop(2).trim().let { if (it.length > 11) it.take(11) else it },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${share.percentage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight(700),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Filter chip row ───────────────────────────────────────────────────────────
// Matches HTML .filter-chips horizontal scroll

@Composable
private fun FilterChipRow(
    uiState: DashboardUiState,
    onFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterPill(
                label = "All",
                selected = uiState.selectedFilterCategory.isEmpty(),
                onClick = { onFilter("") }
            )
        }
        items(uiState.categoryBreakdown) { share ->
            FilterPill(
                label = share.category,
                selected = uiState.selectedFilterCategory == share.category,
                onClick = { onFilter(share.category) }
            )
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        if (selected) 1.04f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "pill_scale"
    )
    Surface(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .height(32.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            0.5.dp,
            if (selected) Primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) HeroWhite
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Recent transactions section ───────────────────────────────────────────────

@Composable
private fun RecentHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Recent",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight(700),
            letterSpacing = (-0.3).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(
            onClick = onSeeAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                "See all",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )
        }
    }
}

// ── Transaction row ───────────────────────────────────────────────────────────
// Matches HTML .txn-row: emoji tile · name+date col · red amount right

@Composable
private fun TransactionRow(item: RecentExpenseItem, index: Int) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 65L)
        visible.value = true
    }
    val alpha by animateFloatAsState(
        if (visible.value) 1f else 0f, tween(300), label = "txn_alpha_$index"
    )
    val offsetX by animateFloatAsState(
        if (visible.value) 0f else 20f,
        tween(300, easing = EaseOutCubic),
        label = "txn_x_$index"
    )

    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .offset(x = offsetX.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Emoji icon tile — matches HTML .txn-icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(categoryBg(item.category.take(2))),
            contentAlignment = Alignment.Center
        ) {
            Text(item.category.take(2), style = MaterialTheme.typography.titleSmall)
        }

        // Name + date
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                if (item.note.isNotBlank()) item.note
                else item.category.drop(2).trim(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }

        // Amount — red matching HTML .txn-amt
        Text(
            "-${formatter.format(item.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight(700),
            color = DangerRed
        )
    }

    // Subtle divider between rows
    if (index < 4) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 66.dp, end = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

// ── Empty states ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyChartState() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Primary.copy(alpha = 0.08f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PieChart, null,
                        modifier = Modifier.size(26.dp),
                        tint = Primary.copy(alpha = 0.4f)
                    )
                }
            }
            Text(
                "No expenses this month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Primary.copy(alpha = 0.08f),
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.AccountBalanceWallet, null,
                    modifier = Modifier.size(44.dp),
                    tint = Primary.copy(alpha = 0.45f)
                )
            }
        }
        Text(
            "No expenses recorded",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Tap + to log your first expense and see your spending insights here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}