package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SakuDatabase
import com.example.data.repository.SakuRepositoryImpl
import com.example.domain.model.Asset
import com.example.domain.model.AssetType
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.report.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SakuReportSynchronizationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SakuDatabase
    private lateinit var repository: SakuRepositoryImpl
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SakuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SakuRepositoryImpl(database)
        dashboardViewModel = DashboardViewModel(repository)
        reportViewModel = ReportViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun testScenarioA_AssetBalanceConsistentBetweenDashboardAndReport() = runTest(testDispatcher) {
        // Collect uiState and dashboard flows in background to keep WhileSubscribed active
        backgroundScope.launch { reportViewModel.uiState.collect {} }
        backgroundScope.launch { dashboardViewModel.assets.collect {} }

        // Scenario A: Tambah Aset BCA dengan saldo Rp5.000.000
        val bca = Asset(
            id = "asset_bca",
            name = "Bank BCA",
            balance = 5000000.0,
            type = AssetType.BANK,
            iconName = "account_balance",
            colorHex = "#133E35",
            isDefault = true
        )
        repository.insertAsset(bca)

        // Juga buat Pocket dengan target berbeda (misal Rp10.000.000)
        // untuk memastikan Laporan TIDAK mengambil target pocket
        val pocket = Pocket(
            id = "pocket_liburan",
            name = "Liburan",
            targetAmount = 10000000.0,
            color = "#133E35",
            icon = "flight_takeoff"
        )
        repository.insertPocket(pocket)
        advanceUntilIdle()

        // Verifikasi Dashboard
        val dashboardAssets = dashboardViewModel.assets.filter { it.isNotEmpty() }.first()
        val dashboardTotal = dashboardAssets.sumOf { it.balance }
        assertEquals(5000000.0, dashboardTotal, 0.001)

        // Verifikasi Laporan
        val reportState = reportViewModel.uiState.filter { it.totalAssetBalance > 0 }.first()
        assertEquals(5000000.0, reportState.totalAssetBalance, 0.001)

        // Konsistensi: Dashboard Total Aset == Laporan Total Aset
        assertEquals(dashboardTotal, reportState.totalAssetBalance, 0.001)
    }

    @Test
    fun testScenarioB_ExpenseUpdatesAssetRealizationDashboardAndReport() = runTest(testDispatcher) {
        backgroundScope.launch { reportViewModel.uiState.collect {} }
        backgroundScope.launch { reportViewModel.report.collect {} }
        backgroundScope.launch { dashboardViewModel.assets.collect {} }

        // Setup BCA Rp5.000.000 dan Pocket
        val bca = Asset(
            id = "asset_bca",
            name = "Bank BCA",
            balance = 5000000.0,
            type = AssetType.BANK
        )
        val pocket = Pocket(
            id = "pocket_makan",
            name = "Makan",
            targetAmount = 2000000.0,
            color = "#133E35",
            icon = "restaurant"
        )
        repository.insertAsset(bca)
        repository.insertPocket(pocket)
        advanceUntilIdle()

        // Skenario B: Catat pengeluaran Rp500.000 dari BCA ke Pocket Makan
        val now = System.currentTimeMillis()
        val expenseTx = Transaction(
            id = "tx_exp_1",
            title = "Makan Siang",
            amount = 500000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_food",
            categoryName = "Makanan",
            categoryIcon = "restaurant",
            assetId = "asset_bca",
            assetName = "Bank BCA",
            dateMillis = now,
            pocketId = "pocket_makan",
            pocketName = "Makan"
        )
        repository.insertTransaction(expenseTx)
        advanceUntilIdle()

        // 1. Saldo BCA berkurang jadi Rp4.500.000
        val updatedBca = repository.getAssetById("asset_bca")
        assertEquals(4500000.0, updatedBca?.balance ?: 0.0, 0.001)

        // 2. Realisasi Kantong Makan = Rp500.000
        val pocketStats = repository.getPocketStats().first()
        val makanStats = pocketStats.find { it.pocketId == "pocket_makan" }
        assertNotNull(makanStats)
        assertEquals(500000.0, makanStats?.realization ?: 0.0, 0.001)

        // 3. Dashboard mencerminkan saldo baru
        val dashboardAssets = dashboardViewModel.assets.filter { it.isNotEmpty() }.first()
        assertEquals(4500000.0, dashboardAssets.sumOf { it.balance }, 0.001)

        // 4. Laporan mencerminkan pengeluaran dan total saldo aset yang benar
        val reportState = reportViewModel.uiState.filter { it.totalExpense > 0 }.first()
        assertEquals(4500000.0, reportState.totalAssetBalance, 0.001)
        assertEquals(500000.0, reportState.totalExpense, 0.001)

        val reportSummary = reportViewModel.report.filter { it.summary.totalExpense > 0 }.first()
        assertEquals(500000.0, reportSummary.summary.totalExpense, 0.001)
    }

    @Test
    fun testScenarioC_IncomeUpdatesAssetDashboardAndReport() = runTest(testDispatcher) {
        backgroundScope.launch { reportViewModel.uiState.collect {} }
        backgroundScope.launch { reportViewModel.report.collect {} }
        backgroundScope.launch { dashboardViewModel.assets.collect {} }

        // Setup BCA Rp5.000.000
        val bca = Asset(
            id = "asset_bca",
            name = "Bank BCA",
            balance = 5000000.0,
            type = AssetType.BANK
        )
        repository.insertAsset(bca)
        advanceUntilIdle()

        // Skenario C: Catat pemasukan Rp1.000.000 ke BCA
        val now = System.currentTimeMillis()
        val incomeTx = Transaction(
            id = "tx_inc_1",
            title = "Gaji Tambahan",
            amount = 1000000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_salary",
            categoryName = "Gaji",
            categoryIcon = "payments",
            assetId = "asset_bca",
            assetName = "Bank BCA",
            dateMillis = now
        )
        repository.insertTransaction(incomeTx)
        advanceUntilIdle()

        // 1. Saldo BCA bertambah jadi Rp6.000.000
        val updatedBca = repository.getAssetById("asset_bca")
        assertEquals(6000000.0, updatedBca?.balance ?: 0.0, 0.001)

        // 2. Dashboard konsisten Rp6.000.000
        val dashboardAssets = dashboardViewModel.assets.filter { it.isNotEmpty() }.first()
        assertEquals(6000000.0, dashboardAssets.sumOf { it.balance }, 0.001)

        // 3. Laporan mencerminkan pemasukan Rp1.000.000 dan saldo aset Rp6.000.000
        val reportState = reportViewModel.uiState.filter { it.totalIncome > 0 }.first()
        assertEquals(6000000.0, reportState.totalAssetBalance, 0.001)
        assertEquals(1000000.0, reportState.totalIncome, 0.001)

        val reportSummary = reportViewModel.report.filter { it.summary.totalIncome > 0 }.first()
        assertEquals(1000000.0, reportSummary.summary.totalIncome, 0.001)
    }

    @Test
    fun testScenarioD_TransferBetweenAssetsPreservesTotalBalance() = runTest(testDispatcher) {
        backgroundScope.launch { reportViewModel.uiState.collect {} }
        backgroundScope.launch { dashboardViewModel.assets.collect {} }

        // Setup BCA Rp5.000.000 dan GoPay Rp1.000.000
        val bca = Asset(
            id = "asset_bca",
            name = "Bank BCA",
            balance = 5000000.0,
            type = AssetType.BANK
        )
        val gopay = Asset(
            id = "asset_gopay",
            name = "GoPay",
            balance = 1000000.0,
            type = AssetType.E_WALLET
        )
        repository.insertAsset(bca)
        repository.insertAsset(gopay)
        advanceUntilIdle()

        // Total awal = Rp6.000.000
        val initialTotal = repository.getActiveAssets().first().sumOf { it.balance }
        assertEquals(6000000.0, initialTotal, 0.001)

        // Skenario D: Transfer Rp500.000 dari BCA ke GoPay
        val now = System.currentTimeMillis()
        val transferTx = Transaction(
            id = "tx_trf_1",
            title = "Topup GoPay",
            amount = 500000.0,
            type = TransactionType.TRANSFER,
            categoryId = "cat_transfer",
            categoryName = "Transfer",
            categoryIcon = "swap_horiz",
            assetId = "asset_bca",
            assetName = "Bank BCA",
            targetAssetId = "asset_gopay",
            targetAssetName = "GoPay",
            dateMillis = now
        )
        repository.insertTransaction(transferTx)
        advanceUntilIdle()

        // 1. BCA = Rp4.500.000
        val updatedBca = repository.getAssetById("asset_bca")
        assertEquals(4500000.0, updatedBca?.balance ?: 0.0, 0.001)

        // 2. GoPay = Rp1.500.000
        val updatedGopay = repository.getAssetById("asset_gopay")
        assertEquals(1500000.0, updatedGopay?.balance ?: 0.0, 0.001)

        // 3. Total Aset tetap Rp6.000.000
        val finalTotal = repository.getActiveAssets().first().sumOf { it.balance }
        assertEquals(6000000.0, finalTotal, 0.001)

        // 4. Dashboard konsisten
        val dashboardAssets = dashboardViewModel.assets.filter { it.size == 2 }.first()
        val dashboardTotal = dashboardAssets.sumOf { it.balance }
        assertEquals(6000000.0, dashboardTotal, 0.001)

        // 5. Laporan Total Aset konsisten Rp6.000.000
        val reportState = reportViewModel.uiState.filter { it.totalAssetBalance > 0 }.first()
        assertEquals(6000000.0, reportState.totalAssetBalance, 0.001)
    }

    @Test
    fun testScenarioE_DeleteTransactionRollsBackAllDerivatives() = runTest(testDispatcher) {
        backgroundScope.launch { reportViewModel.uiState.collect {} }
        backgroundScope.launch { dashboardViewModel.assets.collect {} }

        // Setup BCA Rp5.000.000 dan Pocket Makan
        val bca = Asset(
            id = "asset_bca",
            name = "Bank BCA",
            balance = 5000000.0,
            type = AssetType.BANK
        )
        val pocket = Pocket(
            id = "pocket_makan",
            name = "Makan",
            targetAmount = 2000000.0,
            color = "#133E35",
            icon = "restaurant"
        )
        repository.insertAsset(bca)
        repository.insertPocket(pocket)
        advanceUntilIdle()

        // Insert Expense Rp500.000
        val now = System.currentTimeMillis()
        val expenseTx = Transaction(
            id = "tx_exp_del",
            title = "Makan Malam",
            amount = 500000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_food",
            categoryName = "Makanan",
            categoryIcon = "restaurant",
            assetId = "asset_bca",
            assetName = "Bank BCA",
            dateMillis = now,
            pocketId = "pocket_makan",
            pocketName = "Makan"
        )
        repository.insertTransaction(expenseTx)
        advanceUntilIdle()

        // Verifikasi kondisi setelah expense
        assertEquals(4500000.0, repository.getAssetById("asset_bca")?.balance ?: 0.0, 0.001)

        // Skenario E: Hapus transaksi pengeluaran
        repository.deleteTransaction("tx_exp_del")
        advanceUntilIdle()

        // 1. Saldo BCA kembali utuh ke Rp5.000.000
        val restoredBca = repository.getAssetById("asset_bca")
        assertEquals(5000000.0, restoredBca?.balance ?: 0.0, 0.001)

        // 2. Realisasi Kantong Makan kembali 0
        val pocketStats = repository.getPocketStats().first()
        val makanStats = pocketStats.find { it.pocketId == "pocket_makan" }
        assertEquals(0.0, makanStats?.realization ?: 0.0, 0.001)

        // 3. Dashboard kembali ke saldo awal
        val dashboardAssets = dashboardViewModel.assets.filter { it.any { a -> a.balance == 5000000.0 } }.first()
        assertEquals(5000000.0, dashboardAssets.sumOf { it.balance }, 0.001)

        // 4. Laporan pengeluaran kembali 0 dan total saldo aset kembali Rp5.000.000
        val reportState = reportViewModel.uiState.filter { it.totalExpense == 0.0 && it.totalAssetBalance == 5000000.0 }.first()
        assertEquals(5000000.0, reportState.totalAssetBalance, 0.001)
        assertEquals(0.0, reportState.totalExpense, 0.001)
    }
}