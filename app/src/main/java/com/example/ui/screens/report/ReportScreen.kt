package com.example.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.Formatters
import kotlin.math.max

@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ReportPeriodSelector(
            selectedPeriod = uiState.period,
            onClick = { period -> viewModel.selectPeriod(period) }
        )
        
        when {
            uiState.monthlyCashflows.isEmpty() -> EmptyState()
            else -> ReportContent(uiState = uiState)
        }
    }
}

@Composable
fun ReportPeriodSelector(
    selectedPeriod: ReportPeriod,
    onClick: (ReportPeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onClick(selectedPeriod) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedPeriod.titleIndo,
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada transaksi",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportContent(uiState: ReportUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CashflowOverviewSection(uiState = uiState)
        CashflowTrendSection(uiState = uiState)
        CategorySpendingSection(uiState = uiState)
    }
}

@Composable
fun CashflowOverviewSection(uiState: ReportUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ringkasan Arus Kas",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactMetricCard(
                    label = "Pendapatan",
                    amount = uiState.totalIncome,
                    color = MaterialTheme.colorScheme.tertiary
                )
                CompactMetricCard(
                    label = "Pengeluaran",
                    amount = uiState.totalExpense,
                    color = MaterialTheme.colorScheme.error
                )
                CompactMetricCard(
                    label = "Sisa",
                    amount = uiState.netCashflow,
                    color = if (uiState.netCashflow >= 0) 
                        MaterialTheme.colorScheme.tertiary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CompactMetricCard(label: String, amount: Double, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.3f)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(modifier = Modifier.height(4.dp))
            Text(
                text = Formatters.formatRupiah(amount),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CashflowTrendSection(uiState: ReportUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = uiState.chartSummaryTitle,
                style = MaterialTheme.typography.titleMedium
            )
            CashflowChart(data = uiState.monthlyCashflows)
        }
    }
}

@Composable
fun CashflowChart(data: List<MonthlyCashflow>) {
    if (data.isEmpty()) {
        Text("No data available")
        return
    }
    
    val maxAmount = data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { cashflow ->
                BarItem(
                    income = cashflow.income,
                    expense = cashflow.expense,
                    maxAmount = maxAmount
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { cashflow ->
                Text(
                    text = cashflow.monthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BarItem(income: Double, expense: Double, maxAmount: Double) {
    Row(
        modifier = Modifier.height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (income > 0 && maxAmount > 0) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(((income / maxAmount) * 100).dp)
                    .background(
                        MaterialTheme.colorScheme.tertiary,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }
        if (expense > 0 && maxAmount > 0) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(((expense / maxAmount) * 100).dp)
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }
    }
}

@Composable
fun CategorySpendingSection(uiState: ReportUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pengeluaran Per Kategori",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (uiState.donutSegments.isEmpty()) {
                Text(
                    text = "Tidak ada data kategori",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.categoryBreakdown.take(5).forEach { category ->
                    CategoryRow(
                        name = category.categoryName,
                        amount = category.totalAmount,
                        percentage = category.percentage
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryRow(name: String, amount: Double, percentage: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
