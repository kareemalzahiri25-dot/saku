package com.example.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExportBackupUiState(
    val exportedCsv: String? = null,
    val exportedJson: String? = null,
    val isExporting: Boolean = false,
    val restoreInput: String = "",
    val statusMessage: String? = null,
    val isError: Boolean = false
)

class ExportBackupViewModel(
    private val repository: SakuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportBackupUiState())
    val uiState: StateFlow<ExportBackupUiState> = _uiState.asStateFlow()

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, statusMessage = null)
            val csv = repository.exportDataAsCsv()
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportedCsv = csv,
                statusMessage = "Ekspor CSV berhasil dibuat!",
                isError = false
            )
        }
    }

    fun exportToJson() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, statusMessage = null)
            val json = repository.exportDataAsJson()
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                exportedJson = json,
                statusMessage = "Cadangan JSON berhasil dibuat!",
                isError = false
            )
        }
    }

    fun onRestoreInputChange(input: String) {
        _uiState.value = _uiState.value.copy(restoreInput = input)
    }

    fun restoreFromJson() {
        val input = _uiState.value.restoreInput
        if (input.isBlank()) {
            _uiState.value = _uiState.value.copy(
                statusMessage = "Tempelkan data JSON cadangan terlebih dahulu",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val success = repository.restoreDataFromJson(input)
            _uiState.value = _uiState.value.copy(
                isExporting = false,
                statusMessage = if (success) "Data berhasil dipulihkan!" else "Format JSON tidak valid",
                isError = !success,
                restoreInput = if (success) "" else input
            )
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
