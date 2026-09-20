package com.example

import com.example.data.service.ReceiptItem
import com.example.data.service.ReceiptScanException
import com.example.data.service.ReceiptScannerService
import com.example.data.service.ScannedReceiptResult
import com.example.domain.model.Category
import com.example.domain.model.FinancialReport
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import com.example.ui.screens.transaction.ReceiptSource
import com.example.ui.screens.transaction.ScanEngineMode
import com.example.ui.screens.transaction.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
class SakuReceiptScanningTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSakuRepository : SakuRepository {
        val insertedTransactions = mutableListOf<Transaction>()
        private val sampleCategories = listOf(
            Category("c1", "Makanan & Minuman", TransactionType.EXPENSE, "restaurant", "#D32F2F"),
            Category("c2", "Belanja Bulanan", TransactionType.EXPENSE, "shopping_bag", "#7B1FA2"),
            Category("c3", "Gaji Pokok", TransactionType.INCOME, "payments", "#1B8A4D")
        )
        private val samplePockets = listOf(
            Pocket("p1", "Kantong Utama", balance = 500000.0, isMain = true, colorHex = "#2E7D32")
        )

        override fun getUser(): Flow<User?> = flowOf(null)
        override suspend fun saveUser(user: User) {}

        override fun getAllPockets(): Flow<List<Pocket>> = flowOf(samplePockets)
        override suspend fun getPocketById(id: String): Pocket? = samplePockets.find { it.id == id }
        override suspend fun insertPocket(pocket: Pocket) {}
        override suspend fun updatePocket(pocket: Pocket) {}
        override suspend fun deletePocket(pocketId: String) {}

        override fun getAllCategories(): Flow<List<Category>> = flowOf(sampleCategories)
        override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
            flowOf(sampleCategories.filter { it.type == type })
        override suspend fun insertCategory(category: Category) {}

        override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(insertedTransactions)
        override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> = flowOf(insertedTransactions.take(limit))
        override fun getTransactionsByPocket(pocketId: String): Flow<List<Transaction>> = flowOf(emptyList())
        override suspend fun getTransactionById(id: String): Transaction? = insertedTransactions.find { it.id == id }
        override suspend fun insertTransaction(transaction: Transaction) {
            insertedTransactions.add(transaction)
        }
        override suspend fun deleteTransaction(transactionId: String) {
            insertedTransactions.removeAll { it.id == transactionId }
        }

        override fun getFinancialSummary(): Flow<FinancialSummary> =
            flowOf(FinancialSummary(500000.0, 0.0, 0.0, 0.0, 0))
        override fun getFinancialReport(periodMonth: Int, periodYear: Int): Flow<FinancialReport> =
            flowOf(FinancialReport("Bulan ini", FinancialSummary(500000.0, 0.0, 0.0, 0.0, 0), emptyList(), 0))

        override suspend fun exportDataAsCsv(): String = ""
        override suspend fun exportDataAsJson(): String = ""
        override suspend fun restoreDataFromJson(json: String): Boolean = true
        override suspend fun resetToDefaultData() {}
    }

    private class FakeReceiptScannerService(
        var isAiConfiguredVal: Boolean = true,
        var ocrResultToReturn: ScannedReceiptResult = ScannedReceiptResult(
            merchantName = null,
            dateMillis = 1000L,
            totalAmount = null,
            suggestedCategory = null,
            suggestedType = TransactionType.EXPENSE,
            rawText = "Hasil OCR"
        ),
        var aiResultToReturn: ScannedReceiptResult = ScannedReceiptResult(
            merchantName = "Toko Berkah",
            dateMillis = 2000L,
            totalAmount = 75000.0,
            suggestedCategory = "Makanan & Minuman",
            suggestedType = TransactionType.EXPENSE,
            items = listOf(ReceiptItem("Nasi Goreng", 1, 25000.0), ReceiptItem("Ayam Bakar", 1, 50000.0)),
            rawText = "AI summary"
        ),
        var shouldThrowError: Boolean = false
    ) : ReceiptScannerService {
        override suspend fun scanReceiptOcr(imageBytes: ByteArray): ScannedReceiptResult {
            if (shouldThrowError) throw ReceiptScanException("Gagal OCR")
            return ocrResultToReturn
        }

        override suspend fun scanReceiptWithAi(imageBytes: ByteArray, prompt: String?): ScannedReceiptResult {
            if (!isAiConfiguredVal) throw IllegalStateException("Kunci API Gemini belum dikonfigurasi.")
            if (shouldThrowError) throw ReceiptScanException("Gagal AI")
            return aiResultToReturn
        }

        override fun isAiScannerConfigured(): Boolean = isAiConfiguredVal
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSelectCameraSourceOpensMethodSheet() {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService()
        val viewModel = TransactionViewModel(repo, scanner)

        viewModel.onSelectScanSource(ReceiptSource.CAMERA)

        val state = viewModel.formState.value
        assertEquals(ReceiptSource.CAMERA, state.selectedScanSource)
        assertTrue(state.isScanMethodSheetOpen)
        assertNull(state.scanErrorMessage)
    }

    @Test
    fun testSelectGallerySourceOpensMethodSheet() {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService()
        val viewModel = TransactionViewModel(repo, scanner)

        viewModel.onSelectScanSource(ReceiptSource.GALLERY)

        val state = viewModel.formState.value
        assertEquals(ReceiptSource.GALLERY, state.selectedScanSource)
        assertTrue(state.isScanMethodSheetOpen)
        assertNull(state.scanErrorMessage)
    }

    @Test
    fun testSelectOcrMethodClosesSheetWithoutKeyRequirement() {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService(isAiConfiguredVal = false)
        val viewModel = TransactionViewModel(repo, scanner)

        viewModel.onSelectScanSource(ReceiptSource.CAMERA)
        val result = viewModel.onSelectScanMethod(ScanEngineMode.OCR)

        assertTrue(result)
        val state = viewModel.formState.value
        assertEquals(ScanEngineMode.OCR, state.scanMode)
        assertFalse(state.isScanMethodSheetOpen)
        assertFalse(state.isApiKeyMissingDialogOpen)
    }

    @Test
    fun testSelectAiMethodWithoutApiKeyShowsApiKeyMissingDialog() {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService(isAiConfiguredVal = false)
        val viewModel = TransactionViewModel(repo, scanner)

        viewModel.onSelectScanSource(ReceiptSource.CAMERA)
        val result = viewModel.onSelectScanMethod(ScanEngineMode.AI)

        assertFalse(result)
        val state = viewModel.formState.value
        assertEquals(ScanEngineMode.AI, state.scanMode)
        assertFalse(state.isScanMethodSheetOpen)
        assertTrue(state.isApiKeyMissingDialogOpen)
    }

    @Test
    fun testSwitchToOcrFromMissingKeyDialog() {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService(isAiConfiguredVal = false)
        val viewModel = TransactionViewModel(repo, scanner)

        viewModel.onSelectScanSource(ReceiptSource.GALLERY)
        viewModel.onSelectScanMethod(ScanEngineMode.AI)
        assertTrue(viewModel.formState.value.isApiKeyMissingDialogOpen)

        viewModel.switchToOcrFromMissingKey()

        val state = viewModel.formState.value
        assertEquals(ScanEngineMode.OCR, state.scanMode)
        assertFalse(state.isApiKeyMissingDialogOpen)
    }

    @Test
    fun testCameraPermissionDeniedOpensDialog() {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService()
        val viewModel = TransactionViewModel(repo, scanner)

        viewModel.onCameraPermissionDenied()

        assertTrue(viewModel.formState.value.isCameraPermissionDeniedDialogOpen)
        viewModel.dismissCameraPermissionDeniedDialog()
        assertFalse(viewModel.formState.value.isCameraPermissionDeniedDialogOpen)
    }

    @Test
    fun testProcessingReceiptDoesNotAutoSaveAndOpensReviewDialog() = runTest(testDispatcher) {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService()
        val viewModel = TransactionViewModel(repo, scanner)

        advanceUntilIdle()

        viewModel.processReceipt(byteArrayOf(1, 2, 3), "content://test.jpg", ScanEngineMode.AI)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertFalse(state.isScanning)
        assertTrue(state.isReviewDialogOpen)
        assertNotNull(state.scannedResult)
        assertEquals("Toko Berkah", state.scannedResult?.merchantName)
        assertEquals(75000.0, state.scannedResult?.totalAmount)

        // CRITICAL CHECK: User confirmation required - NO auto-saving to repository!
        assertEquals(0, repo.insertedTransactions.size)
    }

    @Test
    fun testApplyScannedResultPopulatesFormForUserEditingAndSaving() = runTest(testDispatcher) {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService()
        val viewModel = TransactionViewModel(repo, scanner)

        advanceUntilIdle()

        viewModel.processReceipt(byteArrayOf(1, 2, 3), "content://test.jpg", ScanEngineMode.AI)
        advanceUntilIdle()

        val scanned = viewModel.formState.value.scannedResult!!
        viewModel.applyScannedResultToForm(scanned)
        advanceUntilIdle()

        val form = viewModel.formState.value
        assertFalse(form.isReviewDialogOpen)
        assertEquals("Toko Berkah", form.title)
        assertEquals("75000", form.amountString)
        assertEquals(TransactionType.EXPENSE, form.type)
        assertEquals("c1", form.selectedCategoryId) // Makanan & Minuman matched
        assertTrue(form.note.contains("Nasi Goreng"))

        // User can now edit the title or nominal if needed
        viewModel.onTitleChange("Toko Berkah Menteng")
        assertEquals("Toko Berkah Menteng", viewModel.formState.value.title)

        // Then user clicks Save
        var saved = false
        viewModel.saveTransaction { saved = true }
        advanceUntilIdle()

        assertTrue(saved)
        assertEquals(1, repo.insertedTransactions.size)
        assertEquals("Toko Berkah Menteng", repo.insertedTransactions.first().title)
        assertEquals(75000.0, repo.insertedTransactions.first().amount, 0.001)
    }

    @Test
    fun testScanErrorHandling() = runTest(testDispatcher) {
        val repo = FakeSakuRepository()
        val scanner = FakeReceiptScannerService(shouldThrowError = true)
        val viewModel = TransactionViewModel(repo, scanner)

        advanceUntilIdle()

        viewModel.processReceipt(byteArrayOf(1, 2, 3), "content://test.jpg", ScanEngineMode.AI)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertFalse(state.isScanning)
        assertFalse(state.isReviewDialogOpen)
        assertNotNull(state.scanErrorMessage)
        assertTrue(state.scanErrorMessage!!.contains("Gagal AI"))

        viewModel.clearScanError()
        assertNull(viewModel.formState.value.scanErrorMessage)
    }
}
