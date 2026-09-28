package com.example.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.Formatters
import com.example.domain.model.Asset
import com.example.domain.model.CategoryExpenseSummary
import com.example.domain.model.FinancialReport
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

// ============================================================
// Period Enum - Single source of truth for all report sections
// ============================================================
enum class ReportPeriod(val titleIndo: String) {
    TODAY("Hari Ini"),
    THIS_WEEK("Minggu Ini"),
    LAST_WEEK("Minggu Lalu"),
    THIS_MONTH("Bulan Ini"),
    LAST_MONTH("Bulan Lalu"),
    CUSTOM("Kustom")
}

// ============================================================
// Data classes for chart rendering
// ============================================================
data class CashflowPoint(
    val label: String,           // X-axis label (date/week)
    val dateMillis: Long,        // For tooltip/sorting
    val income: Double,
    val expense: Double
)

data class BalancePoint(
    val label: String,
    val dateMillis: Long,
    val balance: Double
)

data class TopTransactionItem(
    val id: String,
    val title: String,
    val categoryName: String,
    val categoryIcon: String,
    val dateMillis: Long,
    val amount: Double,
    val type: TransactionType
)

enum class TopTransactionFilter(val title: String) {
    TOP_5("Top 5"),
    TOP_10("Top 10"),
    TOP_20("Top 20")
}

enum class TransactionTypeFilter(val title: String) {
    ALL("Semua"),
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran")
}

// ============================================================
// UI State
// ============================================================
data class ReportUiState(
    val period: ReportPeriod = ReportPeriod.THIS_MONTH,
    val customStartMillis: Long = 0L,
    val customEndMillis: Long = 0L,
    val periodTitle: String = "Bulan Ini",
    val totalAssetBalance: Double = 0.0,
    // Summary
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val incomeChangePercent: Double = 0.0,    // vs previous period
    val expenseChangePercent: Double = 0.0,   // vs previous period
    // Cashflow line chart
    val cashflowPoints: List<CashflowPoint> = emptyList(),
    // Category donut
    val donutSegments: List<DonutCategorySegment> = emptyList(),
    val categoryBreakdown: List<CategoryExpenseSummary> = emptyList(),
    // Balance trend
    val balancePoints: List<BalancePoint> = emptyList(),
    // Top transactions
    val topTransactions: List<TopTransactionItem> = emptyList(),
    val topTransactionFilter: TopTransactionFilter = TopTransactionFilter.TOP_5,
    val transactionTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    // Loading/empty states
    val isLoading: Boolean = false,
    val hasData: Boolean = false
) {
    val selectedPeriodTitle: String
        get() = when (period) {
            ReportPeriod.CUSTOM -> {
                if (customStartMillis > 0L && customEndMillis > 0L) {
                    "${Formatters.formatShortDateIndo(customStartMillis)} - ${Formatters.formatShortDateIndo(customEndMillis)}"
                } else periodTitle
            }
            else -> periodTitle
        }
}

// Donut segment
data class DonutCategorySegment(
    val categoryName: String,
    val categoryIcon: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: String,
    val isOther: Boolean = false
)

val CATEGORY_CHART_COLORS = listOf(
    "#133E35", // Deep forest green
    "#D32F2F", // Crimson Red
    "#C58E2E", // Gold Amber
    "#1E3A8A", // Navy Blue
    "#EA580C", // Warm Orange
    "#7C3AED", // Royal Purple
    "#0D9488", // Ocean Teal
    "#4C7E6A", // Sage Green
    "#C2185B", // Rose Pink
    "#64748B"  // Neutral Slate for Lainnya
)

class ReportViewModel(
    private val repository: SakuRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _customStartMillis = MutableStateFlow(0L)
    private val _customEndMillis = MutableStateFlow(0L)
    private val _topTransactionFilter = MutableStateFlow(TopTransactionFilter.TOP_5)
    private val _transactionTypeFilter = MutableStateFlow(TransactionTypeFilter.ALL)

    val topTransactionFilter: StateFlow<TopTransactionFilter> = _topTransactionFilter.asStateFlow()
    val transactionTypeFilter: StateFlow<TransactionTypeFilter> = _transactionTypeFilter.asStateFlow()

    private data class FilterState(
        val period: ReportPeriod,
        val customStart: Long,
        val customEnd: Long,
        val topFilter: TopTransactionFilter,
        val typeFilter: TransactionTypeFilter
    )

    private val filterState: Flow<FilterState> = combine(
        _selectedPeriod,
        _customStartMillis,
        _customEndMillis,
        _topTransactionFilter,
        _transactionTypeFilter
    ) { period, customStart, customEnd, topFilter, typeFilter ->
        FilterState(period, customStart, customEnd, topFilter, typeFilter)
    }

    // Main UI State - combines all data sources with filter
    val uiState: StateFlow<ReportUiState> = combine(
        repository.getAllTransactions(),
        repository.getActiveAssets(),
        filterState
    ) { transactions, assets, filters ->
        calculateReportUiState(
            transactions = transactions,
            assets = assets,
            period = filters.period,
            customStartMillis = filters.customStart,
            customEndMillis = filters.customEnd,
            topFilter = filters.topFilter,
            typeFilter = filters.typeFilter
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportUiState()
    )

    // ============================================================
    // Public Actions
    // ============================================================
    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
        _customStartMillis.value = 0L
        _customEndMillis.value = 0L
    }

    fun selectCustomRange(startMillis: Long, endMillis: Long) {
        _selectedPeriod.value = ReportPeriod.CUSTOM
        _customStartMillis.value = startMillis
        _customEndMillis.value = endMillis
    }

    fun setTopTransactionFilter(filter: TopTransactionFilter) {
        _topTransactionFilter.value = filter
    }

    fun setTransactionTypeFilter(filter: TransactionTypeFilter) {
        _transactionTypeFilter.value = filter
    }

    // Backward compatibility for existing tests
    val report: StateFlow<FinancialReport> = uiState.map { state ->
        FinancialReport(
            periodTitle = state.periodTitle,
            summary = FinancialSummary(
                totalAssetBalance = state.totalAssetBalance,
                totalPlannedAllocation = 0.0,
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense,
                totalIncomeThisMonth = state.totalIncome,
                totalExpenseThisMonth = state.totalExpense,
                netSavingsThisMonth = state.totalIncome - state.totalExpense,
                savingsRatePercentage = if (state.totalIncome > 0) ((state.totalIncome - state.totalExpense) / state.totalIncome * 100).toInt().coerceIn(0, 100) else 0
            ),
            categoryBreakdown = state.categoryBreakdown,
            transactionCount = state.cashflowPoints.sumOf { (it.income + it.expense).toInt() }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialReport(
            periodTitle = "Periode Berjalan",
            summary = FinancialSummary(),
            categoryBreakdown = emptyList(),
            transactionCount = 0
        )
    )

    // ============================================================
    // Core Calculation
    // ============================================================
    private fun calculateReportUiState(
        transactions: List<Transaction>,
        assets: List<Asset>,
        period: ReportPeriod,
        customStartMillis: Long,
        customEndMillis: Long,
        topFilter: TopTransactionFilter,
        typeFilter: TransactionTypeFilter
    ): ReportUiState {

        val (periodStart, periodEnd, periodTitle) = when (period) {
            ReportPeriod.TODAY -> {
                val start = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val end = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                Triple(start.timeInMillis, end.timeInMillis, "Hari Ini")
            }
            ReportPeriod.THIS_WEEK -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Triple(start, end, "Minggu Ini")
            }
            ReportPeriod.LAST_WEEK -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.add(Calendar.WEEK_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Triple(start, end, "Minggu Lalu")
            }
            ReportPeriod.THIS_MONTH -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                cal.add(Calendar.MILLISECOND, -1)
                val end = cal.timeInMillis
                Triple(start, end, "Bulan Ini")
            }
            ReportPeriod.LAST_MONTH -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                cal.add(Calendar.MILLISECOND, -1)
                val end = cal.timeInMillis
                Triple(start, end, "Bulan Lalu")
            }
            ReportPeriod.CUSTOM -> {
                if (customStartMillis > 0L && customEndMillis > 0L) {
                    Triple(customStartMillis, customEndMillis, "Kustom")
                } else {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    cal.add(Calendar.MILLISECOND, -1)
                    val end = cal.timeInMillis
                    Triple(start, end, "Bulan Ini")
                }
            }
        }

        // Filter transactions for current period
        val periodTransactions = transactions.filter { tx ->
            tx.dateMillis in periodStart..periodEnd
        }.sortedByDescending { it.dateMillis }

        // Previous period for comparison (same duration, immediately before)
        val periodDuration = periodEnd - periodStart
        val prevPeriodStart = periodStart - periodDuration - 1L
        val prevPeriodEnd = periodStart - 1L
        val prevPeriodTransactions = transactions.filter { tx ->
            tx.dateMillis in prevPeriodStart..prevPeriodEnd
        }

        // Calculate totals
        val totalIncome = periodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = periodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val prevIncome = prevPeriodTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val prevExpense = prevPeriodTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val incomeChangePercent = if (prevIncome > 0.0) ((totalIncome - prevIncome) / prevIncome * 100.0) else 0.0
        val expenseChangePercent = if (prevExpense > 0.0) ((totalExpense - prevExpense) / prevExpense * 100.0) else 0.0

        // Category breakdown for donut
        val categoryExpenses = mutableMapOf<String, Double>()
        val categoryIcons = mutableMapOf<String, String>()
        periodTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
            categoryExpenses[tx.categoryName] = (categoryExpenses[tx.categoryName] ?: 0.0) + tx.amount
            categoryIcons[tx.categoryName] = tx.categoryIcon
        }

        val categoryBreakdown = categoryExpenses.map { (name, amount) ->
            val pct = if (totalExpense > 0.0) (amount / totalExpense).toFloat() else 0f
            CategoryExpenseSummary(
                categoryName = name,
                categoryIcon = categoryIcons[name] ?: "category",
                totalAmount = amount,
                percentage = pct,
                colorHex = "#133E35"
            )
        }.sortedByDescending { it.totalAmount }

        // Donut segments (Top 5 + Lainnya)
        val donutSegments = mutableListOf<DonutCategorySegment>()
        if (categoryBreakdown.size <= 5) {
            categoryBreakdown.forEachIndexed { index, cat ->
                val color = CATEGORY_CHART_COLORS.getOrElse(index) { "#133E35" }
                donutSegments.add(
                    DonutCategorySegment(
                        categoryName = cat.categoryName,
                        categoryIcon = cat.categoryIcon,
                        amount = cat.totalAmount,
                        percentage = cat.percentage,
                        colorHex = color,
                        isOther = false
                    )
                )
            }
        } else {
            val top5 = categoryBreakdown.take(5)
            top5.forEachIndexed { index, cat ->
                val color = CATEGORY_CHART_COLORS.getOrElse(index) { "#133E35" }
                donutSegments.add(
                    DonutCategorySegment(
                        categoryName = cat.categoryName,
                        categoryIcon = cat.categoryIcon,
                        amount = cat.totalAmount,
                        percentage = cat.percentage,
                        colorHex = color,
                        isOther = false
                    )
                )
            }
            val others = categoryBreakdown.drop(5)
            val othersTotal = others.sumOf { it.totalAmount }
            val othersPct = if (totalExpense > 0.0) (othersTotal / totalExpense).toFloat() else 0f
            donutSegments.add(
                DonutCategorySegment(
                    categoryName = "Lainnya",
                    categoryIcon = "category",
                    amount = othersTotal,
                    percentage = othersPct,
                    colorHex = "#64748B",
                    isOther = true
                )
            )
        }

        // Cashflow line chart data points
        val cashflowPoints = buildCashflowPoints(transactions, periodStart, periodEnd)

        // Balance trend - reconstruct from current balances + transaction history
        val balancePoints = buildBalancePoints(transactions, assets, periodStart, periodEnd)

        // Top transactions
        val topTransactions = getTopTransactions(transactions, periodStart, periodEnd, topFilter, typeFilter)

        return ReportUiState(
            period = period,
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis,
            periodTitle = periodTitle,
            totalAssetBalance = assets.sumOf { it.balance },
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            incomeChangePercent = incomeChangePercent,
            expenseChangePercent = expenseChangePercent,
            cashflowPoints = cashflowPoints,
            donutSegments = donutSegments,
            categoryBreakdown = categoryBreakdown,
            balancePoints = balancePoints,
            topTransactions = topTransactions,
            topTransactionFilter = topFilter,
            transactionTypeFilter = typeFilter,
            isLoading = false,
            hasData = periodTransactions.isNotEmpty()
        )
    }

    // ============================================================
    // Cashflow Points Builder (Line Chart)
    // Granularity: daily for ≤31 days, weekly for >31 days
    // ============================================================
    private fun buildCashflowPoints(
        transactions: List<Transaction>,
        periodStart: Long,
        periodEnd: Long
    ): List<CashflowPoint> {
        val periodDays = (periodEnd - periodStart) / (1000L * 60 * 60 * 24)
        val isDaily = periodDays <= 31

        val cal = Calendar.getInstance()
        val points = mutableListOf<CashflowPoint>()

        if (isDaily) {
            // Daily points
            var current = periodStart
            while (current <= periodEnd) {
                cal.timeInMillis = current
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayStart = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val dayEnd = cal.timeInMillis

                val dayTx = transactions.filter { it.dateMillis in dayStart..dayEnd }
                var income = 0.0
                var expense = 0.0
                dayTx.forEach { tx ->
                    when (tx.type) {
                        TransactionType.INCOME -> income += tx.amount
                        TransactionType.EXPENSE -> expense += tx.amount
                        TransactionType.TRANSFER -> {}
                    }
                }
                points.add(
                    CashflowPoint(
                        label = Formatters.formatShortDateIndo(dayStart),
                        dateMillis = dayStart,
                        income = income,
                        expense = expense
                    )
                )
                current += 24L * 60 * 60 * 1000
            }
        } else {
            // Weekly points
            var current = periodStart
            while (current <= periodEnd) {
                cal.timeInMillis = current
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val weekStart = max(cal.timeInMillis, periodStart)
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val weekEnd = min(cal.timeInMillis, periodEnd)

                val weekTx = transactions.filter { it.dateMillis in weekStart..weekEnd }
                var income = 0.0
                var expense = 0.0
                weekTx.forEach { tx ->
                    when (tx.type) {
                        TransactionType.INCOME -> income += tx.amount
                        TransactionType.EXPENSE -> expense += tx.amount
                        TransactionType.TRANSFER -> {}
                    }
                }
                points.add(
                    CashflowPoint(
                        label = "${Formatters.formatShortDateIndo(weekStart)} - ${Formatters.formatShortDateIndo(weekEnd)}",
                        dateMillis = weekStart,
                        income = income,
                        expense = expense
                    )
                )
                current = weekEnd + 1L
            }
        }

        return points
    }

    // ============================================================
    // Balance Points Builder (Balance Trend Line Chart)
    // Reconstructs balance history by replaying transactions backwards
    // from current asset balances
    // ============================================================
    private fun buildBalancePoints(
        transactions: List<Transaction>,
        assets: List<Asset>,
        periodStart: Long,
        periodEnd: Long
    ): List<BalancePoint> {
        val currentTotalBalance = assets.sumOf { it.balance }

        val allTxSorted = transactions.filter { it.type != TransactionType.TRANSFER }
            .sortedBy { it.dateMillis }

        val periodDays = (periodEnd - periodStart) / (1000L * 60 * 60 * 24)
        val isDaily = periodDays <= 31

        val dailyNetChange = mutableMapOf<Long, Double>()
        allTxSorted.forEach { tx ->
            if (tx.dateMillis <= periodEnd) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = tx.dateMillis
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayKey = cal.timeInMillis
                val change = when (tx.type) {
                    TransactionType.INCOME -> tx.amount
                    TransactionType.EXPENSE -> -tx.amount
                    TransactionType.TRANSFER -> 0.0
                }
                dailyNetChange[dayKey] = (dailyNetChange[dayKey] ?: 0.0) + change
            }
        }

        var balanceAtPeriodEnd = currentTotalBalance
        allTxSorted.filter { it.dateMillis > periodEnd }.forEach { tx ->
            val change = when (tx.type) {
                TransactionType.INCOME -> tx.amount
                TransactionType.EXPENSE -> -tx.amount
                TransactionType.TRANSFER -> 0.0
            }
            balanceAtPeriodEnd -= change
        }

        val points = mutableListOf<BalancePoint>()
        var runningBalance = balanceAtPeriodEnd

        val cal = Calendar.getInstance()
        val format = if (isDaily) {
            { d: Long -> Formatters.formatShortDateIndo(d) }
        } else {
            { d: Long ->
                cal.timeInMillis = d
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val ws = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val we = min(cal.timeInMillis, periodEnd)
                "${Formatters.formatShortDateIndo(ws)} - ${Formatters.formatShortDateIndo(we)}"
            }
        }

        if (isDaily) {
            var current = periodEnd
            while (current >= periodStart) {
                cal.timeInMillis = current
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val dayKey = cal.timeInMillis

                points.add(
                    0,
                    BalancePoint(
                        label = format(dayKey),
                        dateMillis = dayKey,
                        balance = runningBalance
                    )
                )

                val dayChange = dailyNetChange[dayKey] ?: 0.0
                runningBalance -= dayChange

                current -= 24L * 60 * 60 * 1000
            }
        } else {
            var current = periodEnd
            while (current >= periodStart) {
                cal.timeInMillis = current
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val weekStart = max(cal.timeInMillis, periodStart)
                val weekEnd = current

                var weekChange = 0.0
                var checkDay = weekStart
                while (checkDay <= weekEnd) {
                    weekChange += dailyNetChange[checkDay] ?: 0.0
                    checkDay += 24L * 60 * 60 * 1000
                }

                points.add(
                    0,
                    BalancePoint(
                        label = format(weekStart),
                        dateMillis = weekStart,
                        balance = runningBalance
                    )
                )

                runningBalance -= weekChange
                current = weekStart - 1L
            }
        }

        return points
    }

    // ============================================================
    // Top Transactions
    // ============================================================
    private fun getTopTransactions(
        transactions: List<Transaction>,
        periodStart: Long,
        periodEnd: Long,
        topFilter: TopTransactionFilter,
        typeFilter: TransactionTypeFilter
    ): List<TopTransactionItem> {
        var filtered = transactions.filter { tx ->
            tx.dateMillis in periodStart..periodEnd
        }

        when (typeFilter) {
            TransactionTypeFilter.INCOME -> filtered = filtered.filter { it.type == TransactionType.INCOME }
            TransactionTypeFilter.EXPENSE -> filtered = filtered.filter { it.type == TransactionType.EXPENSE }
            TransactionTypeFilter.ALL -> {}
        }

        val limit = when (topFilter) {
            TopTransactionFilter.TOP_5 -> 5
            TopTransactionFilter.TOP_10 -> 10
            TopTransactionFilter.TOP_20 -> 20
        }

        return filtered
            .sortedByDescending { it.amount }
            .take(limit)
            .map { tx ->
                TopTransactionItem(
                    id = tx.id,
                    title = tx.title,
                    categoryName = tx.categoryName,
                    categoryIcon = tx.categoryIcon,
                    dateMillis = tx.dateMillis,
                    amount = tx.amount,
                    type = tx.type
                )
            }
    }
}