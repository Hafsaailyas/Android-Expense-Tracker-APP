package com.hafsaIlyas.expensetracker.ui.screens.dashboard

// ui/screens/dashboard/DashboardScreen.kt
// Ultra-modern fintech dashboard — glassmorphic header, animated segments,
// floating section cards, staggered transaction list, smooth micro-interactions

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
import androidx.compose.ui.graphics.drawscope.DrawScope
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

// ── Palette constants (rich dark teal theme for the hero) ─────────────────────
private val HeroTop    = Color(0xFF042A2E)
private val HeroMid    = Color(0xFF0A4A52)
private val HeroAccent = Color(0xFF0DF2C9)
private val HeroWhite  = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController : NavController,
    viewModel     : ExpenseViewModel = hiltViewModel()
) {
    val uiState    by viewModel.dashboardUiState.collectAsState()
    var activeChart by remember { mutableStateOf(ChartType.DONUT) }

    // Screen-level entrance alpha
    val screenAlpha = remember { Animatable(0f) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            screenAlpha.animateTo(1f, tween(500, easing = EaseOutCubic))
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(onAddClick = { navController.navigate(Screen.AddExpense.createRoute()) })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            DashboardSkeleton(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .alpha(screenAlpha.value)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── 1. Hero spending card ─────────────────────────────────────────
            item {
                HeroSpendingCard(
                    uiState       = uiState,
                    modifier      = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }

            // ── 2. Quick-stat chips row ───────────────────────────────────────
            item {
                QuickStatRow(
                    uiState  = uiState,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(20.dp))
            }

            // ── 3. Category breakdown card ────────────────────────────────────
            item {
                SectionLabel(
                    title    = "Spending Breakdown",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(10.dp))
                ChartCard(
                    uiState     = uiState,
                    activeChart = activeChart,
                    onToggle    = { activeChart = it },
                    modifier    = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(20.dp))
            }

            // ── 4. Category filter chips ──────────────────────────────────────
            if (uiState.categoryBreakdown.isNotEmpty()) {
                item {
                    SectionLabel(
                        title    = "Filter by Category",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    CategoryFilterRow(
                        uiState   = uiState,
                        onFilter  = viewModel::onDashboardCategoryFilter
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── 5. Recent transactions ────────────────────────────────────────
            if (uiState.recentExpenses.isNotEmpty()) {
                item {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        SectionLabel(title = "Recent Transactions")
                        TextButton(onClick = { navController.navigate(Screen.ExpenseList.route) }) {
                            Text(
                                "See all",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                Icons.Default.ChevronRight, null,
                                modifier = Modifier.size(16.dp),
                                tint     = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                itemsIndexed(uiState.recentExpenses) { idx, txn ->
                    TransactionRow(item = txn, index = idx)
                }
            }

            // ── 6. Empty state when no data at all ───────────────────────────
            if (uiState.categoryBreakdown.isEmpty() && uiState.recentExpenses.isEmpty()) {
                item { EmptyDashboardState() }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onAddClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "SpendSense",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Your financial overview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        },
        actions = {
            FilledIconButton(
                onClick = onAddClick,
                colors  = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, "Add expense", tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO SPENDING CARD — gradient + animated amount + trend chip + glow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSpendingCard(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    // Entrance animation
    val cardScale = remember { Animatable(0.92f) }
    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        cardAlpha.animateTo(1f, tween(400))
    }

    // Subtle continuous shimmer
    val infiniteTransition = rememberInfiniteTransition(label = "hero_shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue  = -600f,
        targetValue   = 600f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label         = "shimmer_x"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale.value)
            .alpha(cardAlpha.value)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(HeroTop, HeroMid, Color(0xFF0D6B6B))))
            .drawBehind {
                // Shimmer streak across the card
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerX, 0f),
                        end   = Offset(shimmerX + 300f, size.height)
                    )
                )
                // Top-right decorative glow blob
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(HeroAccent.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(size.width - 20f, -40f),
                        radius = 220f
                    ),
                    radius = 220f,
                    center = Offset(size.width - 20f, -40f)
                )
            }
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // Month label
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth, null,
                    modifier = Modifier.size(13.dp),
                    tint     = HeroWhite.copy(alpha = 0.55f)
                )
                Text(
                    uiState.currentMonthName,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
                    color = HeroWhite.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Animated total amount
            AnimatedCurrencyCounter(
                targetValue = uiState.currentMonthTotal,
                style       = MaterialTheme.typography.displaySmall.copy(
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp,
                    fontSize      = 40.sp
                ),
                color = HeroWhite
            )

            Spacer(Modifier.height(12.dp))

            // Trend chip + last month
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroTrendChip(
                    direction        = uiState.trendDirection,
                    percentageChange = uiState.percentageChange
                )
                if (uiState.previousMonthTotal > 0) {
                    Text(
                        "vs ${formatter.format(uiState.previousMonthTotal)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HeroWhite.copy(alpha = 0.40f)
                    )
                }
            }

            // Accent bottom line
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(HeroAccent.copy(0.5f), Color.Transparent)
                        )
                    )
            )
            Spacer(Modifier.height(16.dp))

            // Bottom row: "total spent" label + teal dot indicator
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(HeroAccent)
                    )
                    Text(
                        "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = HeroWhite.copy(alpha = 0.50f)
                    )
                }
                Text(
                    "${uiState.categoryBreakdown.size} categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = HeroAccent.copy(alpha = 0.80f)
                )
            }
        }
    }
}

@Composable
private fun HeroTrendChip(direction: TrendDirection, percentageChange: Double) {
    val (icon, color, text) = when (direction) {
        TrendDirection.UP      -> Triple(Icons.Default.TrendingUp,   Color(0xFFFF6B6B), "+${percentageChange}%")
        TrendDirection.DOWN    -> Triple(Icons.Default.TrendingDown,  Color(0xFF4CAF50), "-${percentageChange}%")
        TrendDirection.NEUTRAL -> Triple(Icons.Default.TrendingFlat, HeroWhite.copy(.4f), "±0%")
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.18f)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            Text(
                "$text vs last month",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUICK STAT CHIPS ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickStatRow(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val topCategory = uiState.categoryBreakdown.maxByOrNull { it.amount }

    // Stagger entrance
    val entranceAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(200)
        entranceAnim.animateTo(1f, tween(500, easing = EaseOutCubic))
    }

    Row(
        modifier              = modifier
            .fillMaxWidth()
            .alpha(entranceAnim.value)
            .offset(y = ((1f - entranceAnim.value) * 16f).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatChip(
            label  = "Categories",
            value  = "${uiState.categoryBreakdown.size}",
            icon   = Icons.Default.PieChart,
            tint   = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            label  = "Top Spend",
            value  = topCategory?.category?.take(6) ?: "—",
            icon   = Icons.Default.Star,
            tint   = Color(0xFFFFB347),
            modifier = Modifier.weight(1f)
        )
        StatChip(
            label  = "Avg/Cat",
            value  = if (uiState.categoryBreakdown.isNotEmpty())
                formatter.format(uiState.currentMonthTotal / uiState.categoryBreakdown.size)
                    .replace("$", "").take(6) + ""
            else "—",
            icon   = Icons.Default.Analytics,
            tint   = Color(0xFF4DD8DF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    label    : String,
    value    : String,
    icon     : androidx.compose.ui.graphics.vector.ImageVector,
    tint     : Color,
    modifier : Modifier = Modifier
) {
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        color     = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tint.copy(alpha = 0.14f),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                value,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHART CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartCard(
    uiState     : DashboardUiState,
    activeChart : ChartType,
    onToggle    : (ChartType) -> Unit,
    modifier    : Modifier = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(120)
        cardAlpha.animateTo(1f, tween(450, easing = EaseOutCubic))
    }

    Surface(
        modifier       = modifier
            .fillMaxWidth()
            .alpha(cardAlpha.value),
        shape          = RoundedCornerShape(24.dp),
        color          = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Category Breakdown",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Segmented toggle
                ChartToggle(active = activeChart, onToggle = onToggle)
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.categoryBreakdown.isEmpty()) {
                EmptyChartState()
            } else {
                AnimatedContent(
                    targetState   = activeChart,
                    label         = "chart_switch",
                    transitionSpec = {
                        (fadeIn(tween(280)) + scaleIn(tween(280), initialScale = 0.95f)) togetherWith
                                (fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.95f))
                    }
                ) { chartType ->
                    when (chartType) {
                        ChartType.DONUT -> DonutWithLegend(uiState, formatter)
                        ChartType.BAR   -> CategoryBarChart(
                            categories = uiState.categoryBreakdown,
                            modifier   = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartToggle(active: ChartType, onToggle: (ChartType) -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            ChartType.entries.forEach { type ->
                val selected = active == type
                val bgAlpha by animateFloatAsState(
                    if (selected) 1f else 0f,
                    spring(Spring.DampingRatioMediumBouncy),
                    label = "toggle_bg"
                )
                Surface(
                    onClick = { onToggle(type) },
                    shape   = RoundedCornerShape(8.dp),
                    color   = MaterialTheme.colorScheme.primary.copy(alpha = bgAlpha),
                    modifier = Modifier
                ) {
                    Text(
                        type.label,
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutWithLegend(uiState: DashboardUiState, formatter: NumberFormat) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        CategoryDonutChart(
            categories  = uiState.categoryBreakdown,
            size        = 175.dp,
            strokeWidth = 24.dp,
            centerLabel = formatter.format(uiState.currentMonthTotal)
        )
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            uiState.categoryBreakdown.take(5).forEach { share ->
                LegendRow(share = share)
            }
        }
    }
}

@Composable
private fun LegendRow(share: CategoryShare) {
    val barWidth by animateFloatAsState(
        targetValue   = share.percentage / 100f,
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow),
        label         = "legend_bar"
    )
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(share.color))
            )
            Text(
                "${share.category.take(9)}",
                style  = MaterialTheme.typography.labelSmall,
                color  = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                "${share.percentage.toInt()}%",
                style  = MaterialTheme.typography.labelSmall,
                color  = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                fontWeight = FontWeight.SemiBold
            )
        }
        // Mini bar under each legend label
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(share.color).copy(alpha = 0.8f))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY FILTER ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryFilterRow(
    uiState  : DashboardUiState,
    onFilter : (String) -> Unit
) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AnimatedFilterChip(
                selected = uiState.selectedFilterCategory.isEmpty(),
                label    = "All",
                color    = MaterialTheme.colorScheme.primary,
                onClick  = { onFilter("") }
            )
        }
        items(uiState.categoryBreakdown) { share ->
            AnimatedFilterChip(
                selected = uiState.selectedFilterCategory == share.category,
                label    = share.category,
                color    = Color(share.color),
                onClick  = { onFilter(share.category) }
            )
        }
    }
}

@Composable
private fun AnimatedFilterChip(
    selected : Boolean,
    label    : String,
    color    : Color,
    onClick  : () -> Unit
) {
    val scale by animateFloatAsState(
        if (selected) 1.06f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )
    val bgAlpha by animateFloatAsState(
        if (selected) 1f else 0f,
        tween(200),
        label = "chip_bg"
    )

    Surface(
        onClick  = onClick,
        modifier = Modifier.scale(scale),
        shape    = RoundedCornerShape(10.dp),
        color    = color.copy(alpha = if (selected) 0.14f else 0f),
        border   = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) color else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style    = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color    = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TRANSACTION ROW — staggered slide-in
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(item: RecentExpenseItem, index: Int) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible.value = true
    }
    val alpha by animateFloatAsState(
        if (visible.value) 1f else 0f, tween(320), label = "txn_alpha_$index"
    )
    val slideX by animateFloatAsState(
        if (visible.value) 0f else 24f,
        tween(320, easing = EaseOutCubic),
        label = "txn_x_$index"
    )

    val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .offset(x = slideX.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.weight(1f)
        ) {
            // Emoji icon box
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(item.category.take(2), style = MaterialTheme.typography.titleMedium)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.category.drop(2).trim(),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    item.formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.65f)
                )
            }
        }
        // Amount
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatter.format(item.amount),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Subtle divider between rows
    if (index < 4) {
        HorizontalDivider(
            modifier  = Modifier.padding(horizontal = 74.dp),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSurface,
        modifier   = modifier
    )
}

@Composable
private fun EmptyChartState() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = MaterialTheme.colorScheme.primaryContainer.copy(0.3f),
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PieChart, null,
                        modifier = Modifier.size(30.dp),
                        tint     = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                }
            }
            Text(
                "No expenses this month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                "Add your first expense to see the breakdown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
private fun EmptyDashboardState() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape    = RoundedCornerShape(28.dp),
            color    = MaterialTheme.colorScheme.primaryContainer.copy(0.35f),
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.AccountBalanceWallet, null,
                    modifier = Modifier.size(44.dp),
                    tint     = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
        Text(
            "No expenses recorded",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Tap + to log your first expense and see your spending insights here.",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private enum class ChartType(val label: String) { DONUT("Donut"), BAR("Bar") }