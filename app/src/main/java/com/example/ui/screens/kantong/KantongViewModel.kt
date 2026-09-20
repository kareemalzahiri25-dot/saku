package com.example.ui.screens.kantong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.Formatters
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class KantongViewMode {
    GRID,
    LIST
}

data class KantongUiState(
    val isAddPocketDialogOpen: Boolean = false,
    val isTransferDialogOpen: Boolean = false,
    val isEditPocketDialogOpen: Boolean = false,
    val editingPocketId: String? = null,
    val editingPocketCurrentBalance: Double = 0.0,
    val editingPocketIsMain: Boolean = false,
    val editPocketName: String = "",
    val editPocketTarget: String = "",
    val editPocketDescription: String = "",
    val editPocketType: String = "Tabungan",
    val editPocketColorHex: String = "#133E35",
    val editPocketIcon: String = "savings",
    val newPocketName: String = "",
    val newPocketBalance: String = "",
    val newPocketTarget: String = "",
    val newPocketDescription: String = "",
    val newPocketType: String = "Tabungan",
    val newPocketColorHex: String = "#133E35",
    val newPocketIcon: String = "savings",
    val viewMode: KantongViewMode = KantongViewMode.GRID,
    // Transfer fields
    val transferSourcePocketId: String = "",
    val transferTargetPocketId: String = "",
    val transferAmount: String = "",
    val transferNote: String = "",
    val errorMessage: String? = null
)

class KantongViewModel(
    private val repository: SakuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KantongUiState())
    val uiState: StateFlow<KantongUiState> = _uiState.asStateFlow()

    val pockets: StateFlow<List<Pocket>> = repository.getAllPockets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setViewMode(mode: KantongViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun openAddPocketDialog(
        presetName: String = "",
        presetTarget: String = "",
        presetType: String = "Tabungan",
        presetIcon: String = "savings",
        presetColor: String = "#133E35"
    ) {
        _uiState.value = _uiState.value.copy(
            isAddPocketDialogOpen = true,
            newPocketName = presetName,
            newPocketBalance = "",
            newPocketTarget = presetTarget,
            newPocketDescription = "",
            newPocketType = presetType,
            newPocketColorHex = presetColor,
            newPocketIcon = presetIcon,
            errorMessage = null
        )
    }

    fun closeAddPocketDialog() {
        _uiState.value = _uiState.value.copy(isAddPocketDialogOpen = false)
    }

    fun onNewPocketNameChange(name: String) {
        _uiState.value = _uiState.value.copy(newPocketName = name, errorMessage = null)
    }

    fun onNewPocketBalanceChange(balance: String) {
        _uiState.value = _uiState.value.copy(newPocketBalance = balance, errorMessage = null)
    }

    fun onNewPocketTargetChange(target: String) {
        _uiState.value = _uiState.value.copy(newPocketTarget = target, errorMessage = null)
    }

    fun onNewPocketDescriptionChange(desc: String) {
        _uiState.value = _uiState.value.copy(newPocketDescription = desc)
    }

    fun onNewPocketTypeChange(type: String) {
        _uiState.value = _uiState.value.copy(newPocketType = type)
    }

    fun onNewPocketColorChange(colorHex: String) {
        _uiState.value = _uiState.value.copy(newPocketColorHex = colorHex)
    }

    fun onNewPocketIconChange(iconName: String) {
        _uiState.value = _uiState.value.copy(newPocketIcon = iconName)
    }

    fun saveNewPocket() {
        val state = _uiState.value
        if (state.newPocketName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama kantong wajib diisi")
            return
        }

        val balance = Formatters.parseAmount(state.newPocketBalance)
        val target = Formatters.parseAmount(state.newPocketTarget)

        viewModelScope.launch {
            val descriptionWithMeta = if (state.newPocketDescription.isNotBlank()) {
                "${state.newPocketType} • ${state.newPocketDescription.trim()}"
            } else {
                state.newPocketType
            }

            val newPocket = Pocket(
                id = "pocket_${System.currentTimeMillis()}",
                name = state.newPocketName.trim(),
                balance = balance,
                targetAmount = target,
                iconName = state.newPocketIcon,
                colorHex = state.newPocketColorHex,
                isMain = false,
                description = descriptionWithMeta
            )
            repository.insertPocket(newPocket)
            closeAddPocketDialog()
        }
    }

    fun openTransferDialog(sourcePocketId: String? = null) {
        val currentPockets = pockets.value
        val defaultSource = sourcePocketId ?: currentPockets.firstOrNull()?.id ?: ""
        val defaultTarget = currentPockets.firstOrNull { it.id != defaultSource }?.id
            ?: currentPockets.getOrNull(1)?.id
            ?: ""

        _uiState.value = _uiState.value.copy(
            isTransferDialogOpen = true,
            transferSourcePocketId = defaultSource,
            transferTargetPocketId = defaultTarget,
            transferAmount = "",
            transferNote = "",
            errorMessage = null
        )
    }

    fun closeTransferDialog() {
        _uiState.value = _uiState.value.copy(isTransferDialogOpen = false)
    }

    fun onTransferSourceChange(pocketId: String) {
        _uiState.value = _uiState.value.copy(transferSourcePocketId = pocketId)
    }

    fun onTransferTargetChange(pocketId: String) {
        _uiState.value = _uiState.value.copy(transferTargetPocketId = pocketId)
    }

    fun onTransferAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(transferAmount = amount, errorMessage = null)
    }

    fun onTransferNoteChange(note: String) {
        _uiState.value = _uiState.value.copy(transferNote = note)
    }

    fun executeTransfer() {
        val state = _uiState.value
        val amount = Formatters.parseAmount(state.transferAmount)
        if (amount <= 0) {
            _uiState.value = state.copy(errorMessage = "Masukkan jumlah transfer yang valid")
            return
        }
        if (state.transferSourcePocketId == state.transferTargetPocketId) {
            _uiState.value = state.copy(errorMessage = "Kantong asal dan tujuan tidak boleh sama")
            return
        }

        val sourcePocket = pockets.value.find { it.id == state.transferSourcePocketId }
        val targetPocket = pockets.value.find { it.id == state.transferTargetPocketId }

        if (sourcePocket == null || targetPocket == null) {
            _uiState.value = state.copy(errorMessage = "Kantong tidak ditemukan")
            return
        }
        if (sourcePocket.balance < amount) {
            _uiState.value = state.copy(errorMessage = "Saldo ${sourcePocket.name} tidak mencukupi")
            return
        }

        viewModelScope.launch {
            val transferTx = Transaction(
                id = "tx_${System.currentTimeMillis()}",
                title = "Pindah Dana: ${sourcePocket.name} ➔ ${targetPocket.name}",
                amount = amount,
                type = TransactionType.TRANSFER,
                categoryId = "cat_transfer",
                categoryName = "Transfer Antar Kantong",
                categoryIcon = "swap_horiz",
                pocketId = sourcePocket.id,
                pocketName = sourcePocket.name,
                targetPocketId = targetPocket.id,
                targetPocketName = targetPocket.name,
                dateMillis = System.currentTimeMillis(),
                note = if (state.transferNote.isNotBlank()) state.transferNote else "Transfer dana antar kantong"
            )
            repository.insertTransaction(transferTx)
            closeTransferDialog()
        }
    }

    fun openEditPocketDialog(pocket: Pocket) {
        val (type, note) = extractTypeAndNote(pocket.description)
        _uiState.value = _uiState.value.copy(
            isEditPocketDialogOpen = true,
            editingPocketId = pocket.id,
            editingPocketCurrentBalance = pocket.balance,
            editingPocketIsMain = pocket.isMain,
            editPocketName = pocket.name,
            editPocketTarget = if (pocket.targetAmount > 0) pocket.targetAmount.toLong().toString() else "",
            editPocketDescription = note,
            editPocketType = type,
            editPocketColorHex = pocket.colorHex,
            editPocketIcon = pocket.iconName,
            errorMessage = null
        )
    }

    fun closeEditPocketDialog() {
        _uiState.value = _uiState.value.copy(
            isEditPocketDialogOpen = false,
            editingPocketId = null,
            errorMessage = null
        )
    }

    fun onEditPocketNameChange(name: String) {
        _uiState.value = _uiState.value.copy(editPocketName = name, errorMessage = null)
    }

    fun onEditPocketTargetChange(target: String) {
        _uiState.value = _uiState.value.copy(editPocketTarget = target, errorMessage = null)
    }

    fun onEditPocketDescriptionChange(desc: String) {
        _uiState.value = _uiState.value.copy(editPocketDescription = desc)
    }

    fun onEditPocketTypeChange(type: String) {
        _uiState.value = _uiState.value.copy(editPocketType = type)
    }

    fun onEditPocketColorChange(colorHex: String) {
        _uiState.value = _uiState.value.copy(editPocketColorHex = colorHex)
    }

    fun onEditPocketIconChange(iconName: String) {
        _uiState.value = _uiState.value.copy(editPocketIcon = iconName)
    }

    fun saveEditPocket(): Job? {
        val state = _uiState.value
        val pocketId = state.editingPocketId ?: return null

        if (state.editPocketName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama kantong wajib diisi")
            return null
        }

        val trimmedTarget = state.editPocketTarget.trim()
        if (trimmedTarget.isNotBlank()) {
            val hasInvalidChars = trimmedTarget.any { !it.isDigit() && it != '.' && it != ',' && it != ' ' }
            if (hasInvalidChars) {
                _uiState.value = state.copy(errorMessage = "Target saldo tidak valid")
                return null
            }
        }
        val target = Formatters.parseAmount(trimmedTarget)
        if (target < 0) {
            _uiState.value = state.copy(errorMessage = "Target saldo tidak valid")
            return null
        }

        val descriptionWithMeta = if (state.editPocketDescription.isNotBlank()) {
            "${state.editPocketType} • ${state.editPocketDescription.trim()}"
        } else {
            state.editPocketType
        }

        val existingPocket = pockets.value.find { it.id == pocketId }
        val balance = existingPocket?.balance ?: state.editingPocketCurrentBalance
        val isMain = existingPocket?.isMain ?: state.editingPocketIsMain

        val updatedPocket = Pocket(
            id = pocketId,
            name = state.editPocketName.trim(),
            balance = balance,
            targetAmount = target.coerceAtLeast(0.0),
            iconName = state.editPocketIcon,
            colorHex = state.editPocketColorHex,
            description = descriptionWithMeta,
            isMain = isMain
        )

        return viewModelScope.launch {
            repository.updatePocket(updatedPocket)
            closeEditPocketDialog()
        }
    }

    private fun extractTypeAndNote(description: String): Pair<String, String> {
        val knownTypes = listOf("Uang Tunai", "Bank", "Tabungan", "Investasi", "Crypto", "Lainnya")
        if (description.contains(" • ")) {
            val typeCandidate = description.substringBefore(" • ").trim()
            val note = description.substringAfter(" • ").trim()
            if (knownTypes.contains(typeCandidate)) {
                return Pair(typeCandidate, note)
            }
        }
        if (knownTypes.contains(description.trim())) {
            return Pair(description.trim(), "")
        }
        return Pair("Tabungan", description.trim())
    }

    fun deletePocket(pocketId: String) {
        viewModelScope.launch {
            repository.deletePocket(pocketId)
        }
    }
}
