package com.example.ui.screens.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.Formatters
import com.example.domain.model.CategoryExpenseSummary
import com.example.domain.model.TransactionType
import com.example.ui.components.SakuCard
import com.example.ui.components.SakuTopBar
import com.example.ui.components.getCategoryIconVector
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuExpenseRedBg
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuGoldLight
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuIncomeGreenBg
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(),
    onNavigateToExport: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header
            item {
                SakuTopBar(title = "Laporan", canNavigateBack = false)
            }

            // 2. Filter Periode Utama
            item {
                PeriodFilterSection(
                    selectedPeriod = uiState.period,
                    periodTitle = uiState.selectedPeriodTitle,
                    onSelectPeriod = { viewModel.selectPeriod(it) },
                    onSelectCustomRange = { start, end -> viewModel.selectCustomRange(start, end) }
                )
            }

            // 3. Ringkasan Pemasukan & Pengeluaran
            item {
                SummarySection(
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    totalTransfer = uiState.totalTransfer,
                    incomeChangePercent = uiState.incomeChangePercent,
                    expenseChangePercent = uiState.expenseChangePercent
                )
            }

            // 4. Grafik Arus Kas (Line Chart)
            item {
                CashflowLineChartSection(points = uiState.cashflowPoints)
            }

            // 5. Pengeluaran Berdasarkan Kategori (Donut Chart)
            item {
                CategoryDonutSection(
                    segments = uiState.donutSegments,
                    categoryBreakdown = uiState.categoryBreakdown,
                    totalExpense = uiState.totalExpense
                )
            }

            // 6. Tren Saldo (Line Chart)
            item {
                BalanceTrendSection(points = uiState.balancePoints)
            }

            // 7. Transaksi Terbesar (Top 5 / 10 / 20)
            item {
                TopTransactionsSection(
                    transactions = uiState.topTransactions,
                    currentFilter = uiState.topTransactionFilter,
                    currentTypeFilter = uiState.transactionTypeFilter,
                    onFilterChange = { viewModel.setTopTransactionFilter(it) },
                    onTypeFilterChange = { viewModel.setTransactionTypeFilter(it) }
                )
            }

            // 8. Tombol Export
            item {
                ExportActionCard(onClick = onNavigateToExport)
            }
        }
    }
}

// ============================================================
// 2. Filter Periode Utama (horizontal chip row)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodFilterSection(
    selectedPeriod: ReportPeriod,
    periodTitle: String,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onSelectCustomRange: (Long, Long) -> Unit
) {
    var showRangePicker by remember { mutableStateOf(false) }

    // Urutan WAJIB: Harian, Mingguan, Bulanan, Tahun Ini, Custom
    val periodOptions = listOf(
        ReportPeriod.TODAY,
        ReportPeriod.THIS_WEEK,
        ReportPeriod.THIS_MONTH,
        ReportPeriod.THIS_YEAR,
        ReportPeriod.CUSTOM
    )

    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header label + periode aktif
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SakuLightGreen, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Filter Periode",
                            style = MaterialTheme.typography.labelSmall,
                            color = SakuTextMuted
                        )
                        Text(
                            text = periodTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SakuDarkGreen
                        )
                    }
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(periodOptions.size) { index ->
                    val period = periodOptions[index]
                    val isSelected = selectedPeriod == period

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (period == ReportPeriod.CUSTOM) {
                                showRangePicker = true
                            } else {
                                onSelectPeriod(period)
                            }
                        },
                        label = {
                            Text(
                                text = when (period) {
                                    ReportPeriod.TODAY -> "Harian"
                                    ReportPeriod.THIS_WEEK -> "Mingguan"
                                    ReportPeriod.THIS_MONTH -> "Bulanan"
                                    ReportPeriod.THIS_YEAR -> "Tahun Ini"
                                    ReportPeriod.CUSTOM -> "Custom"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SakuDarkGreen,
                            selectedLabelColor = Color.White,
                            containerColor = SakuCreamBackground,
                            labelColor = SakuTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SakuCreamBorder,
                            selectedBorderColor = SakuDarkGreen,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }
    }

    if (showRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            onSelectCustomRange(start, end)
                        }
                        showRangePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SakuDarkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) {
                    Text("Batal", color = SakuTextMuted)
                }
            },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = SakuCreamSurface
            )
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Pilih Rentang Tanggal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SakuDarkGreen,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Laporan Periode",
                        style = MaterialTheme.typography.bodySmall,
                        color = SakuTextMuted,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================
// 3. Ringkasan Pemasukan & Pengeluaran
// ============================================================
@Composable
fun SummarySection(
    totalIncome: Double,
    totalExpense: Double,
    totalTransfer: Double,
    incomeChangePercent: Double,
    expenseChangePercent: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryMetricCard(
            modifier = Modifier.weight(1f),
            title = "Pemasukan",
            amount = totalIncome,
            changePercent = incomeChangePercent,
            amountColor = SakuIncomeGreen,
            bgTint = SakuIncomeGreenBg,
            isIncome = true
        )

        SummaryMetricCard(
            modifier = Modifier.weight(1f),
            title = "Pengeluaran",
            amount = totalExpense,
            changePercent = expenseChangePercent,
            amountColor = SakuExpenseRed,
            bgTint = SakuExpenseRedBg,
            isIncome = false
        )

        SummaryMetricCard(
            modifier = Modifier.weight(1f),
            title = "Transfer",
            amount = totalTransfer,
            changePercent = 0.0,
            amountColor = SakuGoldAccent,
            bgTint = SakuGoldLight,
            isIncome = null
        )
    }
}

@Composable
fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    changePercent: Double,
    amountColor: Color,
    bgTint: Color,
    isIncome: Boolean?
) {
    SakuCard(
        modifier = modifier.height(130.dp),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SakuTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(bgTint, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isIncome != null) {
                        Icon(
                            imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = amountColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        // Transfer: swap icon (neutral arrow)
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = amountColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = Formatters.formatRupiah(amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SakuTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isIncome != null) {
                    // Income/Expense: show trend
                    if (changePercent != 0.0) {
                        val sign = if (changePercent > 0) "+" else ""
                        val percentText = "$sign%.1f%% vs lalu".format(changePercent)
                        Text(
                            text = percentText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncome) {
                                if (changePercent >= 0) SakuIncomeGreen else SakuExpenseRed
                            } else {
                                if (changePercent <= 0) SakuIncomeGreen else SakuExpenseRed
                            }
                        )
                    } else {
                        Text(
                            text = "Stabil vs periode lalu",
                            style = MaterialTheme.typography.labelSmall,
                            color = SakuTextMuted
                        )
                    }
                } else {
                    // Transfer: no trend, just neutral label
                    Text(
                        text = "Perpindahan aset",
                        style = MaterialTheme.typography.labelSmall,
                        color = SakuTextMuted
                    )
                }
            }
        }
    }
}

// ============================================================
// 4. Grafik Arus Kas (Line Chart)
// ============================================================
@Composable
fun CashflowLineChartSection(points: List<CashflowPoint>) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arus Kas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SakuDarkGreen
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChartLegend(color = SakuIncomeGreen, label = "Pemasukan")
                    ChartLegend(color = SakuExpenseRed, label = "Pengeluaran")
                }
            }

            if (points.isEmpty()) {
                EmptyChartPlaceholder(message = "Belum ada transaksi di periode ini")
            } else {
                // Tooltip indicator
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SakuCreamBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pt.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SakuDarkGreen
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "+${Formatters.formatRupiah(pt.income)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = SakuIncomeGreen
                                    )
                                    Text(
                                        text = "-${Formatters.formatRupiah(pt.expense)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = SakuExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }

                // Line Chart Canvas
                val maxVal = max(1.0, points.maxOf { max(it.income, it.expense) })
                val pointsCount = points.size

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    val paddingHorizontal = 16.dp.toPx()
                                    val availableWidth = size.width - (paddingHorizontal * 2)
                                    if (pointsCount > 1) {
                                        val step = availableWidth / (pointsCount - 1)
                                        val touchedIndex = ((offset.x - paddingHorizontal + (step / 2)) / step).toInt()
                                        selectedIndex = touchedIndex.coerceIn(0, pointsCount - 1)
                                    } else {
                                        selectedIndex = 0
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val padH = 16.dp.toPx()
                        val padV = 20.dp.toPx()
                        val chartW = width - (padH * 2)
                        val chartH = height - (padV * 2)

                        // 3 Horizontal guideline lines
                        for (i in 0..2) {
                            val y = padV + (chartH * (i / 2f))
                            drawLine(
                                color = SakuCreamBorder.copy(alpha = 0.6f),
                                start = Offset(padH, y),
                                end = Offset(width - padH, y),
                                strokeWidth = 1f
                            )
                        }

                        if (pointsCount == 1) {
                            // Single point
                            val cx = width / 2f
                            val cyIncome = padV + chartH * (1f - (points[0].income / maxVal).toFloat())
                            val cyExpense = padV + chartH * (1f - (points[0].expense / maxVal).toFloat())

                            drawCircle(color = SakuIncomeGreen, radius = 5.dp.toPx(), center = Offset(cx, cyIncome))
                            drawCircle(color = SakuExpenseRed, radius = 5.dp.toPx(), center = Offset(cx, cyExpense))
                        } else {
                            val incomePath = Path()
                            val expensePath = Path()

                            points.forEachIndexed { i, pt ->
                                val x = padH + (i.toFloat() / (pointsCount - 1)) * chartW
                                val yInc = padV + chartH * (1f - (pt.income / maxVal).toFloat().coerceIn(0f, 1f))
                                val yExp = padV + chartH * (1f - (pt.expense / maxVal).toFloat().coerceIn(0f, 1f))

                                if (i == 0) {
                                    incomePath.moveTo(x, yInc)
                                    expensePath.moveTo(x, yExp)
                                } else {
                                    incomePath.lineTo(x, yInc)
                                    expensePath.lineTo(x, yExp)
                                }
                            }

                            // Draw lines
                            drawPath(
                                path = incomePath,
                                color = SakuIncomeGreen,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = expensePath,
                                color = SakuExpenseRed,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw point circles
                            points.forEachIndexed { i, pt ->
                                val x = padH + (i.toFloat() / (pointsCount - 1)) * chartW
                                val yInc = padV + chartH * (1f - (pt.income / maxVal).toFloat().coerceIn(0f, 1f))
                                val yExp = padV + chartH * (1f - (pt.expense / maxVal).toFloat().coerceIn(0f, 1f))

                                val isSel = selectedIndex == i
                                val radius = if (isSel) 6.dp.toPx() else 3.5.dp.toPx()

                                drawCircle(color = SakuIncomeGreen, radius = radius, center = Offset(x, yInc))
                                drawCircle(color = Color.White, radius = radius / 2f, center = Offset(x, yInc))

                                drawCircle(color = SakuExpenseRed, radius = radius, center = Offset(x, yExp))
                                drawCircle(color = Color.White, radius = radius / 2f, center = Offset(x, yExp))
                            }
                        }
                    }
                }

                // X-Axis labels (start, middle, end)
                if (points.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = points.first().label,
                            style = MaterialTheme.typography.labelSmall,
                            color = SakuTextMuted
                        )
                        if (points.size > 2) {
                            Text(
                                text = points[points.size / 2].label,
                                style = MaterialTheme.typography.labelSmall,
                                color = SakuTextMuted
                            )
                        }
                        Text(
                            text = points.last().label,
                            style = MaterialTheme.typography.labelSmall,
                            color = SakuTextMuted
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 5. Pengeluaran Berdasarkan Kategori (Donut Chart)
// ============================================================
@Composable
fun CategoryDonutSection(
    segments: List<DonutCategorySegment>,
    categoryBreakdown: List<CategoryExpenseSummary>,
    totalExpense: Double
) {
    var showAllCategories by remember { mutableStateOf(false) }

    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pengeluaran Berdasarkan Kategori",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SakuDarkGreen
            )

            if (segments.isEmpty() || totalExpense <= 0) {
                EmptyChartPlaceholder(message = "Belum ada pengeluaran pada periode ini")
            } else {
                // Donut Visual + Center Text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 28.dp.toPx()
                        val arcRadius = (size.minDimension - strokeWidth) / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)
                        val arcSize = Size(arcRadius * 2, arcRadius * 2)

                        var startAngle = -90f
                        segments.forEach { seg ->
                            val sweepAngle = (seg.percentage * 360f).coerceAtLeast(1.5f)
                            val color = try {
                                Color(android.graphics.Color.parseColor(seg.colorHex))
                            } catch (e: Exception) {
                                SakuDarkGreen
                            }

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 1f, // small gap
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Donut center label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total Keluar",
                            style = MaterialTheme.typography.labelSmall,
                            color = SakuTextMuted
                        )
                        Text(
                            text = Formatters.formatRupiah(totalExpense),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = SakuTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Category List Items (Top 5 default, expand for all)
                val displayList = if (showAllCategories) categoryBreakdown else categoryBreakdown.take(5)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayList.forEachIndexed { idx, cat ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(CATEGORY_CHART_COLORS.getOrElse(idx) { "#133E35" }))
                        } catch (e: Exception) {
                            SakuDarkGreen
                        }
                        CategoryProgressRow(
                            name = cat.categoryName,
                            icon = cat.categoryIcon,
                            amount = cat.totalAmount,
                            percentage = cat.percentage,
                            indicatorColor = color
                        )
                    }
                }

                // Toggle "Lihat Semua / Sembunyikan"
                if (categoryBreakdown.size > 5) {
                    TextButton(
                        onClick = { showAllCategories = !showAllCategories },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (showAllCategories) "Sembunyikan" else "Lihat Semua (${categoryBreakdown.size} Kategori)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SakuDarkGreen
                        )
                        Icon(
                            imageVector = if (showAllCategories) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    name: String,
    icon: String,
    amount: Double,
    percentage: Float,
    indicatorColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(indicatorColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = getCategoryIconVector(icon),
                    contentDescription = null,
                    tint = SakuDarkGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SakuTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Formatters.formatRupiah(amount),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SakuTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SakuTextMuted,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Linear Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(SakuCreamBorder, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(indicatorColor, RoundedCornerShape(2.dp))
            )
        }
    }
}

// ============================================================
// 6. Tren Saldo (Line Chart)
// ============================================================
@Composable
fun BalanceTrendSection(points: List<BalancePoint>) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tren Saldo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SakuDarkGreen
                )
                ChartLegend(color = SakuGoldAccent, label = "Total Saldo")
            }

            if (points.isEmpty()) {
                EmptyChartPlaceholder(message = "Belum ada histori saldo")
            } else {
                // Tooltip indicator
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SakuCreamBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pt.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SakuDarkGreen
                                )
                                Text(
                                    text = Formatters.formatRupiah(pt.balance),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SakuGoldAccent
                                )
                            }
                        }
                    }
                }

                // Balance Line Chart Canvas
                val minVal = points.minOf { it.balance }
                val maxVal = points.maxOf { it.balance }
                val range = max(1.0, maxVal - minVal)
                val pointsCount = points.size

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    val padH = 16.dp.toPx()
                                    val availableW = size.width - (padH * 2)
                                    if (pointsCount > 1) {
                                        val step = availableW / (pointsCount - 1)
                                        val touched = ((offset.x - padH + (step / 2)) / step).toInt()
                                        selectedIndex = touched.coerceIn(0, pointsCount - 1)
                                    } else {
                                        selectedIndex = 0
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val padH = 16.dp.toPx()
                        val padV = 16.dp.toPx()
                        val chartW = width - (padH * 2)
                        val chartH = height - (padV * 2)

                        // 3 horizontal guidelines
                        for (i in 0..2) {
                            val y = padV + (chartH * (i / 2f))
                            drawLine(
                                color = SakuCreamBorder.copy(alpha = 0.6f),
                                start = Offset(padH, y),
                                end = Offset(width - padH, y),
                                strokeWidth = 1f
                            )
                        }

                        if (pointsCount == 1) {
                            val cx = width / 2f
                            val cy = padV + (chartH / 2f)
                            drawCircle(color = SakuGoldAccent, radius = 5.dp.toPx(), center = Offset(cx, cy))
                        } else {
                            val linePath = Path()
                            val fillPath = Path()

                            points.forEachIndexed { i, pt ->
                                val x = padH + (i.toFloat() / (pointsCount - 1)) * chartW
                                val y = padV + chartH * (1f - ((pt.balance - minVal) / range).toFloat().coerceIn(0f, 1f))

                                if (i == 0) {
                                    linePath.moveTo(x, y)
                                    fillPath.moveTo(x, padV + chartH)
                                    fillPath.lineTo(x, y)
                                } else {
                                    linePath.lineTo(x, y)
                                    fillPath.lineTo(x, y)
                                }

                                if (i == pointsCount - 1) {
                                    fillPath.lineTo(x, padV + chartH)
                                    fillPath.close()
                                }
                            }

                            // Gradient-like area fill
                            drawPath(
                                path = fillPath,
                                color = SakuGoldAccent.copy(alpha = 0.12f),
                                style = Fill
                            )

                            // Line path
                            drawPath(
                                path = linePath,
                                color = SakuGoldAccent,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Point circles
                            points.forEachIndexed { i, pt ->
                                val x = padH + (i.toFloat() / (pointsCount - 1)) * chartW
                                val y = padV + chartH * (1f - ((pt.balance - minVal) / range).toFloat().coerceIn(0f, 1f))
                                val isSel = selectedIndex == i
                                val radius = if (isSel) 6.dp.toPx() else 3.5.dp.toPx()

                                drawCircle(color = SakuGoldAccent, radius = radius, center = Offset(x, y))
                                drawCircle(color = Color.White, radius = radius / 2f, center = Offset(x, y))
                            }
                        }
                    }
                }

                // X-Axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = points.first().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SakuTextMuted
                    )
                    Text(
                        text = points.last().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SakuTextMuted
                    )
                }
            }
        }
    }
}

// ============================================================
// 7. Transaksi Terbesar (Top 5 / 10 / 20)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTransactionsSection(
    transactions: List<TopTransactionItem>,
    currentFilter: TopTransactionFilter,
    currentTypeFilter: TransactionTypeFilter,
    onFilterChange: (TopTransactionFilter) -> Unit,
    onTypeFilterChange: (TransactionTypeFilter) -> Unit
) {
    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Transaksi Terbesar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SakuDarkGreen
            )

            // Filter Row 1: Limit Filter (Top 5 / 10 / 20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopTransactionFilter.entries.forEach { filter ->
                    val isSelected = currentFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SakuDarkGreen,
                            selectedLabelColor = Color.White,
                            containerColor = SakuCreamBackground,
                            labelColor = SakuTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = SakuCreamBorder,
                            selectedBorderColor = SakuDarkGreen,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Filter Row 2: Type Filter (Semua / Pemasukan / Pengeluaran)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionTypeFilter.entries.forEach { typeFilter ->
                    val isSelected = currentTypeFilter == typeFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTypeFilterChange(typeFilter) },
                        label = {
                            Text(
                                text = typeFilter.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = SakuDarkGreen,
                                                    selectedLabelColor = Color.White,
                                                    containerColor = SakuCreamBackground,
                                                    labelColor = SakuTextSecondary
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    borderColor = SakuCreamBorder,
                                                    selectedBorderColor = SakuDarkGreen,
                                                    enabled = true,
                                                    selected = isSelected
                                                )
                    )
                }
            }

            // Transaction items
            if (transactions.isEmpty()) {
                EmptyChartPlaceholder(message = "Tidak ada transaksi yang sesuai kriteria")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    transactions.forEachIndexed { index, tx ->
                        TopTransactionItemRow(
                            rank = index + 1,
                            item = tx
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopTransactionItemRow(
    rank: Int,
    item: TopTransactionItem
) {
    val isIncome = item.type == TransactionType.INCOME
    val isTransfer = item.type == TransactionType.TRANSFER

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (rank <= 3) SakuGoldAccent.copy(alpha = 0.2f) else SakuCreamBorder.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) SakuDarkGreen else SakuTextMuted
                    )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            // Icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        if (isIncome) SakuIncomeGreenBg
                        else if (isTransfer) SakuGoldLight
                        else SakuExpenseRedBg,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isTransfer) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = SakuGoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = getCategoryIconVector(item.categoryIcon),
                        contentDescription = null,
                        tint = if (isIncome) SakuIncomeGreen else SakuExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            // Title & Info
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SakuTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isTransfer) {
                    val source = item.sourceAssetName?.takeIf { it.isNotBlank() } ?: "-"
                    val target = item.targetAssetName?.takeIf { it.isNotBlank() } ?: "-"
                    Text(
                        text = "$source → $target • ${Formatters.formatShortDateIndo(item.dateMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SakuTextMuted
                    )
                } else {
                    Text(
                        text = "${item.categoryName} • ${Formatters.formatShortDateIndo(item.dateMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SakuTextMuted
                    )
                }
            }
        }

        // Amount
        Text(
            text = if (isIncome) {
                "+${Formatters.formatRupiah(item.amount)}"
            } else if (isTransfer) {
                Formatters.formatRupiah(item.amount)
            } else {
                "-${Formatters.formatRupiah(item.amount)}"
            },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isIncome) SakuIncomeGreen else if (isTransfer) SakuGoldAccent else SakuExpenseRed
        )
    }
}

// ============================================================
// 8. Tombol Export
// ============================================================
@Composable
fun ExportActionCard(onClick: () -> Unit) {
    SakuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = SakuDarkGreen,
        borderColor = SakuDarkGreen,
        cornerRadius = 16.dp,
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Ekspor Laporan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Excel, Cadangan Internal, & Cloud",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(-90f)
            )
        }
    }
}

// ============================================================
// Shared Sub-components
// ============================================================
@Composable
fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SakuTextSecondary
        )
    }
}

@Composable
fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SakuTextMuted,
            textAlign = TextAlign.Center
        )
    }
}