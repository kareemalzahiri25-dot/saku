package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.PocketDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PocketEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PocketEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SakuDatabase : RoomDatabase() {
    abstract fun pocketDao(): PocketDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: SakuDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SakuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SakuDatabase::class.java,
                    "saku_database.db"
                )
                .addCallback(SakuDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SakuDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(database: SakuDatabase) {
            // Default User
            val defaultUser = UserEntity(
                id = "user_default",
                name = "Budi Santoso",
                email = "budi.santoso@email.com",
                isBiometricEnabled = false,
                currencyCode = "IDR",
                joinedDateMillis = System.currentTimeMillis() - (60L * 24 * 3600 * 1000)
            )
            database.userDao().insertUser(defaultUser)

            // Default Pockets
            val defaultPockets = listOf(
                PocketEntity(
                    id = "pocket_main",
                    name = "Kantong Utama",
                    balance = 12500000.0,
                    targetAmount = 20000000.0,
                    iconName = "account_balance_wallet",
                    colorHex = "#153E35",
                    isMain = true,
                    description = "Saldo operasional sehari-hari"
                ),
                PocketEntity(
                    id = "pocket_needs",
                    name = "Kebutuhan Pokok",
                    balance = 3450000.0,
                    targetAmount = 5000000.0,
                    iconName = "shopping_cart",
                    colorHex = "#1F594D",
                    isMain = false,
                    description = "Makan, belanja bulanan & tagihan"
                ),
                PocketEntity(
                    id = "pocket_savings",
                    name = "Tabungan Masa Depan",
                    balance = 8200000.0,
                    targetAmount = 15000000.0,
                    iconName = "savings",
                    colorHex = "#C89535",
                    isMain = false,
                    description = "Target liburan & dana jangka panjang"
                ),
                PocketEntity(
                    id = "pocket_emergency",
                    name = "Dana Darurat",
                    balance = 5000000.0,
                    targetAmount = 10000000.0,
                    iconName = "health_and_safety",
                    colorHex = "#286F60",
                    isMain = false,
                    description = "Cadangan darurat keluarga"
                )
            )
            database.pocketDao().insertPockets(defaultPockets)

            // Default Categories
            val defaultCategories = listOf(
                // Pengeluaran
                CategoryEntity("cat_food", "Makanan & Minuman", "EXPENSE", "restaurant", "#D32F2F"),
                CategoryEntity("cat_transport", "Transportasi", "EXPENSE", "directions_car", "#E65100"),
                CategoryEntity("cat_shopping", "Belanja Bulanan", "EXPENSE", "shopping_bag", "#7B1FA2"),
                CategoryEntity("cat_bills", "Tagihan & Utilitas", "EXPENSE", "receipt_long", "#C2185B"),
                CategoryEntity("cat_entertainment", "Hiburan & Hobi", "EXPENSE", "movie", "#F57C00"),
                CategoryEntity("cat_health", "Kesehatan", "EXPENSE", "medication", "#00897B"),
                CategoryEntity("cat_education", "Pendidikan", "EXPENSE", "school", "#1976D2"),
                CategoryEntity("cat_other_exp", "Lainnya", "EXPENSE", "more_horiz", "#5D4037"),
                // Pemasukan
                CategoryEntity("cat_salary", "Gaji Pokok", "INCOME", "payments", "#1B8A4D"),
                CategoryEntity("cat_freelance", "Freelance & Proyek", "INCOME", "laptop_mac", "#2E7D32"),
                CategoryEntity("cat_investment", "Investasi & Dividen", "INCOME", "trending_up", "#388E3C"),
                CategoryEntity("cat_bonus", "Bonus & THR", "INCOME", "card_giftcard", "#43A047"),
                CategoryEntity("cat_other_inc", "Pemasukan Lain", "INCOME", "savings", "#558B2F")
            )
            database.categoryDao().insertCategories(defaultCategories)

            // Default Initial Transactions
            val now = System.currentTimeMillis()
            val day = 24L * 3600 * 1000
            val defaultTransactions = listOf(
                TransactionEntity(
                    id = "tx_1",
                    title = "Gaji Bulanan PT Digital",
                    amount = 12000000.0,
                    typeString = "INCOME",
                    categoryId = "cat_salary",
                    categoryName = "Gaji Pokok",
                    categoryIcon = "payments",
                    pocketId = "pocket_main",
                    pocketName = "Kantong Utama",
                    targetPocketId = null,
                    targetPocketName = null,
                    dateMillis = now - (2 * day),
                    note = "Gaji transfer awal bulan",
                    receiptImageUrl = null
                ),
                TransactionEntity(
                    id = "tx_2",
                    title = "Belanja Supermarket Mingguan",
                    amount = 650000.0,
                    typeString = "EXPENSE",
                    categoryId = "cat_shopping",
                    categoryName = "Belanja Bulanan",
                    categoryIcon = "shopping_bag",
                    pocketId = "pocket_needs",
                    pocketName = "Kebutuhan Pokok",
                    targetPocketId = null,
                    targetPocketName = null,
                    dateMillis = now - (1 * day),
                    note = "Beras, minyak, sayuran segar",
                    receiptImageUrl = null
                ),
                TransactionEntity(
                    id = "tx_3",
                    title = "Makan Siang Resto Padang",
                    amount = 45000.0,
                    typeString = "EXPENSE",
                    categoryId = "cat_food",
                    categoryName = "Makanan & Minuman",
                    categoryIcon = "restaurant",
                    pocketId = "pocket_needs",
                    pocketName = "Kebutuhan Pokok",
                    targetPocketId = null,
                    targetPocketName = null,
                    dateMillis = now - (4 * 3600 * 1000),
                    note = "Ayam gulai + es teh",
                    receiptImageUrl = null
                ),
                TransactionEntity(
                    id = "tx_4",
                    title = "Bensin Pertamax Mobil",
                    amount = 250000.0,
                    typeString = "EXPENSE",
                    categoryId = "cat_transport",
                    categoryName = "Transportasi",
                    categoryIcon = "directions_car",
                    pocketId = "pocket_main",
                    pocketName = "Kantong Utama",
                    targetPocketId = null,
                    targetPocketName = null,
                    dateMillis = now - (3 * day),
                    note = "Isi full tank",
                    receiptImageUrl = null
                ),
                TransactionEntity(
                    id = "tx_5",
                    title = "Alokasi Tabungan Liburan",
                    amount = 1500000.0,
                    typeString = "TRANSFER",
                    categoryId = "cat_other_exp",
                    categoryName = "Transfer",
                    categoryIcon = "swap_horiz",
                    pocketId = "pocket_main",
                    pocketName = "Kantong Utama",
                    targetPocketId = "pocket_savings",
                    targetPocketName = "Tabungan Masa Depan",
                    dateMillis = now - (2 * day),
                    note = "Menyisihkan dana liburan",
                    receiptImageUrl = null
                )
            )
            database.transactionDao().insertTransactions(defaultTransactions)
        }
    }
}
