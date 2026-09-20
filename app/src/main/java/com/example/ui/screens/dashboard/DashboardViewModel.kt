package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Asset
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val user: User? = null,
    val summary: FinancialSummary = FinancialSummary(),
    val assets: List<Asset> = emptyList(),
    val pockets: List<Pocket> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isBalanceVisible: Boolean = true,
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: SakuRepository
) : ViewModel() {

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    val user: StateFlow<User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val summary: StateFlow<FinancialSummary> = repository.getFinancialSummary()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            FinancialSummary()
        )

    val assets: StateFlow<List<Asset>> = repository.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pockets: StateFlow<List<Pocket>> = repository.getAllPockets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<Transaction>> = repository.getRecentTransactions(8)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }
}
