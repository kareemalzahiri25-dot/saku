package com.example.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val iconName: String,
    val colorHex: String,
    val isBiometricEnabled: Boolean = false
) {
    val profileInitial: String get() = name.take(1).uppercase()
}

data class Asset(
    val id: String,
    val name: String,
    val balance: Double,
    val type: AssetType = AssetType.BANK,
    val iconName: String = "account_balance",
    val colorHex: String = "#153E35",
    val isDefault: Boolean = false
) {
    val assetId: String get() = id  // Self-reference for Transaction backward compat
}

enum class AssetType {
    BANK, E_WALLET, CASH, INVESTMENT, CRYPTO, OTHER
}

data class Pocket(
    val id: String,
    val name: String,
    val targetAmount: Double = 0.0,
    val color: String = "#153E35",
    val icon: String = "wallet",
    val isActive: Boolean = true,
    val completed: Boolean = false,
    val archived: Boolean = false,
    // Transient helper fields for UI display
    val description: String = ""
) {
    val colorHex: String get() = color
    val iconName: String get() = icon
    val isMain: Boolean get() = false

    constructor(
        id: String,
        name: String,
        targetAmount: Double = 0.0,
        iconName: String = "wallet",
        colorHex: String = "#153E35",
        isMain: Boolean = false,
        description: String = "",
        completed: Boolean = false,
        archived: Boolean = false
    ) : this(
        id = id,
        name = name,
        targetAmount = targetAmount,
        color = colorHex,
        icon = iconName,
        isActive = true,
        completed = completed,
        archived = archived,
        description = description
    )
}

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val assetId: String,
    val assetName: String = "",
    val dateMillis: Long,
    val note: String = "",
    // For TRANSFER type transactions
    val targetAssetId: String? = null,
    val targetAssetName: String? = null,
    // Legacy pocket reference (deprecated - use ExpenseAllocation instead)
    @Deprecated("Use ExpenseAllocation for multi-pocket expense splits")
    val pocketId: String? = null,
    @Deprecated("Use ExpenseAllocation for multi-pocket expense splits")
    val pocketName: String? = null,
    // Legacy transfer fields (deprecated - transfer is now Asset-to-Asset only)
    @Deprecated("Transfer between Pockets is no longer supported")
    val targetPocketId: String? = null,
    @Deprecated("Transfer between Pockets is no longer supported")
    val targetPocketName: String? = null
) {
    constructor(
        id: String,
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: String,
        categoryName: String,
        categoryIcon: String,
        assetId: String,
        assetName: String = "",
        dateMillis: Long,
        note: String = "",
        targetAssetId: String? = null,
        targetAssetName: String? = null
    ) : this(
        id, title, amount, type, categoryId, categoryName, categoryIcon,
        assetId, assetName, dateMillis, note, targetAssetId, targetAssetName,
        pocketId = null, pocketName = null, targetPocketId = null, targetPocketName = null
    )
}

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val type: TransactionType,
    val colorHex: String = "#153E35"
)

/**
 * Represents planned allocation from an Asset to a Pocket.
 * This is pure planning intent - not actual money movement.
 * Planned Allocations are never silently modified by transactions.
 */
data class PocketAllocation(
    val id: String,
    val assetId: String,
    val pocketId: String,
    val allocatedAmount: Double
)

/**
 * Represents an expense allocation to one or more Pockets.
 * An Expense can have 0, 1, or multiple ExpenseAllocations.
 * Sum of ExpenseAllocation.amounts must never exceed the parent Expense.amount.
 * The remainder (Expense.amount - sum(allocations)) is Unallocated.
 */
data class ExpenseAllocation(
    val id: String,
    val transactionId: String,
    val pocketId: String,
    val amount: Double
)

/**
 * Derived Pocket statistics (computed, not stored).
 * 
 * - realization: SUM(ExpenseAllocation.amount) for this Pocket
 * - remainingPlanningNeed: Target - Realization (may be negative if over-realized)
 * - plannedAllocation: SUM(PocketAllocation.allocatedAmount) from all Assets to this Pocket
 * - excessPlanningAllocation: max(0, PlannedAllocation - RemainingPlanningNeed)
 * - fundingShortfall: max(0, PlannedAllocation - Asset.ActualBalance) [informational only]
 */
data class PocketStats(
    val pocketId: String,
    val pocketName: String,
    val targetAmount: Double,
    val realization: Double,
    val plannedAllocation: Double
) {
    val remainingPlanningNeed: Double get() = targetAmount - realization
    val excessPlanningAllocation: Double get() = maxOf(0.0, plannedAllocation - remainingPlanningNeed)
    val progress: Double get() = if (targetAmount > 0) realization / targetAmount else 0.0
}

data class FinancialSummary(
    val totalAssetBalance: Double = 0.0,
    val totalPlannedAllocation: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalIncomeThisMonth: Double = 0.0,
    val totalExpenseThisMonth: Double = 0.0,
    val netSavingsThisMonth: Double = 0.0,
    val savingsRatePercentage: Int = 0
) {
    @Deprecated("Use totalAssetBalance - totalPlannedAllocation is not cash")
    val totalBalance: Double get() = totalAssetBalance
    
    val fundingShortfall: Double get() = maxOf(0.0, totalPlannedAllocation - totalAssetBalance)
}

/**
 * Category expense summary for financial reports.
 */
data class CategoryExpenseSummary(
    val categoryName: String,
    val categoryIcon: String,
    val totalAmount: Double,
    val percentage: Float,
    val colorHex: String = "#153E35"
)

/**
 * Financial report for a specific period.
 */
data class FinancialReport(
    val periodTitle: String,
    val summary: FinancialSummary,
    val categoryBreakdown: List<CategoryExpenseSummary> = emptyList(),
    val transactionCount: Int = 0
)
