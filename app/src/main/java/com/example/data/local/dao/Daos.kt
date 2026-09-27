package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseAllocationEntity
import com.example.data.local.entity.PocketAllocationEntity
import com.example.data.local.entity.PocketEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY name ASC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getAssetById(id: String): AssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteAssetById(id: String)

    @Query("DELETE FROM assets")
    suspend fun clearAll()
}

@Dao
interface PocketDao {
    @Query("SELECT * FROM pockets ORDER BY name ASC")
    fun getAllPockets(): Flow<List<PocketEntity>>

    @Query("SELECT * FROM pockets WHERE isActive = 1 ORDER BY name ASC")
    fun getActivePockets(): Flow<List<PocketEntity>>

    @Query("SELECT * FROM pockets WHERE id = :id LIMIT 1")
    suspend fun getPocketById(id: String): PocketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPocket(pocket: PocketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPockets(pockets: List<PocketEntity>)

    @Update
    suspend fun updatePocket(pocket: PocketEntity)

    @Query("DELETE FROM pockets WHERE id = :id")
    suspend fun deletePocketById(id: String)

    @Query("DELETE FROM pockets")
    suspend fun clearAll()
}

@Dao
interface PocketAllocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: PocketAllocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<PocketAllocationEntity>)

    @Update
    suspend fun updateAllocation(allocation: PocketAllocationEntity)

    @Query("SELECT * FROM pocket_allocations WHERE id = :id LIMIT 1")
    suspend fun getAllocationById(id: String): PocketAllocationEntity?

    @Query("SELECT * FROM pocket_allocations WHERE assetId = :assetId")
    fun getAllocationsByAsset(assetId: String): Flow<List<PocketAllocationEntity>>

    @Query("SELECT * FROM pocket_allocations WHERE pocketId = :pocketId")
    fun getAllocationsByPocket(pocketId: String): Flow<List<PocketAllocationEntity>>

    @Query("SELECT * FROM pocket_allocations WHERE assetId = :assetId AND pocketId = :pocketId LIMIT 1")
    suspend fun getAllocation(assetId: String, pocketId: String): PocketAllocationEntity?

    @Query("SELECT COALESCE(SUM(allocatedAmount), 0) FROM pocket_allocations WHERE pocketId = :pocketId")
    suspend fun getTotalPlannedAllocation(pocketId: String): Double

    @Query("SELECT COALESCE(SUM(allocatedAmount), 0) FROM pocket_allocations WHERE assetId = :assetId")
    suspend fun getTotalPlannedForAsset(assetId: String): Double

    @Query("DELETE FROM pocket_allocations WHERE id = :id")
    suspend fun deleteAllocationById(id: String)

    @Query("DELETE FROM pocket_allocations WHERE pocketId = :pocketId")
    suspend fun deleteAllocationsByPocket(pocketId: String)

    @Query("DELETE FROM pocket_allocations WHERE assetId = :assetId")
    suspend fun deleteAllocationsByAsset(assetId: String)

    @Query("DELETE FROM pocket_allocations")
    suspend fun clearAll()
    
    @Query("SELECT * FROM pocket_allocations WHERE assetId = :assetId")
    suspend fun getAllocationsByAssetSync(assetId: String): List<PocketAllocationEntity>
    
    @Query("SELECT * FROM pocket_allocations WHERE pocketId = :pocketId")
    suspend fun getAllocationsByPocketSync(pocketId: String): List<PocketAllocationEntity>
    
    @Query("SELECT * FROM pocket_allocations")
    suspend fun getAllAllocationsSync(): List<PocketAllocationEntity>
    
    @Query("SELECT * FROM pocket_allocations")
    fun getAllAllocations(): Flow<List<PocketAllocationEntity>>
}

@Dao
interface ExpenseAllocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: ExpenseAllocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<ExpenseAllocationEntity>)

    @Update
    suspend fun updateAllocation(allocation: ExpenseAllocationEntity)

    @Query("SELECT * FROM expense_allocations WHERE id = :id LIMIT 1")
    suspend fun getAllocationById(id: String): ExpenseAllocationEntity?

    @Query("SELECT * FROM expense_allocations WHERE transactionId = :transactionId")
    fun getAllocationsByTransaction(transactionId: String): Flow<List<ExpenseAllocationEntity>>

    @Query("SELECT * FROM expense_allocations WHERE transactionId = :transactionId")
    suspend fun getAllocationsByTransactionSync(transactionId: String): List<ExpenseAllocationEntity>

    @Query("SELECT * FROM expense_allocations WHERE pocketId = :pocketId")
    fun getAllocationsByPocket(pocketId: String): Flow<List<ExpenseAllocationEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense_allocations WHERE pocketId = :pocketId")
    suspend fun getTotalRealization(pocketId: String): Double

    @Query("DELETE FROM expense_allocations WHERE id = :id")
    suspend fun deleteAllocationById(id: String)

    @Query("DELETE FROM expense_allocations WHERE transactionId = :transactionId")
    suspend fun deleteAllocationsByTransaction(transactionId: String)

    @Query("DELETE FROM expense_allocations WHERE pocketId = :pocketId")
    suspend fun deleteAllocationsByPocket(pocketId: String)

    @Query("DELETE FROM expense_allocations")
    suspend fun clearAll()
    
    @Query("SELECT * FROM expense_allocations WHERE pocketId = :pocketId")
    suspend fun getAllocationsByPocketSync(pocketId: String): List<ExpenseAllocationEntity>

    @Query("SELECT * FROM expense_allocations")
    suspend fun getAllAllocationsSync(): List<ExpenseAllocationEntity>

    @Query("SELECT * FROM expense_allocations")
    fun getAllAllocations(): Flow<List<ExpenseAllocationEntity>>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE typeString = :typeString ORDER BY name ASC")
    fun getCategoriesByType(typeString: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clearAll()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE assetId = :assetId OR targetAssetId = :assetId ORDER BY dateMillis DESC")
    fun getTransactionsByAsset(assetId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}
