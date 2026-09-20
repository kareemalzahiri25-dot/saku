package com.example.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.Formatters
import com.example.domain.model.CategoryExpenseSummary
import com.example.domain.model.FinancialReport
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ReportPeriod(val titleIndo: String, val monthOffset: Int = 0) {
    THIS_MONTH("Bulan Ini", 0),
    THREE_MONTHS("3 Bulan", 0),
    SIX_MONTHS("6 Bulan", 0),
    THIS_YEAR("Tahun Ini", 0),
    CUSTOM("Kustom", -1),
    LAST_MONTH("Bulan Lalu", -1)
}

data class MonthlyCashflow(
    val monthLabel: String,      // "Jun", "Jul", "Agt", "Sep"
    val fullMonthLabel: String,  // "Juni 2026"
    val year: Int,
    val monthIndex: Int,         // 0..11
    val income: Double,
    val expense: Double,
    val net: Double,
    val savingsRate: Int
)

data class DonutCategorySegment(
    val categoryName: String,
    val categoryIcon: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: String,
    val isOther: Boolean = false
)

data class FinancialInsights(
    val savingsInsight: String,
    val topCategoryInsight: String?,
    val cashflowTrendInsight: String?,
    val recommendation: String,
    val isSurplus: Boolean
)

data class ReportUiState(
    val period: ReportPeriod = ReportPeriod.THIS_MONTH,
    val customMonthOffset: Int = -1, // Defaults to 1 month ago for custom
    val periodTitle: String = "Bulan Ini",
    val chartSummaryTitle: String = "Tren 4 Bulan Terakhir",
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netCashflow: Double = 0.0,
    val savingsRatePercentage: Int = 0,
    val transactionCount: Int = 0,
    val monthlyCashflows: List<MonthlyCashflow> = emptyList(),
    val categoryBreakdown: List<CategoryExpenseSummary> = emptyList(),
    val donutSegments: List<DonutCategorySegment> = emptyList(),
    val selectedCategoryName: String? = null,
    val selectedMonthIndex: Int? = null,
    val insights: FinancialInsights? = null,
    val isExportDialogOpen: Boolean = false,
    val exportedCsv: String? = null,
    val exportStatusMessage: String? = null,
    val isExporting: Boolean = false
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

    private val _customMonthOffset = MutableStateFlow(-1)
    private val _selectedCategoryName = MutableStateFlow<String?>(null)
    private val _selectedMonthIndex = MutableStateFlow<Int?>(null)
    private val _isExportDialogOpen = MutableStateFlow(false)
    private val _exportedCsv = MutableStateFlow<String?>(null)
    private val _exportStatusMessage = MutableStateFlow<String?>(null)
    private val _isExporting = MutableStateFlow(false)

    private data class FilterParams(
        val period: ReportPeriod,
        val customOffset: Int,
        val selectedCategory: String?,
        val selectedMonth: Int?
    )

    val uiState: StateFlow<ReportUiState> = combine(
        combine(
            repository.getAllTransactions(),
            repository.getAllPockets()
        ) { txs, pks -> Pair(txs, pks) },
        combine(
            _selectedPeriod,
            _customMonthOffset,
            _selectedCategoryName,
            _selectedMonthIndex
        ) { period, customOffset, selCat, selMonth ->
            FilterParams(period, customOffset, selCat, selMonth)
        },
        combine(
            _isExportDialogOpen,
            _exportedCsv,
            _exportStatusMessage,
            _isExporting
        ) { isOpen, csv, msg, exporting ->
            ExportState(isOpen, csv, msg, exporting)
        }
    ) { (transactions, pockets), filters, exportState ->
        val baseState = calculateReportUiState(
            transactions = transactions,
            pockets = pockets,
            period = filters.period,
            customOffset = filters.customOffset,
            selectedCategory = filters.selectedCategory,
            selectedMonth = filters.selectedMonth
        )
        baseState.copy(
            isExportDialogOpen = exportState.isOpen,
            exportedCsv = exportState.csv,
            exportStatusMessage = exportState.msg,
            isExporting = exportState.exporting
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportUiState()
    )

    val report: StateFlow<FinancialReport> = uiState.map { state ->
        FinancialReport(
            periodTitle = state.periodTitle,
            summary = FinancialSummary(
                totalBalance = state.totalBalance,
                totalIncomeThisMonth = state.totalIncome,
                totalExpenseThisMonth = state.totalExpense,
                netSavingsThisMonth = state.netCashflow,
                savingsRatePercentage = state.savingsRatePercentage
            ),
            categoryBreakdown = state.categoryBreakdown,
            transactionCount = state.transactionCount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialReport(
            periodTitle = "Periode Berjalan",
            summary = FinancialSummary(0.0, 0.0, 0.0, 0.0, 0),
            categoryBreakdown = emptyList(),
            transactionCount = 0
        )
    )

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
        _selectedCategoryName.value = null
        _selectedMonthIndex.value = null
    }

    fun selectCustomMonth(offset: Int) {
        _customMonthOffset.value = offset
        _selectedPeriod.value = ReportPeriod.CUSTOM
        _selectedCategoryName.value = null
        _selectedMonthIndex.value = null
    }

    fun toggleCategorySelection(categoryName: String) {
        if (_selectedCategoryName.value == categoryName) {
            _selectedCategoryName.value = null
        } else {
            _selectedCategoryName.value = categoryName
        }
    }

    fun selectMonthBar(monthIndex: Int?) {
        _selectedMonthIndex.value = monthIndex
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _isExporting.value = true
            val csv = repository.exportDataAsCsv()
            _exportedCsv.value = csv
            _isExportDialogOpen.value = true
            _exportStatusMessage.value = "Data CSV berhasil diekspor!"
            _isExporting.value = false
        }
    }

    fun closeExportDialog() {
        _isExportDialogOpen.value = false
        _exportStatusMessage.value = null
    }

    private data class ExportState(
        val isOpen: Boolean,
        val csv: String?,
        val msg: String?,
        val exporting: Boolean
    )

    private fun calculateReportUiState(
        transactions: List<Transaction>,
        pockets: List<Pocket>,
        period: ReportPeriod,
        customOffset: Int,
        selectedCategory: String?,
        selectedMonth: Int?
    ): ReportUiState {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) // 0..11

        val totalBalance = pockets.sumOf { it.balance }

        // Determine months to include in the monthly cashflow trend chart
        val trendMonths = mutableListOf<Pair<Int, Int>>() // Pair(year, monthIndex)
        val periodMonths = mutableListOf<Pair<Int, Int>>()
        var periodTitle = ""
        var chartSummaryTitle = ""

        when (period) {
            ReportPeriod.THIS_MONTH -> {
                periodMonths.add(Pair(currentYear, currentMonth))
                periodTitle = formatMonthYear(currentMonth, currentYear)
                chartSummaryTitle = "Tren 4 Bulan Terakhir"

                // Show last 4 months in cashflow trend
                for (offset in 3 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -offset)
                    trendMonths.add(Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)))
                }
            }
            ReportPeriod.LAST_MONTH -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -1)
                val yr = cal.get(Calendar.YEAR)
                val mo = cal.get(Calendar.MONTH)
                periodMonths.add(Pair(yr, mo))
                periodTitle = formatMonthYear(mo, yr)
                chartSummaryTitle = "Tren 4 Bulan (Bulan Lalu)"

                for (offset in 3 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.MONTH, -1 - offset)
                    trendMonths.add(Pair(c.get(Calendar.YEAR), c.get(Calendar.MONTH)))
                }
            }
            ReportPeriod.THREE_MONTHS -> {
                chartSummaryTitle = "Tren 3 Bulan Terakhir"
                for (offset in 2 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -offset)
                    val p = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
                    periodMonths.add(p)
                    trendMonths.add(p)
                }
                val first = periodMonths.first()
                val last = periodMonths.last()
                periodTitle = "${formatShortMonth(first.second)} - ${formatShortMonth(last.second)} ${last.first}"
            }
            ReportPeriod.SIX_MONTHS -> {
                chartSummaryTitle = "Tren 6 Bulan Terakhir"
                for (offset in 5 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -offset)
                    val p = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
                    periodMonths.add(p)
                    trendMonths.add(p)
                }
                val first = periodMonths.first()
                val last = periodMonths.last()
                periodTitle = "${formatShortMonth(first.second)} - ${formatShortMonth(last.second)} ${last.first}"
            }
            ReportPeriod.THIS_YEAR -> {
                chartSummaryTitle = "Tren Tahun $currentYear"
                for (m in 0..currentMonth) {
                    val p = Pair(currentYear, m)
                    periodMonths.add(p)
                    trendMonths.add(p)
                }
                periodTitle = "Tahun $currentYear"
            }
            ReportPeriod.CUSTOM -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, customOffset)
                val yr = cal.get(Calendar.YEAR)
                val mo = cal.get(Calendar.MONTH)
                periodMonths.add(Pair(yr, mo))
                periodTitle = formatMonthYear(mo, yr)
                chartSummaryTitle = "Tren 4 Bulan Terpilih"

                for (offset in 3 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.MONTH, customOffset - offset)
                    trendMonths.add(Pair(c.get(Calendar.YEAR), c.get(Calendar.MONTH)))
                }
            }
        }

        // Calculate period aggregated totals
        val periodTransactions = transactions.filter { tx ->
            isTransactionInMonths(tx.dateMillis, periodMonths)
        }

        var totalIncome = 0.0
        var totalExpense = 0.0
        val categoryExpenses = mutableMapOf<String, Double>()
        val categoryIcons = mutableMapOf<String, String>()

        periodTransactions.forEach { tx ->
            when (tx.type) {
                TransactionType.INCOME -> totalIncome += tx.amount
                TransactionType.EXPENSE -> {
                    totalExpense += tx.amount
                    categoryExpenses[tx.categoryName] = (categoryExpenses[tx.categoryName] ?: 0.0) + tx.amount
                    categoryIcons[tx.categoryName] = tx.categoryIcon
                }
                TransactionType.TRANSFER -> { /* Internal pocket transfer */ }
            }
        }

        val netCashflow = totalIncome - totalExpense
        val savingsRatePercentage = if (totalIncome > 0) {
            ((netCashflow / totalIncome) * 100).toInt().coerceIn(0, 100)
        } else 0

        // Calculate Category Breakdown
        val categoryBreakdown = categoryExpenses.map { (name, amount) ->
            val pct = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
            CategoryExpenseSummary(
                categoryName = name,
                categoryIcon = categoryIcons[name] ?: "category",
                totalAmount = amount,
                percentage = pct,
                colorHex = "#133E35"
            )
        }.sortedByDescending { it.totalAmount }

        // Build Donut Segments (Top 4 individual + Lainnya if > 4)
        val donutSegments = mutableListOf<DonutCategorySegment>()
        if (categoryBreakdown.size <= 4) {
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
            val top4 = categoryBreakdown.take(4)
            top4.forEachIndexed { index, cat ->
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
            val others = categoryBreakdown.drop(4)
            val othersTotal = others.sumOf { it.totalAmount }
            val othersPct = if (totalExpense > 0) (othersTotal / totalExpense).toFloat() else 0f
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

        // Calculate Monthly Cashflows for Trend Charts (Bar & Savings Rate)
        val monthlyCashflows = trendMonths.map { (yr, mo) ->
            val monthTx = transactions.filter { isTransactionInMonth(it.dateMillis, yr, mo) }
            var moIncome = 0.0
            var moExpense = 0.0
            monthTx.forEach { tx ->
                when (tx.type) {
                    TransactionType.INCOME -> moIncome += tx.amount
                    TransactionType.EXPENSE -> moExpense += tx.amount
                    TransactionType.TRANSFER -> {}
                }
            }
            val moNet = moIncome - moExpense
            val moRate = if (moIncome > 0) ((moNet / moIncome) * 100).toInt().coerceIn(0, 100) else 0

            MonthlyCashflow(
                monthLabel = formatShortMonth(mo),
                fullMonthLabel = formatMonthYear(mo, yr),
                year = yr,
                monthIndex = mo,
                income = moIncome,
                expense = moExpense,
                net = moNet,
                savingsRate = moRate
            )
        }

        // Generate Financial Insights from actual data
        val insights = generateFinancialInsights(
            savingsRate = savingsRatePercentage,
            netCashflow = netCashflow,
            totalExpense = totalExpense,
            donutSegments = donutSegments,
            monthlyCashflows = monthlyCashflows
        )

        return ReportUiState(
            period = period,
            customMonthOffset = customOffset,
            periodTitle = periodTitle,
            chartSummaryTitle = chartSummaryTitle,
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netCashflow = netCashflow,
            savingsRatePercentage = savingsRatePercentage,
            transactionCount = periodTransactions.size,
            monthlyCashflows = monthlyCashflows,
            categoryBreakdown = categoryBreakdown,
            donutSegments = donutSegments,
            selectedCategoryName = selectedCategory,
            selectedMonthIndex = selectedMonth,
            insights = insights
        )
    }

    private fun generateFinancialInsights(
        savingsRate: Int,
        netCashflow: Double,
        totalExpense: Double,
        donutSegments: List<DonutCategorySegment>,
        monthlyCashflows: List<MonthlyCashflow>
    ): FinancialInsights {
        // 1. Savings Rate Insight
        val savingsInsight = when {
            savingsRate >= 20 ->
                "Rasio tabungan Anda mencapai $savingsRate%, melampaui standar anjuran 20%. Disiplin finansial berada di jalur yang sangat sehat."
            savingsRate > 0 ->
                "Rasio tabungan saat ini $savingsRate% (di bawah target ideal 20%). Batasi pos keinginan untuk meningkatkan porsi tabungan."
            else ->
                "Arus kas periode ini belum memiliki simpanan bersih (surplus 0% atau defisit). Perlu evaluasi menyeluruh terhadap pos pengeluaran."
        }

        // 2. Top Category Insight
        val topCategoryInsight = if (donutSegments.isNotEmpty() && totalExpense > 0) {
            val top = donutSegments.first()
            val pctStr = (top.percentage * 100).toInt()
            "Pos belanja terbesar adalah ${top.categoryName} sebesar ${Formatters.formatRupiah(top.amount)} ($pctStr% dari total pengeluaran)."
        } else null

        // 3. Cashflow Trend Insight (comparing last 2 months if available)
        val cashflowTrendInsight = if (monthlyCashflows.size >= 2) {
            val current = monthlyCashflows.last()
            val prev = monthlyCashflows[monthlyCashflows.size - 2]
            val diff = current.expense - prev.expense
            when {
                diff < -50000 -> "Pengeluaran bulan ini lebih hemat ${Formatters.formatRupiah(-diff)} dibanding bulan sebelumnya."
                diff > 50000 -> "Pengeluaran meningkat ${Formatters.formatRupiah(diff)} dibanding bulan sebelumnya."
                else -> "Pengeluaran relatif stabil dibanding bulan sebelumnya."
            }
        } else null

        // 4. Recommendation
        val recommendation = if (netCashflow > 0) {
            "Surplus bersih sebesar ${Formatters.formatRupiah(netCashflow)} siap dialokasikan ke Kantong Tabungan atau Dana Darurat."
        } else {
            "Disarankan menyusun batas anggaran harian agar arus kas kembali seimbang dan surplus."
        }

        return FinancialInsights(
            savingsInsight = savingsInsight,
            topCategoryInsight = topCategoryInsight,
            cashflowTrendInsight = cashflowTrendInsight,
            recommendation = recommendation,
            isSurplus = netCashflow >= 0
        )
    }

    private fun isTransactionInMonth(dateMillis: Long, year: Int, month: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
    }

    private fun isTransactionInMonths(dateMillis: Long, months: List<Pair<Int, Int>>): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        return months.any { it.first == y && it.second == m }
    }

    private fun formatMonthYear(month: Int, year: Int): String {
        val names = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val name = if (month in names.indices) names[month] else "Bulan"
        return "$name $year"
    }

    private fun formatShortMonth(month: Int): String {
        val names = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agt", "Sep", "Okt", "Nov", "Des")
        return if (month in names.indices) names[month] else "Bln"
    }
}
