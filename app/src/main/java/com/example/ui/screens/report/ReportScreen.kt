package com.example.ui.screens.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.Formatters
import com.example.domain.model.CategoryExpenseSummary
import com.example.ui.components.SakuCard
import com.example.ui.components.getCategoryIconVector
import com.example.ui.theme.SakuCreamBackground
import com.example.ui.theme.SakuCreamBorder
import com.example.ui.theme.SakuCreamSurface
import com.example.ui.theme.SakuCreamSurfaceVariant
import com.example.ui.theme.SakuDarkGreen
import com.example.ui.theme.SakuDarkGreenDeep
import com.example.ui.theme.SakuExpenseRed
import com.example.ui.theme.SakuExpenseRedBg
import com.example.ui.theme.SakuGoldAccent
import com.example.ui.theme.SakuGoldLight
import com.example.ui.theme.SakuIncomeGreen
import com.example.ui.theme.SakuIncomeGreenBg
import com.example.ui.theme.SakuLightGreen
import com.example.ui.theme.SakuSageAccent
import com.example.ui.theme.SakuTextMuted
import com.example.ui.theme.SakuTextPrimary
import com.example.ui.theme.SakuTextSecondary
import java.util.Locale

@Composable
fun ReportScreen(
    viewModel: ReportViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    var copySnackbarVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuCreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header & Export Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Laporan",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SakuDarkGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Evaluasi arus kas, kebiasaan belanja, dan rasio tabungan",
                            style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Secondary Action: Ekspor
                    Surface(
                        onClick = { viewModel.exportToCsv() },
                        shape = RoundedCornerShape(12.dp),
                        color = SakuCreamSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                        modifier = Modifier.testTag("btn_export_report")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Ekspor Laporan",
                                tint = SakuDarkGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Ekspor",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                        }
                    }
                }
            }

            // Period Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val periods = listOf(
                        ReportPeriod.THIS_MONTH,
                        ReportPeriod.THREE_MONTHS,
                        ReportPeriod.SIX_MONTHS,
                        ReportPeriod.THIS_YEAR,
                        ReportPeriod.CUSTOM
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SakuCreamSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            periods.forEach { p ->
                                val isSelected = uiState.period == p
                                Surface(
                                    onClick = { viewModel.selectPeriod(p) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SakuDarkGreen else Color.Transparent,
                                    modifier = Modifier
                                        .height(38.dp)
                                        .testTag("period_tab_${p.name}")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = p.titleIndo,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else SakuTextSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick selector when KUSTOM period is active
                    if (uiState.period == ReportPeriod.CUSTOM) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val customOptions = listOf(
                                -1 to "Bulan Lalu",
                                -2 to "2 Bln Lalu",
                                -3 to "3 Bln Lalu",
                                -4 to "4 Bln Lalu"
                            )
                            customOptions.forEach { (offset, label) ->
                                val isChosen = uiState.customMonthOffset == offset
                                Surface(
                                    onClick = { viewModel.selectCustomMonth(offset) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChosen) SakuSageAccent else SakuCreamSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isChosen) SakuSageAccent else SakuCreamBorder
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isChosen) Color.White else SakuTextPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Financial Summary: 3 Compact Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pemasukan
                    CompactMetricCard(
                        title = "Pemasukan",
                        amount = uiState.totalIncome,
                        icon = Icons.Default.ArrowUpward,
                        iconTint = SakuIncomeGreen,
                        iconBg = SakuIncomeGreenBg,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_income"
                    )

                    // Pengeluaran
                    CompactMetricCard(
                        title = "Pengeluaran",
                        amount = uiState.totalExpense,
                        icon = Icons.Default.ArrowDownward,
                        iconTint = SakuExpenseRed,
                        iconBg = SakuExpenseRedBg,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_expense"
                    )

                    // Sisa Saldo
                    CompactMetricCard(
                        title = "Sisa Saldo",
                        amount = uiState.totalBalance,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = SakuGoldAccent,
                        iconBg = SakuGoldLight,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_balance"
                    )
                }
            }

            // Prominent Arus Kas / Net Cashflow Card
            item {
                ProminentCashflowCard(uiState = uiState)
            }

            // 3. Cashflow Chart: Grouped Vertical Bar Chart
            item {
                CashflowGroupedBarChartCard(
                    uiState = uiState,
                    onSelectMonth = { viewModel.selectMonthBar(it) }
                )
            }

            // 4. Category Spending Visualization: Donut Chart
            item {
                CategorySpendingDonutCard(
                    uiState = uiState,
                    onToggleCategory = { viewModel.toggleCategorySelection(it) }
                )
            }

            // 5. Additional Financial Insight Visualization: Savings Rate Line Chart
            item {
                SavingsRateTrendCard(uiState = uiState)
            }
        }

        // Export Dialog
        if (uiState.isExportDialogOpen && uiState.exportedCsv != null) {
            AlertDialog(
                onDismissRequest = { viewModel.closeExportDialog() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = SakuDarkGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ekspor Laporan Keuangan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuDarkGreen
                            )
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Format CSV transaksi Saku siap disalin ke spreadsheet atau aplikasi pengolah data.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SakuTextSecondary)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SakuCreamSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SakuCreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = uiState.exportedCsv ?: "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = SakuTextPrimary,
                                    maxLines = 7,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (copySnackbarVisible) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SakuIncomeGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Berhasil disalin ke papan klip!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SakuIncomeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(uiState.exportedCsv ?: ""))
                            copySnackbarVisible = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SakuDarkGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.closeExportDialog()
                        copySnackbarVisible = false
                    }) {
                        Text("Tutup", color = SakuTextSecondary)
                    }
                },
                containerColor = SakuCreamSurface,
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// COMPONENT: Compact Metric Card
// -----------------------------------------------------------------------------------------
@Composable
private fun CompactMetricCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    SakuCard(
        modifier = modifier.testTag(testTag),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 16.dp,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SakuTextMuted,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCompactAmount(amount),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// COMPONENT: Prominent Arus Kas Card
// -----------------------------------------------------------------------------------------
@Composable
private fun ProminentCashflowCard(uiState: ReportUiState) {
    val isSurplus = uiState.netCashflow >= 0
    val brush = Brush.verticalGradient(
        colors = listOf(SakuDarkGreen, SakuDarkGreenDeep)
    )

    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuDarkGreen,
        cornerRadius = 22.dp,
        elevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top Tag & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Arus Kas Bersih (Net Cashflow)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = uiState.periodTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SakuLightGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSurplus) Color(0xFF1B6A45) else Color(0xFF8A2424)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSurplus) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isSurplus) "Surplus Kas" else "Defisit Kas",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Big Net Amount
                val formattedNet = if (uiState.netCashflow >= 0) {
                    "+${Formatters.formatRupiah(uiState.netCashflow)}"
                } else {
                    Formatters.formatRupiah(uiState.netCashflow)
                }

                Text(
                    text = formattedNet,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                )

                // Sub-metrics Bar: Rasio Tabungan & Transaksi
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = SakuGoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Rasio Simpanan: ${uiState.savingsRatePercentage}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Text(
                            text = "${uiState.transactionCount} Transaksi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// COMPONENT: Grouped Vertical Bar Chart Card
// -----------------------------------------------------------------------------------------
@Composable
private fun CashflowGroupedBarChartCard(
    uiState: ReportUiState,
    onSelectMonth: (Int?) -> Unit
) {
    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 20.dp,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Perbandingan Arus Kas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                    Text(
                        text = uiState.chartSummaryTitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                    )
                }

                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SakuDarkGreen)
                        )
                        Text(
                            text = "Masuk",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = SakuTextSecondary
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SakuExpenseRed)
                        )
                        Text(
                            text = "Keluar",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = SakuTextSecondary
                            )
                        )
                    }
                }
            }

            // Interactive Tooltip (if a month is selected)
            AnimatedVisibility(visible = uiState.selectedMonthIndex != null) {
                val selIndex = uiState.selectedMonthIndex ?: 0
                val item = uiState.monthlyCashflows.getOrNull(selIndex)
                if (item != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SakuLightGreen,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SakuSageAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.fullMonthLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SakuDarkGreen
                                )
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "In: ${formatCompactAmount(item.income)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuDarkGreen
                                    )
                                )
                                Text(
                                    text = "Out: ${formatCompactAmount(item.expense)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SakuExpenseRed
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // The Canvas Grouped Bar Chart
            if (uiState.monthlyCashflows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat data pada periode ini",
                        style = MaterialTheme.typography.bodySmall.copy(color = SakuTextMuted)
                    )
                }
            } else {
                CashflowBarCanvas(
                    cashflows = uiState.monthlyCashflows,
                    selectedIndex = uiState.selectedMonthIndex,
                    onSelectMonth = onSelectMonth
                )
            }
        }
    }
}

@Composable
private fun CashflowBarCanvas(
    cashflows: List<MonthlyCashflow>,
    selectedIndex: Int?,
    onSelectMonth: (Int?) -> Unit
) {
    val maxVal = remember(cashflows) {
        val peak = cashflows.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0
        if (peak <= 0.0) 1000000.0 else peak * 1.15
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(cashflows) {
                detectTapGestures { offset ->
                    val totalGroups = cashflows.size
                    val leftPadding = 42.dp.toPx()
                    val chartWidth = size.width - leftPadding
                    val relX = offset.x - leftPadding
                    if (relX >= 0 && relX <= chartWidth && totalGroups > 0) {
                        val groupW = chartWidth / totalGroups
                        val clickedGroup = (relX / groupW).toInt().coerceIn(0, totalGroups - 1)
                        if (selectedIndex == clickedGroup) {
                            onSelectMonth(null)
                        } else {
                            onSelectMonth(clickedGroup)
                        }
                    }
                }
            }
    ) {
        val leftAxisPadding = 44.dp
        val bottomAxisPadding = 26.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Y-Axis Compact Labels
            Column(
                modifier = Modifier
                    .width(leftAxisPadding)
                    .height(200.dp - bottomAxisPadding),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCompactAmount(maxVal),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = SakuTextMuted
                    ),
                    maxLines = 1
                )
                Text(
                    text = formatCompactAmount(maxVal / 2),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = SakuTextMuted
                    ),
                    maxLines = 1
                )
                Text(
                    text = "0",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = SakuTextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Main Canvas Area
            Column(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val w = size.width
                    val h = size.height
                    val n = cashflows.size
                    if (n == 0) return@Canvas

                    // Draw subtle gridlines
                    val gridEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    val gridColor = SakuCreamBorder.copy(alpha = 0.8f)

                    // Top gridline
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, 2f),
                        end = Offset(w, 2f),
                        pathEffect = gridEffect,
                        strokeWidth = 1f
                    )

                    // Mid gridline
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, h / 2),
                        end = Offset(w, h / 2),
                        pathEffect = gridEffect,
                        strokeWidth = 1f
                    )

                    // Base baseline
                    drawLine(
                        color = SakuCreamBorder,
                        start = Offset(0f, h),
                        end = Offset(w, h),
                        strokeWidth = 1.5f
                    )

                    val groupWidth = w / n
                    val barWidth = (groupWidth * 0.28f).coerceIn(8.dp.toPx(), 18.dp.toPx())
                    val barSpacing = 4.dp.toPx()

                    cashflows.forEachIndexed { i, item ->
                        val groupCenterX = i * groupWidth + (groupWidth / 2)
                        val incomeX = groupCenterX - barWidth - (barSpacing / 2)
                        val expenseX = groupCenterX + (barSpacing / 2)

                        val incomeH = ((item.income / maxVal) * h).toFloat().coerceAtLeast(3f)
                        val expenseH = ((item.expense / maxVal) * h).toFloat().coerceAtLeast(3f)

                        val isHighlighted = selectedIndex == i

                        // Draw selection highlight pillar background
                        if (isHighlighted) {
                            drawRoundRect(
                                color = SakuLightGreen.copy(alpha = 0.5f),
                                topLeft = Offset(i * groupWidth + 2f, 0f),
                                size = Size(groupWidth - 4f, h),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }

                        // Pemasukan Bar
                        drawRoundRect(
                            color = SakuDarkGreen,
                            topLeft = Offset(incomeX, h - incomeH),
                            size = Size(barWidth, incomeH),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )

                        // Pengeluaran Bar
                        drawRoundRect(
                            color = SakuExpenseRed,
                            topLeft = Offset(expenseX, h - expenseH),
                            size = Size(barWidth, expenseH),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )
                    }
                }

                // X-Axis Month Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomAxisPadding),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    cashflows.forEachIndexed { i, item ->
                        val isSel = selectedIndex == i
                        Text(
                            text = item.monthLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) SakuDarkGreen else SakuTextSecondary
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable {
                                onSelectMonth(if (isSel) null else i)
                            }
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// COMPONENT: Category Spending Donut Card
// -----------------------------------------------------------------------------------------
@Composable
private fun CategorySpendingDonutCard(
    uiState: ReportUiState,
    onToggleCategory: (String) -> Unit
) {
    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 20.dp,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Distribusi Pengeluaran",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                    Text(
                        text = "Proporsi alokasi pos belanja pada periode ini",
                        style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                    )
                }

                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = SakuSageAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (uiState.donutSegments.isEmpty() || uiState.totalExpense <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada catatan pengeluaran pada periode ini.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SakuTextMuted)
                    )
                }
            } else {
                // Donut Chart & Center Metric
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val selectedSegment = uiState.donutSegments.find { it.categoryName == uiState.selectedCategoryName }

                    // Canvas Donut
                    DonutCanvas(
                        segments = uiState.donutSegments,
                        selectedCategory = uiState.selectedCategoryName,
                        onToggleCategory = onToggleCategory,
                        modifier = Modifier.size(190.dp)
                    )

                    // Center Content Inside Donut
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        if (selectedSegment != null) {
                            Text(
                                text = selectedSegment.categoryName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SakuTextSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Formatters.formatRupiah(selectedSegment.amount),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "${(selectedSegment.percentage * 100).toInt()}% porsi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SakuSageAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        } else {
                            Text(
                                text = "Total Belanja",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = SakuTextMuted
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Formatters.formatRupiah(uiState.totalExpense),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SakuDarkGreen,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "${uiState.transactionCount} Transaksi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = SakuTextSecondary
                                )
                            )
                        }
                    }
                }

                // Interactive Legend Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.donutSegments.forEach { seg ->
                        val isSelected = uiState.selectedCategoryName == seg.categoryName
                        val segColor = parseColorSafely(seg.colorHex)

                        Surface(
                            onClick = { onToggleCategory(seg.categoryName) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SakuLightGreen else SakuCreamSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SakuDarkGreen else SakuCreamBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(segColor)
                                )
                                Text(
                                    text = seg.categoryName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SakuDarkGreen else SakuTextPrimary,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "${(seg.percentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SakuTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Category List Breakdown Items
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.categoryBreakdown.forEach { cat ->
                        val isSelected = uiState.selectedCategoryName == cat.categoryName
                        DetailedCategoryRow(
                            summary = cat,
                            isSelected = isSelected,
                            onToggle = { onToggleCategory(cat.categoryName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(
    segments: List<DonutCategorySegment>,
    selectedCategory: String?,
    onToggleCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInput(segments) {
            detectTapGestures { offset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val touchAngle = (Math.toDegrees(
                    Math.atan2(
                        (offset.y - center.y).toDouble(),
                        (offset.x - center.x).toDouble()
                    )
                ).toFloat() + 360f + 90f) % 360f

                var accumAngle = 0f
                for (seg in segments) {
                    val sweep = seg.percentage * 360f
                    if (touchAngle >= accumAngle && touchAngle < accumAngle + sweep) {
                        onToggleCategory(seg.categoryName)
                        break
                    }
                    accumAngle += sweep
                }
            }
        }
    ) {
        val strokeWidthDefault = 22.dp.toPx()
        val strokeWidthSelected = 30.dp.toPx()
        val radius = (size.minDimension - strokeWidthSelected) / 2f
        val arcSize = Size(radius * 2, radius * 2)
        val topLeft = Offset(
            (size.width - radius * 2) / 2f,
            (size.height - radius * 2) / 2f
        )

        var currentStartAngle = -90f // Start from top

        segments.forEach { seg ->
            val sweep = (seg.percentage * 360f).coerceAtLeast(1f)
            val isSelected = selectedCategory == seg.categoryName
            val sliceStroke = if (isSelected) strokeWidthSelected else strokeWidthDefault
            val color = parseColorSafely(seg.colorHex)

            // Draw slice arc with small 2.5 degree gap for separation
            val adjustedSweep = (sweep - 2.5f).coerceAtLeast(0.5f)

            drawArc(
                color = color,
                startAngle = currentStartAngle + 1.25f,
                sweepAngle = adjustedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sliceStroke, cap = StrokeCap.Round)
            )

            currentStartAngle += sweep
        }
    }
}

@Composable
private fun DetailedCategoryRow(
    summary: CategoryExpenseSummary,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SakuLightGreen.copy(alpha = 0.7f) else SakuCreamSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) SakuDarkGreen else SakuCreamBorder.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SakuExpenseRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIconVector(summary.categoryIcon),
                            contentDescription = null,
                            tint = SakuExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = summary.categoryName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SakuTextPrimary
                            )
                        )
                        Text(
                            text = "${(summary.percentage * 100).toInt()}% dari total",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SakuTextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Text(
                    text = Formatters.formatRupiah(summary.totalAmount),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SakuDarkGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { summary.percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = SakuDarkGreen,
                trackColor = SakuCreamBorder
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// COMPONENT: Savings Rate Trend Card (Additional Financial Insight Visualization)
// -----------------------------------------------------------------------------------------
@Composable
private fun SavingsRateTrendCard(uiState: ReportUiState) {
    SakuCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SakuCreamSurface,
        cornerRadius = 20.dp,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tren Rasio Simpanan",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen
                        )
                    )
                    Text(
                        text = "Persentase tabungan bersih terhadap pemasukan",
                        style = MaterialTheme.typography.labelSmall.copy(color = SakuTextSecondary)
                    )
                }

                // Reference Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SakuGoldLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SakuGoldAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Target Anjuran 20%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuGoldAccent,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (uiState.monthlyCashflows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada catatan untuk menghitung rasio simpanan",
                        style = MaterialTheme.typography.bodySmall.copy(color = SakuTextMuted)
                    )
                }
            } else {
                SavingsRateLineCanvas(cashflows = uiState.monthlyCashflows)

                // Subtitle explanation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SakuSageAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Garis emas putus-putus adalah indikator target acuan finansial 20%.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SakuTextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingsRateLineCanvas(cashflows: List<MonthlyCashflow>) {
    val bottomLabelHeight = 24.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val w = size.width
            val h = size.height
            val n = cashflows.size
            if (n == 0) return@Canvas

            // Max Y range for scale: at least 50% so the 20% reference line is nicely placed
            val maxRate = (cashflows.maxOfOrNull { it.savingsRate } ?: 20).coerceAtLeast(40)
            val maxY = maxRate * 1.25f // headroom

            // Target reference line at 20%
            val targetY = h - ((20f / maxY) * h)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

            drawLine(
                color = SakuGoldAccent,
                start = Offset(0f, targetY),
                end = Offset(w, targetY),
                pathEffect = dashEffect,
                strokeWidth = 2f
            )

            // Prepare points
            val stepX = if (n > 1) w / (n - 1) else w / 2
            val points = cashflows.mapIndexed { i, item ->
                val x = if (n > 1) i * stepX else w / 2
                val y = h - ((item.savingsRate.toFloat() / maxY) * h).coerceIn(10f, h - 10f)
                Offset(x, y)
            }

            // Draw gradient area under the curve
            val areaPath = Path().apply {
                moveTo(points.first().x, h)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, h)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(SakuDarkGreen.copy(alpha = 0.25f), Color.Transparent)
                )
            )

            // Draw line connecting points
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = linePath,
                color = SakuDarkGreen,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw dots at each month point
            points.forEachIndexed { i, pt ->
                // Outer circle
                drawCircle(
                    color = SakuLightGreen,
                    radius = 6.dp.toPx(),
                    center = pt
                )
                // Inner dot
                drawCircle(
                    color = SakuDarkGreen,
                    radius = 3.5.dp.toPx(),
                    center = pt
                )
            }
        }

        // Percentage & Month label row beneath canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomLabelHeight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            cashflows.forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${item.savingsRate}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SakuDarkGreen,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = item.monthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SakuTextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// UTILITY FUNCTIONS
// -----------------------------------------------------------------------------------------
private fun formatCompactAmount(amount: Double): String {
    val absVal = kotlin.math.abs(amount)
    val prefix = if (amount < 0) "-Rp " else "Rp "
    return when {
        absVal >= 1_000_000_000 -> "$prefix${String.format(Locale.US, "%.1f", absVal / 1_000_000_000).removeSuffix(".0")} M"
        absVal >= 1_000_000 -> "$prefix${String.format(Locale.US, "%.1f", absVal / 1_000_000).removeSuffix(".0")} jt"
        absVal >= 1_000 -> "$prefix${String.format(Locale.US, "%.0f", absVal / 1_000)} rb"
        else -> Formatters.formatRupiah(amount)
    }
}

private fun parseColorSafely(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        SakuDarkGreen
    }
}
