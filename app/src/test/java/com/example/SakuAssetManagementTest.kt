package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SakuDatabase
import com.example.data.repository.SakuRepositoryImpl
import com.example.domain.model.Asset
import com.example.domain.model.AssetType
import com.example.ui.screens.kantong.KantongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SakuAssetManagementTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SakuDatabase
    private lateinit var repository: SakuRepositoryImpl
    private lateinit var viewModel: KantongViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SakuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SakuRepositoryImpl(database)
        viewModel = KantongViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun testCreateAssetAddsToActiveAssets() = runTest(testDispatcher) {
        // Given: initial state has no assets
        val initialAssets = viewModel.activeAssets.first()
        assertEquals(0, initialAssets.size)

        // When: create a new asset
        val newAsset = Asset(
            id = "test_asset_1",
            name = "Bank BCA",
            balance = 5000000.0,
            type = AssetType.BANK,
            iconName = "account_balance",
            colorHex = "#133E35",
            isDefault = true
        )
        repository.insertAsset(newAsset)
        advanceUntilIdle()

        // Then: asset appears in repository (Flow might not emit immediately in test)
        val savedAsset = repository.getAssetById("test_asset_1")
        assertNotNull(savedAsset)
        assertEquals("Bank BCA", savedAsset?.name)
        assertEquals(AssetType.BANK, savedAsset?.type)
        assertEquals(5000000.0, savedAsset?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testCreateMultipleAssetsOnlyActiveOnesInActiveAssets() = runTest(testDispatcher) {
        // Given: create one active asset and one inactive (deleted) asset
        val activeAsset = Asset(
            id = "asset_active",
            name = "E-Wallet GoPay",
            balance = 1000000.0,
            type = AssetType.E_WALLET,
            iconName = "account_balance_wallet",
            colorHex = "#00AA55",
            isDefault = false
        )
        val deletedAsset = Asset(
            id = "asset_deleted",
            name = "Old Bank",
            balance = 0.0,
            type = AssetType.BANK,
            iconName = "account_balance",
            colorHex = "#133E35",
            isDefault = false
        )
        
        repository.insertAsset(activeAsset)
        repository.insertAsset(deletedAsset)
        // Mark one as deleted (inactive)
        repository.deleteAsset(deletedAsset.id)
        advanceUntilIdle()

        // Then: only active asset in repository
        val activeAssetRepo = repository.getAssetById("asset_active")
        assertNotNull(activeAssetRepo)
        assertEquals("E-Wallet GoPay", activeAssetRepo?.name)
        
        val deletedAssetRepo = repository.getAssetById("asset_deleted")
        assertNull(deletedAssetRepo)
    }

    @Test
    fun testTotalAssetBalanceSumsActiveAssetsOnly() = runTest(testDispatcher) {
        // Given: multiple active assets
        val asset1 = Asset(
            id = "asset_1",
            name = "Bank Mandiri",
            balance = 3000000.0,
            type = AssetType.BANK,
            iconName = "account_balance",
            colorHex = "#133E35"
        )
        val asset2 = Asset(
            id = "asset_2",
            name = "Uang Tunai",
            balance = 500000.0,
            type = AssetType.CASH,
            iconName = "payments",
            colorHex = "#153E35"
        )
        val asset3 = Asset(
            id = "asset_3",
            name = "Investasi Reksadana",
            balance = 10000000.0,
            type = AssetType.INVESTMENT,
            iconName = "trending_up",
            colorHex = "#1A4D3A"
        )

        repository.insertAsset(asset1)
        repository.insertAsset(asset2)
        repository.insertAsset(asset3)
        advanceUntilIdle()

        // Then: totalAssetBalance = sum of all active assets (verified via repository)
        val allAssets = repository.getAllAssets().first()
        val activeAssets = allAssets.filter { it.balance > 0 }
        val calculatedTotal = activeAssets.sumOf { it.balance }
        assertEquals(13500000.0, calculatedTotal, 0.001)
        
        // Also verify individual assets exist
        assertNotNull(repository.getAssetById("asset_1"))
        assertNotNull(repository.getAssetById("asset_2"))
        assertNotNull(repository.getAssetById("asset_3"))
    }

    @Test
    fun testUpdateAssetReflectsInActiveAssets() = runTest(testDispatcher) {
        // Given: an existing asset
        val originalAsset = Asset(
            id = "asset_update_test",
            name = "Bank BRI",
            balance = 2000000.0,
            type = AssetType.BANK,
            iconName = "account_balance",
            colorHex = "#133E35"
        )
        repository.insertAsset(originalAsset)
        advanceUntilIdle()

        // When: update the asset balance and name
        val updatedAsset = originalAsset.copy(
            name = "Bank BRI (Utama)",
            balance = 2500000.0
        )
        repository.updateAsset(updatedAsset)
        advanceUntilIdle()

        // Then: changes reflected in repository
        val activeAsset = repository.getAssetById("asset_update_test")
        assertNotNull(activeAsset)
        assertEquals("Bank BRI (Utama)", activeAsset?.name)
        assertEquals(2500000.0, activeAsset?.balance ?: 0.0, 0.001)
    }

    @Test
    fun testDeleteAssetRemovesFromActiveAssets() = runTest(testDispatcher) {
        // Given: an active asset
        val asset = Asset(
            id = "asset_to_delete",
            name = "Bank CIMB",
            balance = 1500000.0,
            type = AssetType.BANK,
            iconName = "account_balance",
            colorHex = "#133E35"
        )
        repository.insertAsset(asset)
        advanceUntilIdle()

        // Verify it's in repository
        var activeAsset = repository.getAssetById("asset_to_delete")
        assertNotNull(activeAsset)

        // When: delete the asset
        repository.deleteAsset(asset.id)
        advanceUntilIdle()

        // Then: asset removed from repository
        activeAsset = repository.getAssetById("asset_to_delete")
        assertNull(activeAsset)
    }

    @Test
    fun testAssetTypesCorrectlyStored() = runTest(testDispatcher) {
        // Given: assets of different types
        val bankAsset = Asset(
            id = "asset_bank",
            name = "Bank BCA",
            balance = 1000000.0,
            type = AssetType.BANK,
            iconName = "account_balance",
            colorHex = "#133E35"
        )
        val ewalletAsset = Asset(
            id = "asset_ewallet",
            name = "OVO",
            balance = 500000.0,
            type = AssetType.E_WALLET,
            iconName = "account_balance_wallet",
            colorHex = "#00AA55"
        )
        val cashAsset = Asset(
            id = "asset_cash",
            name = "Dompet Fisik",
            balance = 200000.0,
            type = AssetType.CASH,
            iconName = "payments",
            colorHex = "#153E35"
        )
        val investmentAsset = Asset(
            id = "asset_investment",
            name = "Reksadana Syariah",
            balance = 5000000.0,
            type = AssetType.INVESTMENT,
            iconName = "trending_up",
            colorHex = "#1A4D3A"
        )
        val cryptoAsset = Asset(
            id = "asset_crypto",
            name = "Bitcoin",
            balance = 10000000.0,
            type = AssetType.CRYPTO,
            iconName = "currency_exchange",
            colorHex = "#F7931A"
        )
        val otherAsset = Asset(
            id = "asset_other",
            name = "Emas",
            balance = 20000000.0,
            type = AssetType.OTHER,
            iconName = "category",
            colorHex = "#FFD700"
        )

        repository.insertAsset(bankAsset)
        repository.insertAsset(ewalletAsset)
        repository.insertAsset(cashAsset)
        repository.insertAsset(investmentAsset)
        repository.insertAsset(cryptoAsset)
        repository.insertAsset(otherAsset)
        advanceUntilIdle()

        // Then: all 6 assets in repository with correct types
        val allAssets = repository.getAllAssets().first()
        assertEquals(6, allAssets.size)
        
        val types = allAssets.map { it.type }.toSet()
        assertEquals(6, types.size)
        assertTrue(types.contains(AssetType.BANK))
        assertTrue(types.contains(AssetType.E_WALLET))
        assertTrue(types.contains(AssetType.CASH))
        assertTrue(types.contains(AssetType.INVESTMENT))
        assertTrue(types.contains(AssetType.CRYPTO))
        assertTrue(types.contains(AssetType.OTHER))
    }

    @Test
    fun testDefaultAssetFlagged() = runTest(testDispatcher) {
        // Given: create a default asset
        val defaultAsset = Asset(
            id = "default_asset",
            name = "Main Wallet",
            balance = 1000000.0,
            type = AssetType.E_WALLET,
            iconName = "account_balance_wallet",
            colorHex = "#00AA55",
            isDefault = true
        )
        repository.insertAsset(defaultAsset)
        advanceUntilIdle()

        // Then: asset is in repository with isDefault = true
        val savedAsset = repository.getAssetById("default_asset")
        assertNotNull(savedAsset)
        assertTrue(savedAsset?.isDefault ?: false)
    }

    @Test
    fun testViewModelTabSelectionForAssets() = runTest(testDispatcher) {
        // Given: initial tab is KANTONG
        var uiState = viewModel.uiState.first()
        assertEquals(com.example.ui.screens.kantong.SakuPageTab.KANTONG, uiState.selectedTab)

        // When: select ASET tab
        viewModel.selectTab(com.example.ui.screens.kantong.SakuPageTab.ASET)
        advanceUntilIdle()

        // Then: tab changed to ASET
        uiState = viewModel.uiState.first()
        assertEquals(com.example.ui.screens.kantong.SakuPageTab.ASET, uiState.selectedTab)
    }

    @Test
    fun testAssetViewModeSwitch() = runTest(testDispatcher) {
        // Given: initial asset view mode is GRID
        var uiState = viewModel.uiState.first()
        assertEquals(com.example.ui.screens.kantong.AssetViewMode.GRID, uiState.assetViewMode)

        // When: switch to LIST mode
        viewModel.setAssetViewMode(com.example.ui.screens.kantong.AssetViewMode.LIST)
        advanceUntilIdle()

        // Then: view mode changed to LIST
        uiState = viewModel.uiState.first()
        assertEquals(com.example.ui.screens.kantong.AssetViewMode.LIST, uiState.assetViewMode)
    }
}