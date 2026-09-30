package com.example.ui.screens.dashboard

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Asset
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.PocketStats
import com.example.domain.model.Transaction
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import com.example.ui.screens.profile.AvatarBitmapUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: SakuRepository
) : ViewModel() {

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val user: StateFlow<User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _avatarBitmap = MutableStateFlow<Bitmap?>(null)
    val avatarBitmap: StateFlow<Bitmap?> = _avatarBitmap.asStateFlow()

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

    val pocketStats: StateFlow<List<PocketStats>> = repository.getPocketStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<Transaction>> = repository.getRecentTransactions(8)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transactionId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal menghapus transaksi"
            }
        }
    }

    // Load avatar bitmap from internal storage
    fun loadAvatarBitmap(context: Context) {
        viewModelScope.launch {
            val bitmap = AvatarBitmapUtil.loadAvatarBitmap(context)
            _avatarBitmap.value = bitmap
        }
    }
}
