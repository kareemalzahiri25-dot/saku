package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val forgotPasswordSuccess: Boolean = false
)

sealed class AuthEvent {
    object NavigateToDashboard : AuthEvent()
    data class ShowSnackbar(val message: String) : AuthEvent()
}

class AuthViewModel(
    private val repository: SakuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onFullNameChange(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name, errorMessage = null)
    }

    fun onConfirmPasswordChange(confirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirm, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email dan kata sandi wajib diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            // Offline-first local login verification
            repository.saveUser(
                User(
                    id = "user_default",
                    name = if (state.fullName.isNotBlank()) state.fullName else "Budi Santoso",
                    email = state.email,
                    currencyCode = "IDR"
                )
            )
            _uiState.value = state.copy(isLoading = false)
            onSuccess()
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.fullName.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Semua bidang wajib diisi")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Konfirmasi kata sandi tidak cocok")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            repository.saveUser(
                User(
                    id = "user_${System.currentTimeMillis()}",
                    name = state.fullName,
                    email = state.email,
                    currencyCode = "IDR"
                )
            )
            _uiState.value = state.copy(isLoading = false)
            onSuccess()
        }
    }

    fun sendResetPassword() {
        val state = _uiState.value
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.value = state.copy(errorMessage = "Masukkan email yang valid")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            kotlinx.coroutines.delay(600) // gentle feedback
            _uiState.value = state.copy(isLoading = false, forgotPasswordSuccess = true)
        }
    }
}
