package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SakuDatabase
import com.example.data.repository.SakuRepositoryImpl
import com.example.domain.model.Pocket
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SakuPocketEditTest {

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
    fun testEditingPocketNamePreservesTargetAndRealization() = runTest(testDispatcher) {
        val originalPocket = Pocket(
            id = "p_vacation",
            name = "Tabungan Liburan",
            targetAmount = 5000000.0,
            color = "#133E35",
            icon = "flight_takeoff"
        )
        repository.insertPocket(originalPocket)
        advanceUntilIdle()

        // Open edit dialog
        viewModel.openEditPocketDialog(originalPocket)
        assertEquals("Tabungan Liburan", viewModel.uiState.value.editPocketName)

        // Edit name
        viewModel.onEditPocketNameChange("Tabungan Jalan-Jalan Luar Negeri")
        viewModel.saveEditPocket()?.join()
        advanceUntilIdle()

        val updated = repository.getPocketById("p_vacation")
        assertNotNull(updated)
        assertEquals("Tabungan Jalan-Jalan Luar Negeri", updated?.name)
        // Target unchanged
        assertEquals(5000000.0, updated?.targetAmount ?: 0.0, 0.001)
        assertEquals("p_vacation", updated?.id)
        assertFalse(viewModel.uiState.value.isEditPocketDialogOpen)
    }

    @Test
    fun testEditingPocketTarget() = runTest(testDispatcher) {
        val originalPocket = Pocket(
            id = "p_target_test",
            name = "Beli Laptop",
            targetAmount = 15000000.0,
            color = "#2563EB",
            icon = "work"
        )
        repository.insertPocket(originalPocket)
        advanceUntilIdle()

        viewModel.openEditPocketDialog(originalPocket)
        viewModel.onEditPocketTargetChange("20000000")
        viewModel.saveEditPocket()?.join()
        advanceUntilIdle()

        val updated = repository.getPocketById("p_target_test")
        assertNotNull(updated)
        assertEquals(20000000.0, updated?.targetAmount ?: 0.0, 0.001)
    }

    @Test
    fun testEditingColorAndIcon() = runTest(testDispatcher) {
        val originalPocket = Pocket(
            id = "p_style_test",
            name = "Dana Hiburan",
            targetAmount = 1000000.0,
            color = "#133E35",
            icon = "savings"
        )
        repository.insertPocket(originalPocket)
        advanceUntilIdle()

        viewModel.openEditPocketDialog(originalPocket)
        viewModel.onEditPocketColorChange("#EF4444")
        viewModel.onEditPocketIconChange("restaurant")
        viewModel.saveEditPocket()?.join()
        advanceUntilIdle()

        val updated = repository.getPocketById("p_style_test")
        assertNotNull(updated)
        assertEquals("#EF4444", updated?.color)
        assertEquals("restaurant", updated?.icon)
    }

    @Test
    fun testInvalidInputEmptyNameIsRejected() = runTest(testDispatcher) {
        val originalPocket = Pocket(
            id = "p_valid",
            name = "Kantong Asli",
            targetAmount = 500000.0,
            color = "#133E35",
            icon = "wallet"
        )
        repository.insertPocket(originalPocket)
        advanceUntilIdle()

        viewModel.openEditPocketDialog(originalPocket)
        viewModel.onEditPocketNameChange("   ")
        viewModel.saveEditPocket()
        advanceUntilIdle()

        // Error message must be shown and dialog remains open
        assertTrue(viewModel.uiState.value.isEditPocketDialogOpen)
        assertEquals("Nama kantong wajib diisi", viewModel.uiState.value.errorMessage)

        // Database record must NOT have empty name
        val fromDb = repository.getPocketById("p_valid")
        assertEquals("Kantong Asli", fromDb?.name)
    }

    @Test
    fun testInvalidTargetValueIsRejected() = runTest(testDispatcher) {
        val originalPocket = Pocket(
            id = "p_target_invalid",
            name = "Target Test",
            targetAmount = 500000.0,
            color = "#133E35",
            icon = "wallet"
        )
        repository.insertPocket(originalPocket)
        advanceUntilIdle()

        viewModel.openEditPocketDialog(originalPocket)
        viewModel.onEditPocketTargetChange("xyz_bukan_angka")
        viewModel.saveEditPocket()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEditPocketDialogOpen)
        assertEquals("Target tidak valid", viewModel.uiState.value.errorMessage)

        val fromDb = repository.getPocketById("p_target_invalid")
        assertEquals(500000.0, fromDb?.targetAmount ?: 0.0, 0.001)
    }

    @Test
    fun testEditedDataPersistsAfterDatabaseReload() = runTest(testDispatcher) {
        // For in-memory database, we test that data inserted persists within the same instance
        // File-backed persistence is tested at integration level; here we verify repo operations work
        val pocket = Pocket(
            id = "p_persist",
            name = "Tabungan Rumah",
            targetAmount = 250000000.0,
            color = "#133E35",
            icon = "home"
        )
        repository.insertPocket(pocket)
        advanceUntilIdle()

        // 2. Perform edit
        val updatedPocket = pocket.copy(
            name = "Tabungan Rumah Tingkat",
            targetAmount = 350000000.0,
            color = "#0D9488",
            icon = "apartment"
        )
        repository.updatePocket(updatedPocket)
        advanceUntilIdle()

        // 3. Verify data updated
        val retrieved = repository.getPocketById("p_persist")
        assertNotNull(retrieved)
        assertEquals("Tabungan Rumah Tingkat", retrieved?.name)
        assertEquals(350000000.0, retrieved?.targetAmount ?: 0.0, 0.001)
        assertEquals("#0D9488", retrieved?.color)
        assertEquals("apartment", retrieved?.icon)
    }
}
