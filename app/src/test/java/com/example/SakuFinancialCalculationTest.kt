package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SakuDatabase
import com.example.data.repository.SakuRepositoryImpl
import com.example.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SakuFinancialCalculationTest {

    private lateinit var db: SakuDatabase
    private lateinit var repository: SakuRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SakuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SakuRepositoryImpl(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    // ========================================
    // 1. ASSET BALANCE CALCULATIONS
    // ========================================

    @Test
    fun testIncomeIncreasesAssetBalance() = runBlocking {
        // Setup: Create asset with initial balance
        val asset = Asset(
            id = "asset_1",
            name = "Bank BCA",
            balance = 1_000_000.0,
            type = AssetType.BANK
        )
        repository.insertAsset(asset)

        // Action: Add income transaction
        val income = Transaction(
            id = "tx_1",
            title = "Gaji",
            amount = 5_000_000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_1",
            categoryName = "Gaji",
            categoryIcon = "payments",
            assetId = "asset_1",
            assetName = "Bank BCA",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(income)

        // Verify: Asset balance increased
        val updatedAsset = repository.getAssetById("asset_1")
        assertEquals(6_000_000.0, updatedAsset?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testExpenseDecreasesAssetBalance() = runBlocking {
        // Setup: Create asset with initial balance
        val asset = Asset(
            id = "asset_2",
            name = "E-Wallet",
            balance = 500_000.0,
            type = AssetType.E_WALLET
        )
        repository.insertAsset(asset)

        // Action: Add expense transaction
        val expense = Transaction(
            id = "tx_2",
            title = "Makan",
            amount = 50_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_2",
            categoryName = "Makanan",
            categoryIcon = "restaurant",
            assetId = "asset_2",
            assetName = "E-Wallet",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(expense)

        // Verify: Asset balance decreased
        val updatedAsset = repository.getAssetById("asset_2")
        assertEquals(450_000.0, updatedAsset?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testTransferMovesBetweenAssets() = runBlocking {
        // Setup: Create two assets
        val sourceAsset = Asset(
            id = "asset_3",
            name = "Bank BCA",
            balance = 1_000_000.0,
            type = AssetType.BANK
        )
        val targetAsset = Asset(
            id = "asset_4",
            name = "E-Wallet",
            balance = 100_000.0,
            type = AssetType.E_WALLET
        )
        repository.insertAsset(sourceAsset)
        repository.insertAsset(targetAsset)

        // Action: Transfer between assets
        val transfer = Transaction(
            id = "tx_3",
            title = "Transfer",
            amount = 200_000.0,
            type = TransactionType.TRANSFER,
            categoryId = "cat_transfer",
            categoryName = "Transfer",
            categoryIcon = "swap_horiz",
            assetId = "asset_3",
            assetName = "Bank BCA",
            targetAssetId = "asset_4",
            targetAssetName = "E-Wallet",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(transfer)

        // Verify: Source decreased, target increased
        val updatedSource = repository.getAssetById("asset_3")
        val updatedTarget = repository.getAssetById("asset_4")
        assertEquals(800_000.0, updatedSource?.balance ?: 0.0, 0.001)
        assertEquals(300_000.0, updatedTarget?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testTotalAssetBalanceIsSumOfActiveAssets() = runBlocking {
        // Setup: Create multiple assets
        val asset1 = Asset(id = "a1", name = "Bank", balance = 2_000_000.0, type = AssetType.BANK, isDefault = false)
        val asset2 = Asset(id = "a2", name = "Wallet", balance = 500_000.0, type = AssetType.E_WALLET, isDefault = false)
        repository.insertAsset(asset1)
        repository.insertAsset(asset2)

        // Verify: Total equals sum
        val summary = repository.getFinancialSummary().first()
        assertEquals(2_500_000.0, summary.totalAssetBalance, 0.001)
    }

    // ========================================
    // 2. POCKET STATS CALCULATIONS
    // ========================================

    @Test
    fun testPocketRealizationFromExpenseAllocation() = runBlocking {
        // Setup: Create pocket and asset
        val pocket = Pocket(
            id = "p_1",
            name = "Liburan",
            targetAmount = 10_000_000.0,
            color = "#153E35",
            icon = "flight"
        )
        repository.insertPocket(pocket)

        val asset = Asset(id = "a_1", name = "Bank", balance = 10_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        // Create expense with pocket allocation
        val expense = Transaction(
            id = "tx_1",
            title = "Tiket Pesawat",
            amount = 2_000_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_1",
            categoryName = "Transportasi",
            categoryIcon = "flight",
            assetId = "a_1",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis(),
            pocketId = "p_1"
        )
        repository.insertTransaction(expense)

        // Verify: Realization computed from ExpenseAllocation
        val stats = repository.getPocketStats().first()
        val pocketStats = stats.find { it.pocketId == "p_1" }
        assertEquals(2_000_000.0, pocketStats?.realization ?: 0.0, 0.001)
    }

    @Test
    fun testPocketPlannedAllocationFromPocketAllocation() = runBlocking {
        // Setup: Create pocket and asset
        val pocket = Pocket(
            id = "p_2",
            name = "Emergency Fund",
            targetAmount = 50_000_000.0,
            color = "#153E35",
            icon = "savings"
        )
        repository.insertPocket(pocket)

        val asset = Asset(id = "a_2", name = "Bank", balance = 100_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        // Create pocket allocation (planned funding)
        val allocation = PocketAllocation(
            id = "pa_1",
            assetId = "a_2",
            pocketId = "p_2",
            allocatedAmount = 30_000_000.0
        )
        repository.insertPocketAllocation(allocation)

        // Verify: Planned allocation computed
        val stats = repository.getPocketStats().first()
        val pocketStats = stats.find { it.pocketId == "p_2" }
        assertEquals(30_000_000.0, pocketStats?.plannedAllocation ?: 0.0, 0.001)
    }

    @Test
    fun testPocketRemainingPlanningNeedCalculation() = runBlocking {
        // Setup: Pocket with target, some realization
        val pocket = Pocket(
            id = "p_3",
            name = "Gadget",
            targetAmount = 15_000_000.0,
            color = "#153E35",
            icon = "phone_android"
        )
        repository.insertPocket(pocket)

        val asset = Asset(id = "a_3", name = "Bank", balance = 20_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        // Create expense with pocket allocation (realization = 5M)
        val expense = Transaction(
            id = "tx_1",
            title = "Beli HP",
            amount = 5_000_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_1",
            categoryName = "Elektronik",
            categoryIcon = "phone_android",
            assetId = "a_3",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis(),
            pocketId = "p_3"
        )
        repository.insertTransaction(expense)

        // Verify: remainingPlanningNeed = target - realization = 10M
        val stats = repository.getPocketStats().first()
        val pocketStats = stats.find { it.pocketId == "p_3" }
        assertEquals(15_000_000.0, pocketStats?.targetAmount ?: 0.0, 0.001)
        assertEquals(5_000_000.0, pocketStats?.realization ?: 0.0, 0.001)
        assertEquals(10_000_000.0, pocketStats?.remainingPlanningNeed ?: 0.0, 0.001)
    }

    // ========================================
    // 3. FINANCIAL SUMMARY CALCULATIONS
    // ========================================

    @Test
    fun testTotalIncomeIsCumulative() = runBlocking {
        val asset = Asset(id = "a_1", name = "Bank", balance = 10_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        // Create multiple income transactions
        repeat(3) { i ->
            val income = Transaction(
                id = "tx_income_$i",
                title = "Gaji $i",
                amount = 5_000_000.0,
                type = TransactionType.INCOME,
                categoryId = "cat_1",
                categoryName = "Gaji",
                categoryIcon = "payments",
                assetId = "a_1",
                assetName = "Bank",
                dateMillis = System.currentTimeMillis() - (i * 86400000L) // Different days
            )
            repository.insertTransaction(income)
        }

        // Verify: Total income = 15M (3 x 5M)
        val summary = repository.getFinancialSummary().first()
        assertEquals(15_000_000.0, summary.totalIncome, 0.001)
    }

    @Test
    fun testTotalExpenseIsCumulative() = runBlocking {
        val asset = Asset(id = "a_2", name = "Bank", balance = 50_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        // Create multiple expense transactions
        val amounts = listOf(100_000.0, 250_000.0, 50_000.0)
        amounts.forEachIndexed { i, amt ->
            val expense = Transaction(
                id = "tx_expense_$i",
                title = "Expense $i",
                amount = amt,
                type = TransactionType.EXPENSE,
                categoryId = "cat_2",
                categoryName = "Makanan",
                categoryIcon = "restaurant",
                assetId = "a_2",
                assetName = "Bank",
                dateMillis = System.currentTimeMillis()
            )
            repository.insertTransaction(expense)
        }

        // Verify: Total expense = sum of all expenses
        val summary = repository.getFinancialSummary().first()
        assertEquals(400_000.0, summary.totalExpense, 0.001)
    }

    @Test
    fun testThisMonthCalculationsAreCorrect() = runBlocking {
        val asset = Asset(id = "a_3", name = "Bank", balance = 100_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 15) // Middle of current month
        val thisMonthTime = cal.timeInMillis

        cal.add(java.util.Calendar.MONTH, -1) // Last month
        val lastMonthTime = cal.timeInMillis

        // This month income
        val incomeThisMonth = Transaction(
            id = "tx_this_income",
            title = "Gaji Bulan Ini",
            amount = 10_000_000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_1",
            categoryName = "Gaji",
            categoryIcon = "payments",
            assetId = "a_3",
            assetName = "Bank",
            dateMillis = thisMonthTime
        )
        repository.insertTransaction(incomeThisMonth)

        // Last month income (should not count in this month)
        val incomeLastMonth = Transaction(
            id = "tx_last_income",
            title = "Gaji Bulan Lalu",
            amount = 8_000_000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_1",
            categoryName = "Gaji",
            categoryIcon = "payments",
            assetId = "a_3",
            assetName = "Bank",
            dateMillis = lastMonthTime
        )
        repository.insertTransaction(incomeLastMonth)

        val summary = repository.getFinancialSummary().first()
        // This month income should be 10M (not 18M)
        assertEquals(10_000_000.0, summary.totalIncomeThisMonth, 0.001)
        // Total income should be 18M (cumulative)
        assertEquals(18_000_000.0, summary.totalIncome, 0.001)
    }

    @Test
    fun testNetSavingsAndRateCalculation() = runBlocking {
        val asset = Asset(id = "a_4", name = "Bank", balance = 100_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        // This month: 10M income, 7M expense
        val income = Transaction(
            id = "tx_income",
            title = "Gaji",
            amount = 10_000_000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_1",
            categoryName = "Gaji",
            categoryIcon = "payments",
            assetId = "a_4",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(income)

        val expense = Transaction(
            id = "tx_expense",
            title = "Belanja",
            amount = 7_000_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_2",
            categoryName = "Belanja",
            categoryIcon = "shopping_bag",
            assetId = "a_4",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(expense)

        val summary = repository.getFinancialSummary().first()
        // Net savings = 10M - 7M = 3M
        assertEquals(3_000_000.0, summary.netSavingsThisMonth, 0.001)
        // Savings rate = 3M / 10M = 30%
        assertEquals(30, summary.savingsRatePercentage)
    }

    // ========================================
    // 4. EXPENSE ALLOCATION TESTS
    // ========================================

    @Test
    fun testSinglePocketExpenseAllocation() = runBlocking {
        val asset = Asset(id = "a_1", name = "Bank", balance = 10_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        val pocket = Pocket(
            id = "p_1",
            name = "Liburan",
            targetAmount = 5_000_000.0,
            color = "#153E35",
            icon = "flight"
        )
        repository.insertPocket(pocket)

        // Create expense with single pocket
        val expense = Transaction(
            id = "tx_1",
            title = "Tiket",
            amount = 1_500_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_1",
            categoryName = "Transportasi",
            categoryIcon = "flight",
            assetId = "a_1",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis(),
            pocketId = "p_1"
        )
        repository.insertTransaction(expense)

        // Verify: One expense allocation created
        val allocations = repository.getExpenseAllocations("tx_1").first()
        assertEquals(1, allocations.size)
        assertEquals(1_500_000.0, allocations[0].amount, 0.001)
        assertEquals("p_1", allocations[0].pocketId)
    }

    @Test
    fun testMultiPocketExpenseAllocation() = runBlocking {
        val asset = Asset(id = "a_2", name = "Bank", balance = 20_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        val pocket1 = Pocket(id = "p_1", name = "Liburan", targetAmount = 10_000_000.0, color = "#153E35", icon = "flight")
        val pocket2 = Pocket(id = "p_2", name = "Gadget", targetAmount = 15_000_000.0, color = "#153E35", icon = "phone")
        repository.insertPocket(pocket1)
        repository.insertPocket(pocket2)

        // Create expense without pocket (will be allocated later)
        val expense = Transaction(
            id = "tx_2",
            title = "Belanja Online",
            amount = 3_000_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_1",
            categoryName = "Belanja",
            categoryIcon = "shopping_bag",
            assetId = "a_2",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(expense)

        // Manually create multiple allocations (simulating multi-pocket split)
        val alloc1 = ExpenseAllocation(
            id = "ea_1",
            transactionId = "tx_2",
            pocketId = "p_1",
            amount = 1_000_000.0
        )
        val alloc2 = ExpenseAllocation(
            id = "ea_2",
            transactionId = "tx_2",
            pocketId = "p_2",
            amount = 1_500_000.0
        )
        repository.insertExpenseAllocation(alloc1)
        repository.insertExpenseAllocation(alloc2)

        // Verify: Both allocations exist
        val allocations = repository.getExpenseAllocations("tx_2").first()
        assertEquals(2, allocations.size)
        val totalAllocated = allocations.sumOf { it.amount }
        assertEquals(2_500_000.0, totalAllocated, 0.001)

        // Verify: Each pocket's realization is updated
        val stats = repository.getPocketStats().first()
        val p1Stats = stats.find { it.pocketId == "p_1" }
        val p2Stats = stats.find { it.pocketId == "p_2" }
        assertEquals(1_000_000.0, p1Stats?.realization ?: 0.0, 0.001)
        assertEquals(1_500_000.0, p2Stats?.realization ?: 0.0, 0.001)
    }

    @Test
    fun testExpenseAllocationCannotExceedTransactionAmount() = runBlocking {
        val asset = Asset(id = "a_3", name = "Bank", balance = 10_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        val pocket = Pocket(id = "p_1", name = "Test", targetAmount = 5_000_000.0, color = "#153E35", icon = "wallet")
        repository.insertPocket(pocket)

        // Create expense of 1M
        val expense = Transaction(
            id = "tx_3",
            title = "Test",
            amount = 1_000_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_1",
            categoryName = "Test",
            categoryIcon = "test",
            assetId = "a_3",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(expense)

        // Try to allocate 1.5M (should fail)
        val alloc = ExpenseAllocation(
            id = "ea_3",
            transactionId = "tx_3",
            pocketId = "p_1",
            amount = 1_500_000.0
        )

        var exceptionThrown = false
        try {
            repository.insertExpenseAllocation(alloc)
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    // ========================================
    // 5. DELETE TRANSACTION REVERSAL TESTS
    // ========================================

    @Test
    fun testDeleteIncomeReversesAssetBalance() = runBlocking {
        val asset = Asset(id = "a_1", name = "Bank", balance = 5_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        val income = Transaction(
            id = "tx_1",
            title = "Gaji",
            amount = 3_000_000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_1",
            categoryName = "Gaji",
            categoryIcon = "payments",
            assetId = "a_1",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(income)

        // After insert: balance = 8M
        assertEquals(8_000_000.0, repository.getAssetById("a_1")?.balance ?: 0.0, 0.001)

        // Delete transaction
        repository.deleteTransaction("tx_1")

        // After delete: balance back to 5M
        assertEquals(5_000_000.0, repository.getAssetById("a_1")?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testDeleteExpenseReversesAssetBalance() = runBlocking {
        val asset = Asset(id = "a_2", name = "Bank", balance = 5_000_000.0, type = AssetType.BANK)
        repository.insertAsset(asset)

        val expense = Transaction(
            id = "tx_2",
            title = "Makan",
            amount = 500_000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_1",
            categoryName = "Makanan",
            categoryIcon = "restaurant",
            assetId = "a_2",
            assetName = "Bank",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(expense)

        // After insert: balance = 4.5M
        assertEquals(4_500_000.0, repository.getAssetById("a_2")?.balance ?: 0.0, 0.001)

        // Delete transaction
        repository.deleteTransaction("tx_2")

        // After delete: balance back to 5M
        assertEquals(5_000_000.0, repository.getAssetById("a_2")?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testDeleteTransferReversesBothAssets() = runBlocking {
        val source = Asset(id = "a_3", name = "Bank", balance = 1_000_000.0, type = AssetType.BANK)
        val target = Asset(id = "a_4", name = "Wallet", balance = 500_000.0, type = AssetType.E_WALLET)
        repository.insertAsset(source)
        repository.insertAsset(target)

        val transfer = Transaction(
            id = "tx_3",
            title = "Transfer",
            amount = 300_000.0,
            type = TransactionType.TRANSFER,
            categoryId = "cat_1",
            categoryName = "Transfer",
            categoryIcon = "swap_horiz",
            assetId = "a_3",
            assetName = "Bank",
            targetAssetId = "a_4",
            targetAssetName = "Wallet",
            dateMillis = System.currentTimeMillis()
        )
        repository.insertTransaction(transfer)

        // After transfer: source=700K, target=800K
        assertEquals(700_000.0, repository.getAssetById("a_3")?.balance ?: 0.0, 0.001)
        assertEquals(800_000.0, repository.getAssetById("a_4")?.balance ?: 0.0, 0.001)

        // Delete transaction
        repository.deleteTransaction("tx_3")

        // After delete: back to original
        assertEquals(1_000_000.0, repository.getAssetById("a_3")?.balance ?: 0.0, 0.001)
        assertEquals(500_000.0, repository.getAssetById("a_4")?.balance ?: 0.0, 0.001)
    }
}
