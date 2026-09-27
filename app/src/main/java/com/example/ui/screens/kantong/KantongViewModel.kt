package com.example.ui.screens.kantong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.Formatters
import com.example.domain.model.Pocket
import com.example.domain.model.PocketStats
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
    val isEditPocketDialogOpen: Boolean = false,
    val editingPocketId: String? = null,
    val editingPocketCurrentRealization: Double = 0.0,
    val editPocketName: String = "",
    val editPocketTarget: String = "",
    val editPocketDescription: String = "",
    val editPocketType: String = "Tabungan",
    val editPocketColorHex: String = "#133E35",
    val editPocketIcon: String = "savings",
    val newPocketName: String = "",
    val newPocketTarget: String = "",
    val newPocketDescription: String = "",
    val newPocketType: String = "Tabungan",
    val newPocketColorHex: String = "#133E35",
    val newPocketIcon: String = "savings",
    val viewMode: KantongViewMode = KantongViewMode.GRID,
    val errorMessage: String? = null
)

class KantongViewModel(
    private val repository: SakuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KantongUiState())
    val uiState: StateFlow<KantongUiState> = _uiState.asStateFlow()

    val pockets: StateFlow<List<Pocket>> = repository.getAllPockets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pocketStats: StateFlow<List<PocketStats>> = repository.getPocketStats()
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

    fun saveNewPocket(): Job? {
        val state = _uiState.value

        if (state.newPocketName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama kantong wajib diisi")
            return null
        }

        val trimmedTarget = state.newPocketTarget.trim()
        val target = if (trimmedTarget.isNotBlank()) {
            val hasInvalidChars = trimmedTarget.any { !it.isDigit() && it != '.' && it != ',' && it != ' ' }
            if (hasInvalidChars) {
                _uiState.value = state.copy(errorMessage = "Target tidak valid")
                return null
            }
            val parsed = Formatters.parseAmount(trimmedTarget)
            if (parsed < 0) {
                _uiState.value = state.copy(errorMessage = "Target tidak boleh negatif")
                return null
            }
            parsed
        } else {
            0.0
        }

        val descriptionWithMeta = if (state.newPocketDescription.isNotBlank()) {
            "${state.newPocketType} • ${state.newPocketDescription.trim()}"
        } else {
            state.newPocketType
        }

        val newPocket = Pocket(
            id = "pocket_${System.currentTimeMillis()}",
            name = state.newPocketName.trim(),
            targetAmount = target.coerceAtLeast(0.0),
            icon = state.newPocketIcon,
            color = state.newPocketColorHex,
            description = descriptionWithMeta,
            completed = false,
            archived = false
        )

        return viewModelScope.launch {
            repository.insertPocket(newPocket)
            closeAddPocketDialog()
        }
    }

    fun openEditPocketDialog(pocket: Pocket) {
        val (type, note) = extractTypeAndNote(pocket.description)
        val stats = pocketStats.value.find { it.pocketId == pocket.id }
        val realization = stats?.realization ?: 0.0
        _uiState.value = _uiState.value.copy(
            isEditPocketDialogOpen = true,
            editingPocketId = pocket.id,
            editingPocketCurrentRealization = realization,
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
        val target = if (trimmedTarget.isNotBlank()) {
            val hasInvalidChars = trimmedTarget.any { !it.isDigit() && it != '.' && it != ',' && it != ' ' }
            if (hasInvalidChars) {
                _uiState.value = state.copy(errorMessage = "Target tidak valid")
                return null
            }
            val parsed = Formatters.parseAmount(trimmedTarget)
            if (parsed < 0) {
                _uiState.value = state.copy(errorMessage = "Target tidak valid")
                return null
            }
            parsed
        } else {
            0.0
        }

        val descriptionWithMeta = if (state.editPocketDescription.isNotBlank()) {
            "${state.editPocketType} • ${state.editPocketDescription.trim()}"
        } else {
            state.editPocketType
        }

        val existingPocket = pockets.value.find { it.id == pocketId }

        val updatedPocket = Pocket(
            id = pocketId,
            name = state.editPocketName.trim(),
            targetAmount = target.coerceAtLeast(0.0),
            icon = state.editPocketIcon,
            color = state.editPocketColorHex,
            description = descriptionWithMeta,
            completed = existingPocket?.completed ?: false,
            archived = existingPocket?.archived ?: false
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

    fun markAsCompleted(pocketId: String, completed: Boolean) {
        viewModelScope.launch {
            val pocket = repository.getPocketById(pocketId)
            if (pocket != null) {
                val updated = pocket.copy(completed = completed)
                repository.updatePocket(updated)
            }
        }
    }

    fun markAsArchived(pocketId: String, archived: Boolean) {
        viewModelScope.launch {
            val pocket = repository.getPocketById(pocketId)
            if (pocket != null) {
                val updated = pocket.copy(archived = archived)
                repository.updatePocket(updated)
            }
        }
    }
}