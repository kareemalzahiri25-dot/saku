package com.example.domain.repository

import com.example.domain.model.Asset
import com.example.domain.model.Category
import com.example.domain.model.ExpenseAllocation
import com.example.domain.model.FinancialReport
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.PocketAllocation
import com.example.domain.model.PocketStats
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
    suspend fun insertAsset(asset: Asset)
    suspend fun updateAsset(asset: Asset)
    suspend fun deleteAsset(assetId: String)

    // Pockets (Planning layer)
    fun getAllPockets(): Flow<List<Pocket>>
    fun getActivePockets(): Flow<List<Pocket>>
    suspend fun getPocketById(id: String): Pocket?
    suspend fun insertPocket(pocket: Pocket)
    suspend fun updatePocket(pocket: Pocket)
    suspend fun deletePocket(pocketId: String)
    
    // Pocket Allocations (Asset -> Pocket planned funding)
    fun getPocketAllocations(): Flow<List<PocketAllocation>>
    fun getPocketAllocationsByAsset(assetId: String): Flow<List<PocketAllocation>>
    fun getPocketAllocationsByPocket(pocketId: String): Flow<List<PocketAllocation>>
    suspend fun insertPocketAllocation(allocation: PocketAllocation)
    suspend fun updatePocketAllocation(allocation: PocketAllocation)
    suspend fun deletePocketAllocation(allocationId: String)
    suspend fun replacePocketAllocations(pocketId: String, allocations: List<PocketAllocation>)

    // Categories
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>
    suspend fun insertCategory(category: Category)

    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    fun getTransactionsByAsset(assetId: String): Flow<List<Transaction>>
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
    
    // Expense Allocations (Expense -> Pocket realization)
    fun getExpenseAllocations(transactionId: String): Flow<List<ExpenseAllocation>>
    suspend fun insertExpenseAllocation(allocation: ExpenseAllocation)
    suspend fun deleteExpenseAllocation(allocationId: String)

    // Pocket Statistics (Realization & Planning)
    fun getPocketStats(): Flow<List<PocketStats>>
    
    // Financial Summaries & Reports
    fun getFinancialSummary(): Flow<FinancialSummary>
    fun getFinancialReport(periodMonth: Int, periodYear: Int): Flow<FinancialReport>
    
    // Backup, Restore & Reset
    suspend fun exportDataAsCsv(): String
    suspend fun exportDataAsJson(): String
    suspend fun exportToXlsx(outputStream: java.io.OutputStream): Boolean
    suspend fun restoreDataFromJson(json: String): Boolean
    suspend fun resetToDefaultData()
}