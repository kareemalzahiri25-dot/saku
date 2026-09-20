package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PocketEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketDao {
    @Query("SELECT * FROM pockets ORDER BY isMain DESC, name ASC")
    fun getAllPockets(): Flow<List<PocketEntity>>

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

    @Query("SELECT * FROM transactions WHERE pocketId = :pocketId OR targetPocketId = :pocketId ORDER BY dateMillis DESC")
    fun getTransactionsByPocket(pocketId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

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
