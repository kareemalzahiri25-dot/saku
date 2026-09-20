package com.example.domain.repository

import com.example.domain.model.Asset
import com.example.domain.model.Category
import com.example.domain.model.FinancialReport
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SakuRepository {
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
    
    // Assets (Actual money)
    fun getAllAssets(): Flow<List<Asset>>
    fun getActiveAssets(): Flow<List<Asset>>
    suspend fun getAssetById(id: String): Asset?
    suspend fun getAssetAvailableBalance(assetId: String): Double
    suspend fun insertAsset(asset: Asset)
    suspend fun updateAsset(asset: Asset)
    suspend fun deleteAsset(assetId: String)

    // Pockets (Allocated planned budget)
    fun getAllPockets(): Flow<List<Pocket>>
    suspend fun getPocketById(id: String): Pocket?
    suspend fun insertPocket(pocket: Pocket)
    suspend fun updatePocket(pocket: Pocket)
    suspend fun deletePocket(pocketId: String)
    suspend fun movePocketAllocation(pocketId: String, targetAssetId: String, newAllocatedAmount: Double)
    
    // Categories
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>
    suspend fun insertCategory(category: Category)
    
    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    fun getTransactionsByPocket(pocketId: String): Flow<List<Transaction>>
    fun getTransactionsByAsset(assetId: String): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
    
    // Financial Summaries & Reports
    fun getFinancialSummary(): Flow<FinancialSummary>
    fun getFinancialReport(periodMonth: Int, periodYear: Int): Flow<FinancialReport>
    
    // Backup, Restore & Reset
    suspend fun exportDataAsCsv(): String
    suspend fun exportDataAsJson(): String
    suspend fun restoreDataFromJson(json: String): Boolean
    suspend fun resetToDefaultData()
}
