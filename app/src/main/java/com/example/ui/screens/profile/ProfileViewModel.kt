package com.example.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.service.ApiKeyConfigService
import com.example.data.service.CurrencyConversionService
import com.example.domain.model.FinancialSummary
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import android.content.Context
import android.graphics.Bitmap
import com.example.ui.screens.profile.AvatarBitmapUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ProfileUiState(
    val geminiApiKeyInput: String = "",
    val isApiKeySaved: Boolean = false,
    val apiKeyVerificationStatus: ApiKeyVerificationStatus = ApiKeyVerificationStatus.Unknown,
    val selectedCurrency: String = "IDR",
    val convertedBalancePreview: String = "",
    val isResetDialogOpen: Boolean = false,
    val isApiKeyDialogOpen: Boolean = false,
    val isSuccessMessage: String? = null,
    val showSaveConfirmationDialog: Boolean = false,
    val showEmailInput: Boolean = false
)

enum class ApiKeyVerificationStatus {
    Unknown("Belum Diverifikasi"),
    Verifying("Memverifikasi..."),
    Verified("Terverifikasi ✓"),
    Invalid("Tidak Valid"),
    QuotaExceeded("Kuota Habis"),
    NetworkError("Gagal Terhubung");

    val label: String
    constructor(label: String) {
        this.label = label
    }
}

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
            FinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0)
        )

    val transactionCount: StateFlow<Int> = repository.getAllTransactions()
        .map { transactions -> transactions.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _avatarBitmap = MutableStateFlow<Bitmap?>(null)
    val avatarBitmap: StateFlow<Bitmap?> = _avatarBitmap.asStateFlow()

    init {
        val existingKey = apiKeyConfigService.getGeminiApiKey() ?: ""
        _uiState.value = _uiState.value.copy(
            geminiApiKeyInput = existingKey,
            isApiKeySaved = apiKeyConfigService.isGeminiConfigured(),
            apiKeyVerificationStatus = apiKeyConfigService.getVerificationStatus()
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
        val trimmedKey = _uiState.value.geminiApiKeyInput.trim()
        apiKeyConfigService.setGeminiApiKey(trimmedKey)
        apiKeyConfigService.setVerificationStatus(ApiKeyVerificationStatus.Unknown)
        _uiState.value = _uiState.value.copy(
            geminiApiKeyInput = trimmedKey, // Update state with trimmed value
            isApiKeyDialogOpen = false,
            isApiKeySaved = apiKeyConfigService.isGeminiConfigured(),
            apiKeyVerificationStatus = ApiKeyVerificationStatus.Unknown,
            isSuccessMessage = "Kunci API berhasil disimpan"
        )
    }

    fun verifyApiKey() {
        val key = _uiState.value.geminiApiKeyInput.trim()
        if (key.isBlank()) {
            _uiState.value = _uiState.value.copy(
                apiKeyVerificationStatus = ApiKeyVerificationStatus.Invalid
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            apiKeyVerificationStatus = ApiKeyVerificationStatus.Verifying
        )

        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            val jsonRequest = JSONObject().apply {
                val contentsArray = JSONObject().apply {
                    val partsArray = JSONObject().apply {
                        put("text", "test")
                    }
                    put("parts", partsArray)
                }
                put("contents", contentsArray)
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val result = try {
                val response = client.newCall(request).execute()
                val body = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    when (response.code) {
                        400, 401, 403 -> ApiKeyVerificationStatus.Invalid
                        429 -> ApiKeyVerificationStatus.QuotaExceeded
                        else -> ApiKeyVerificationStatus.NetworkError
                    }
                } else {
                    // Check if response has valid structure
                    val root = JSONObject(body)
                    if (root.has("candidates") && root.getJSONArray("candidates").length() > 0) {
                        ApiKeyVerificationStatus.Verified
                    } else {
                        ApiKeyVerificationStatus.Invalid
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                ApiKeyVerificationStatus.NetworkError
            } catch (e: java.net.UnknownHostException) {
                ApiKeyVerificationStatus.NetworkError
            } catch (e: Exception) {
                ApiKeyVerificationStatus.NetworkError
            }

            _uiState.value = _uiState.value.copy(
                apiKeyVerificationStatus = result
            )
        }
    }

    fun selectCurrency(targetCurrency: String) {
        _uiState.value = _uiState.value.copy(selectedCurrency = targetCurrency)
        updateConversionPreview(targetCurrency)
    }

    private fun updateConversionPreview(targetCurrency: String) {
        val totalIdr = summary.value.totalAssetBalance
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
            try {
                repository.saveUser(currentUser.copy(isBiometricEnabled = !currentUser.isBiometricEnabled))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSuccessMessage = "Gagal mengubah biometric: ${e.message}"
                )
            }
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
            try {
                repository.resetToDefaultData()
                _uiState.value = _uiState.value.copy(
                    isResetDialogOpen = false,
                    isSuccessMessage = "Data aplikasi telah disetel ulang"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isResetDialogOpen = false,
                    isSuccessMessage = "Gagal reset data: ${e.message}"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(isSuccessMessage = null)
    }

    fun openSaveConfirmationDialog() {
        _uiState.value = _uiState.value.copy(showSaveConfirmationDialog = true)
    }

    fun closeSaveConfirmationDialog() {
        _uiState.value = _uiState.value.copy(showSaveConfirmationDialog = false)
    }

    fun confirmSaveData(namaLengkap: String, email: String) {
        val currentUser = user.value ?: return
        
        // Validasi minimal
        if (namaLengkap.isBlank() || email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                showSaveConfirmationDialog = false,
                isSuccessMessage = "Nama dan Email tidak boleh kosong"
            )
            return
        }
        
        val updatedUser = currentUser.copy(
            name = namaLengkap,
            email = email
        )
        
        viewModelScope.launch {
            try {
                repository.saveUser(updatedUser)
                // Success: Update UI di main thread
                _uiState.value = _uiState.value.copy(
                    showSaveConfirmationDialog = false,
                    isSuccessMessage = "Data berhasil disimpan"
                )
            } catch (e: Exception) {
                // Fail: Error handling
                _uiState.value = _uiState.value.copy(
                    showSaveConfirmationDialog = false,
                    isSuccessMessage = "Gagal menyimpan: ${e.message}"
                )
            }
        }
    }

    fun toggleEmailInput() {
        _uiState.value = _uiState.value.copy(showEmailInput = !_uiState.value.showEmailInput)
    }

    // Load avatar bitmap from internal storage
    fun loadAvatarBitmap(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = AvatarBitmapUtil.loadAvatarBitmap(context)
            _avatarBitmap.value = bitmap
        }
    }
}
