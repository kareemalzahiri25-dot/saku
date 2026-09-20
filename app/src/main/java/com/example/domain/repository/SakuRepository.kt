package com.example.domain.repository

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
    
    fun getAllPockets(): Flow<List<Pocket>>
    suspend fun getPocketById(id: String): Pocket?
    suspend fun insertPocket(pocket: Pocket)
    suspend fun updatePocket(pocket: Pocket)
    suspend fun deletePocket(pocketId: String)
    
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>
    suspend fun insertCategory(category: Category)
    
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    fun getTransactionsByPocket(pocketId: String): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
    
    fun getFinancialSummary(): Flow<FinancialSummary>
    fun getFinancialReport(periodMonth: Int, periodYear: Int): Flow<FinancialReport>
    
    suspend fun exportDataAsCsv(): String
    suspend fun exportDataAsJson(): String
    suspend fun restoreDataFromJson(json: String): Boolean
    suspend fun resetToDefaultData()
}
