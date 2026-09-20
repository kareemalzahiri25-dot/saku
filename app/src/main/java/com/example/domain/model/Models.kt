package com.example.domain.model

enum class TransactionType(val titleIndo: String) {
    EXPENSE("Pengeluaran"),
    INCOME("Pemasukan"),
    TRANSFER("Transfer Dana")
}

data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorHex: String
)

data class Pocket(
    val id: String,
    val name: String,
    val balance: Double,
    val targetAmount: Double = 0.0,
    val iconName: String = "wallet",
    val colorHex: String = "#153E35",
    val isMain: Boolean = false,
    val description: String = ""
)

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val pocketId: String,
    val pocketName: String,
    val targetPocketId: String? = null,
    val targetPocketName: String? = null,
    val dateMillis: Long,
    val note: String = "",
    val receiptImageUrl: String? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isBiometricEnabled: Boolean = false,
    val currencyCode: String = "IDR",
    val joinedDateMillis: Long = System.currentTimeMillis()
)

data class FinancialSummary(
    val totalBalance: Double,
    val totalIncomeThisMonth: Double,
    val totalExpenseThisMonth: Double,
    val netSavingsThisMonth: Double,
    val savingsRatePercentage: Int
)

data class CategoryExpenseSummary(
    val categoryName: String,
    val categoryIcon: String,
    val totalAmount: Double,
    val percentage: Float,
    val colorHex: String
)

data class FinancialReport(
    val periodTitle: String,
    val summary: FinancialSummary,
    val categoryBreakdown: List<CategoryExpenseSummary>,
    val transactionCount: Int
)
