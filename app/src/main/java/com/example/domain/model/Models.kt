package com.example.domain.model

enum class TransactionType(val titleIndo: String) {
    EXPENSE("Pengeluaran"),
    INCOME("Pemasukan"),
    TRANSFER("Transfer Dana")
}

enum class AssetType(val titleIndo: String, val defaultIcon: String) {
    BANK("Bank", "account_balance"),
    CASH("Uang Tunai", "payments"),
    EWALLET("E-Wallet", "wallet"),
    INVESTMENT("Investasi", "trending_up"),
    OTHER("Lainnya", "savings")
}

data class Asset(
    val id: String,
    val name: String,
    val type: AssetType = AssetType.BANK,
    val balance: Double,
    val currency: String = "IDR",
    val isActive: Boolean = true
)

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
    val assetId: String = "",
    val allocatedAmount: Double = 0.0,
    val targetAmount: Double = 0.0,
    val color: String = "#153E35",
    val icon: String = "wallet",
    val isActive: Boolean = true,
    // Transient helper fields for UI display
    val assetName: String = "",
    val description: String = ""
) {
    val colorHex: String get() = color
    val iconName: String get() = icon
    // A Pocket does not have independent money; balance represents allocated budget
    val balance: Double get() = allocatedAmount
    val isMain: Boolean get() = false

    constructor(
        id: String,
        name: String,
        balance: Double,
        targetAmount: Double = 0.0,
        iconName: String = "wallet",
        colorHex: String = "#153E35",
        isMain: Boolean = false,
        description: String = "",
        assetId: String = ""
    ) : this(
        id = id,
        name = name,
        assetId = assetId,
        allocatedAmount = balance,
        targetAmount = targetAmount,
        color = colorHex,
        icon = iconName,
        isActive = true,
        assetName = "",
        description = description
    )
}

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val categoryName: String = "",
    val categoryIcon: String = "receipt_long",
    val assetId: String = "",
    val assetName: String = "",
    val pocketId: String? = null,
    val pocketName: String? = null,
    val targetAssetId: String? = null,
    val targetAssetName: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val receiptImageUrl: String? = null
) {
    val merchant: String get() = title
    val date: Long get() = dateMillis
    // Backward compatibility aliases
    val targetPocketId: String? get() = targetAssetId
    val targetPocketName: String? get() = targetAssetName

    constructor(
        id: String,
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: String,
        categoryName: String = "",
        categoryIcon: String = "receipt_long",
        pocketId: String? = null,
        pocketName: String? = null,
        targetPocketId: String? = null,
        targetPocketName: String? = null,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = "",
        receiptImageUrl: String? = null,
        assetId: String = pocketId ?: ""
    ) : this(
        id = id,
        title = title,
        amount = amount,
        type = type,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        assetId = if (assetId.isNotBlank()) assetId else (pocketId ?: ""),
        assetName = "",
        pocketId = pocketId,
        pocketName = pocketName,
        targetAssetId = targetPocketId,
        targetAssetName = targetPocketName,
        dateMillis = dateMillis,
        note = note,
        receiptImageUrl = receiptImageUrl
    )
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isBiometricEnabled: Boolean = false,
    val currencyCode: String = "IDR",
    val joinedDateMillis: Long = System.currentTimeMillis()
)

data class FinancialSummary(
    val totalActualBalance: Double = 0.0,        // Saldo Asli = SUM(Asset.balance)
    val totalAllocatedAmount: Double = 0.0,      // Dana Dialokasikan = SUM(Pocket.allocatedAmount)
    val totalAvailableBalance: Double = 0.0,      // Saldo Tersedia = Saldo Asli - Dana Dialokasikan
    val totalIncomeThisMonth: Double = 0.0,
    val totalExpenseThisMonth: Double = 0.0,
    val netSavingsThisMonth: Double = 0.0,
    val savingsRatePercentage: Int = 0
) {
    constructor(
        totalBalance: Double,
        totalIncomeThisMonth: Double,
        totalExpenseThisMonth: Double,
        netSavingsThisMonth: Double,
        savingsRatePercentage: Int
    ) : this(
        totalActualBalance = totalBalance,
        totalAllocatedAmount = 0.0,
        totalAvailableBalance = totalBalance,
        totalIncomeThisMonth = totalIncomeThisMonth,
        totalExpenseThisMonth = totalExpenseThisMonth,
        netSavingsThisMonth = netSavingsThisMonth,
        savingsRatePercentage = savingsRatePercentage
    )

    // Backward compatibility for existing UI references
    val totalBalance: Double get() = totalActualBalance
}

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
