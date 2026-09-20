package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.service.ApiKeyConfigService
import com.example.data.service.CurrencyConversionService
import com.example.domain.model.FinancialSummary
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val geminiApiKeyInput: String = "",
    val isApiKeySaved: Boolean = false,
    val selectedCurrency: String = "IDR",
    val convertedBalancePreview: String = "",
    val isResetDialogOpen: Boolean = false,
    val isApiKeyDialogOpen: Boolean = false,
    val isSuccessMessage: String? = null
)

class ProfileViewModel(
    private val repository: SakuRepository,
    private val apiKeyConfigService: ApiKeyConfigService,
    private val currencyConversionService: CurrencyConversionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val user: StateFlow<User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val summary: StateFlow<FinancialSummary> = repository.getFinancialSummary()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            FinancialSummary(0.0, 0.0, 0.0, 0.0, 0)
        )

    init {
        val existingKey = apiKeyConfigService.getGeminiApiKey() ?: ""
        _uiState.value = _uiState.value.copy(
            geminiApiKeyInput = existingKey,
            isApiKeySaved = apiKeyConfigService.isGeminiConfigured()
        )
        viewModelScope.launch {
            summary.collect {
                updateConversionPreview(_uiState.value.selectedCurrency)
            }
        }
    }

    fun openApiKeyDialog() {
        _uiState.value = _uiState.value.copy(
            isApiKeyDialogOpen = true,
            geminiApiKeyInput = apiKeyConfigService.getGeminiApiKey() ?: ""
        )
    }

    fun closeApiKeyDialog() {
        _uiState.value = _uiState.value.copy(isApiKeyDialogOpen = false)
    }

    fun onApiKeyChange(key: String) {
        _uiState.value = _uiState.value.copy(geminiApiKeyInput = key)
    }

    fun saveApiKey() {
        apiKeyConfigService.setGeminiApiKey(_uiState.value.geminiApiKeyInput)
        _uiState.value = _uiState.value.copy(
            isApiKeyDialogOpen = false,
            isApiKeySaved = apiKeyConfigService.isGeminiConfigured(),
            isSuccessMessage = "Kunci API berhasil disimpan"
        )
    }

    fun selectCurrency(targetCurrency: String) {
        _uiState.value = _uiState.value.copy(selectedCurrency = targetCurrency)
        updateConversionPreview(targetCurrency)
    }

    private fun updateConversionPreview(targetCurrency: String) {
        val totalIdr = summary.value.totalBalance
        val converted = currencyConversionService.convertFromIdr(totalIdr, targetCurrency)
        val formatted = when (targetCurrency) {
            "USD" -> "$%.2f".format(converted)
            "SGD" -> "S$%.2f".format(converted)
            "EUR" -> "€%.2f".format(converted)
            "JPY" -> "¥%.0f".format(converted)
            else -> "Rp %,.0f".format(totalIdr)
        }
        _uiState.value = _uiState.value.copy(convertedBalancePreview = formatted)
    }

    fun toggleBiometric() {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            repository.saveUser(currentUser.copy(isBiometricEnabled = !currentUser.isBiometricEnabled))
        }
    }

    fun openResetDialog() {
        _uiState.value = _uiState.value.copy(isResetDialogOpen = true)
    }

    fun closeResetDialog() {
        _uiState.value = _uiState.value.copy(isResetDialogOpen = false)
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetToDefaultData()
            _uiState.value = _uiState.value.copy(
                isResetDialogOpen = false,
                isSuccessMessage = "Data aplikasi telah disetel ulang"
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(isSuccessMessage = null)
    }
}
