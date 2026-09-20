package com.example.ui.screens.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.Formatters
import com.example.data.service.ReceiptScannerService
import com.example.data.service.ScannedReceiptResult
import com.example.domain.model.Category
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScanEngineMode(val label: String) {
    OCR("OCR Biasa"),
    AI("AI Scan")
}

enum class ReceiptSource(val label: String) {
    CAMERA("Kamera"),
    GALLERY("Galeri")
}

data class TransactionFormState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountString: String = "",
    val title: String = "",
    val selectedCategoryId: String = "",
    val selectedPocketId: String = "",
    val note: String = "",
    // Receipt scanning state
    val selectedScanSource: ReceiptSource? = null,
    val isScanMethodSheetOpen: Boolean = false,
    val scanMode: ScanEngineMode = ScanEngineMode.OCR,
    val isScanning: Boolean = false,
    val scanStatusMessage: String? = null,
    val isReviewDialogOpen: Boolean = false,
    val scannedResult: ScannedReceiptResult? = null,
    val scannedImageUri: String? = null,
    val isApiKeyMissingDialogOpen: Boolean = false,
    val isCameraPermissionDeniedDialogOpen: Boolean = false,
    val scanErrorMessage: String? = null,
    val scanSuccessMessage: String? = null,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    // Backward compatibility
    val isScanningSheetOpen: Boolean = false
)

class TransactionViewModel(
    private val repository: SakuRepository,
    private val receiptScannerService: ReceiptScannerService
) : ViewModel() {

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pockets: StateFlow<List<Pocket>> = repository.getAllPockets()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            categories.collect { cats ->
                if (_formState.value.selectedCategoryId.isEmpty()) {
                    val defaultCat = cats.firstOrNull { it.type == _formState.value.type }
                    if (defaultCat != null) {
                        _formState.value = _formState.value.copy(selectedCategoryId = defaultCat.id)
                    }
                }
            }
        }
        viewModelScope.launch {
            pockets.collect { pList ->
                if (_formState.value.selectedPocketId.isEmpty()) {
                    val defaultPocket = pList.firstOrNull { it.isMain } ?: pList.firstOrNull()
                    if (defaultPocket != null) {
                        _formState.value = _formState.value.copy(selectedPocketId = defaultPocket.id)
                    }
                }
            }
        }
    }

    fun setTransactionType(type: TransactionType) {
        val matchingCat = categories.value.firstOrNull { it.type == type }
        _formState.value = _formState.value.copy(
            type = type,
            selectedCategoryId = matchingCat?.id ?: "",
            errorMessage = null
        )
    }

    fun onAmountChange(amount: String) {
        _formState.value = _formState.value.copy(amountString = amount, errorMessage = null)
    }

    fun onTitleChange(title: String) {
        _formState.value = _formState.value.copy(title = title, errorMessage = null)
    }

    fun onCategorySelect(categoryId: String) {
        _formState.value = _formState.value.copy(selectedCategoryId = categoryId)
    }

    fun onPocketSelect(pocketId: String) {
        _formState.value = _formState.value.copy(selectedPocketId = pocketId)
    }

    fun onNoteChange(note: String) {
        _formState.value = _formState.value.copy(note = note)
    }

    // --- Scan Struk Flow ---

    fun onSelectScanSource(source: ReceiptSource) {
        _formState.value = _formState.value.copy(
            selectedScanSource = source,
            isScanMethodSheetOpen = true,
            scanErrorMessage = null
        )
    }

    fun onSelectScanMethod(mode: ScanEngineMode): Boolean {
        _formState.value = _formState.value.copy(scanMode = mode)
        if (mode == ScanEngineMode.AI && !isAiConfigured()) {
            _formState.value = _formState.value.copy(
                isScanMethodSheetOpen = false,
                isApiKeyMissingDialogOpen = true
            )
            return false
        }
        _formState.value = _formState.value.copy(isScanMethodSheetOpen = false)
        return true
    }

    fun isAiConfigured(): Boolean {
        return receiptScannerService.isAiScannerConfigured()
    }

    fun dismissScanMethodSheet() {
        _formState.value = _formState.value.copy(isScanMethodSheetOpen = false)
    }

    fun dismissApiKeyMissingDialog() {
        _formState.value = _formState.value.copy(isApiKeyMissingDialogOpen = false)
    }

    fun switchToOcrFromMissingKey() {
        _formState.value = _formState.value.copy(
            scanMode = ScanEngineMode.OCR,
            isApiKeyMissingDialogOpen = false
        )
    }

    fun onCameraPermissionDenied() {
        _formState.value = _formState.value.copy(isCameraPermissionDeniedDialogOpen = true)
    }

    fun dismissCameraPermissionDeniedDialog() {
        _formState.value = _formState.value.copy(isCameraPermissionDeniedDialogOpen = false)
    }

    fun onScanError(message: String) {
        _formState.value = _formState.value.copy(
            isScanning = false,
            scanStatusMessage = null,
            scanErrorMessage = message
        )
    }

    fun clearScanError() {
        _formState.value = _formState.value.copy(scanErrorMessage = null)
    }

    fun dismissReviewDialog() {
        _formState.value = _formState.value.copy(isReviewDialogOpen = false)
    }

    fun applyScannedResultToForm(result: ScannedReceiptResult) {
        val targetType = result.suggestedType
        val currentCats = categories.value
        val matchedCategory = currentCats.find { 
            it.name.equals(result.suggestedCategory, ignoreCase = true) 
        } ?: currentCats.firstOrNull { it.type == targetType }

        val formattedAmount = result.totalAmount?.let {
            if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
        } ?: _formState.value.amountString

        val scanNote = if (_formState.value.scanMode == ScanEngineMode.AI) {
            "Pindai AI Gemini: " + (result.items.takeIf { it.isNotEmpty() }?.joinToString(", ") { "${it.name} (${Formatters.formatRupiah(it.price)})" } ?: result.merchantName ?: "Struk")
        } else {
            "Pindai OCR: " + (result.items.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.name } ?: result.merchantName ?: "Struk")
        }

        _formState.value = _formState.value.copy(
            isReviewDialogOpen = false,
            title = result.merchantName ?: _formState.value.title.ifBlank { "Pembelian Struk" },
            amountString = formattedAmount,
            type = targetType,
            selectedCategoryId = matchedCategory?.id ?: _formState.value.selectedCategoryId,
            note = scanNote,
            scanSuccessMessage = "Data struk berhasil diterapkan ke formulir. Anda dapat mengeditnya jika diperlukan."
        )

        if (matchedCategory == null) {
            viewModelScope.launch {
                val cats = repository.getAllCategories().first()
                val match = cats.find { it.name.equals(result.suggestedCategory, ignoreCase = true) }
                    ?: cats.firstOrNull { it.type == targetType }
                if (match != null && (_formState.value.selectedCategoryId.isEmpty() || _formState.value.selectedCategoryId != match.id)) {
                    _formState.value = _formState.value.copy(selectedCategoryId = match.id)
                }
            }
        }
    }

    fun processReceipt(
        imageBytes: ByteArray,
        uriString: String? = null,
        mode: ScanEngineMode = _formState.value.scanMode
    ) {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(
                isScanning = true,
                scanErrorMessage = null,
                scanStatusMessage = if (mode == ScanEngineMode.AI) {
                    "Menganalisis struk dengan AI Gemini..."
                } else {
                    "Mengekstrak teks struk dengan OCR..."
                }
            )

            try {
                val result = if (mode == ScanEngineMode.AI) {
                    receiptScannerService.scanReceiptWithAi(imageBytes)
                } else {
                    receiptScannerService.scanReceiptOcr(imageBytes)
                }

                _formState.value = _formState.value.copy(
                    isScanning = false,
                    scanStatusMessage = null,
                    scannedResult = result,
                    scannedImageUri = uriString,
                    isReviewDialogOpen = true
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isScanning = false,
                    scanStatusMessage = null,
                    scanErrorMessage = e.message ?: "Terjadi kesalahan saat memproses gambar struk."
                )
            }
        }
    }

    // Backward-compatibility functions
    fun openScanningSheet() {
        _formState.value = _formState.value.copy(isScanMethodSheetOpen = true)
    }

    fun closeScanningSheet() {
        _formState.value = _formState.value.copy(isScanMethodSheetOpen = false, isScanningSheetOpen = false)
    }

    fun setScanMode(mode: ScanEngineMode) {
        _formState.value = _formState.value.copy(scanMode = mode)
    }

    fun clearScanSuccessMessage() {
        _formState.value = _formState.value.copy(scanSuccessMessage = null)
    }

    fun processReceiptImage(imageBytes: ByteArray, mode: ScanEngineMode = _formState.value.scanMode) {
        processReceipt(imageBytes, null, mode)
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val state = _formState.value
        val amount = Formatters.parseAmount(state.amountString)
        if (amount <= 0) {
            _formState.value = state.copy(errorMessage = "Nominal transaksi harus lebih besar dari 0")
            return
        }
        if (state.title.isBlank()) {
            _formState.value = state.copy(errorMessage = "Nama transaksi wajib diisi")
            return
        }

        val category = categories.value.find { it.id == state.selectedCategoryId }
            ?: categories.value.firstOrNull { it.type == state.type }
        val pocket = pockets.value.find { it.id == state.selectedPocketId }
            ?: pockets.value.firstOrNull()

        if (category == null || pocket == null) {
            _formState.value = state.copy(errorMessage = "Pilih kategori dan kantong terlebih dahulu")
            return
        }

        viewModelScope.launch {
            val tx = Transaction(
                id = "tx_${System.currentTimeMillis()}",
                title = state.title.trim(),
                amount = amount,
                type = state.type,
                categoryId = category.id,
                categoryName = category.name,
                categoryIcon = category.iconName,
                pocketId = pocket.id,
                pocketName = pocket.name,
                dateMillis = System.currentTimeMillis(),
                note = state.note.trim()
            )
            repository.insertTransaction(tx)
            _formState.value = TransactionFormState(isSavedSuccess = true)
            onSuccess()
        }
    }
}
