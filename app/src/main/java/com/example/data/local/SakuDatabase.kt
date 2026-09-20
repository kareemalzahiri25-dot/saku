package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.PocketDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PocketEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AssetEntity::class,
        PocketEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SakuDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun pocketDao(): PocketDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: SakuDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create assets table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `assets` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `typeString` TEXT NOT NULL,
                        `balance` REAL NOT NULL,
                        `currency` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 2. Migrate existing pockets into assets (preserving all user funds without loss)
                db.execSQL("""
                    INSERT OR REPLACE INTO `assets` (`id`, `name`, `typeString`, `balance`, `currency`, `isActive`)
                    SELECT `id`, `name`, CASE WHEN `isMain` = 1 THEN 'BANK' ELSE 'OTHER' END, `balance`, 'IDR', 1
                    FROM `pockets`
                """.trimIndent())

                // 3. Recreate pockets table for the new allocated model
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pockets_new` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `assetId` TEXT NOT NULL,
                        `allocatedAmount` REAL NOT NULL,
                        `targetAmount` REAL NOT NULL,
                        `color` TEXT NOT NULL,
                        `icon` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT OR REPLACE INTO `pockets_new` (`id`, `name`, `assetId`, `allocatedAmount`, `targetAmount`, `color`, `icon`, `isActive`)
                    SELECT `id`, `name`, `id`, 0.0, `targetAmount`, `colorHex`, `iconName`, 1
                    FROM `pockets`
                """.trimIndent())

                db.execSQL("DROP TABLE `pockets`")
                db.execSQL("ALTER TABLE `pockets_new` RENAME TO `pockets`")

                // 4. Update transactions table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `typeString` TEXT NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `categoryIcon` TEXT NOT NULL,
                        `assetId` TEXT NOT NULL,
                        `assetName` TEXT NOT NULL,
                        `pocketId` TEXT,
                        `pocketName` TEXT,
                        `targetAssetId` TEXT,
                        `targetAssetName` TEXT,
                        `dateMillis` INTEGER NOT NULL,
                        `note` TEXT NOT NULL,
                        `receiptImageUrl` TEXT,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `transactions_new` (
                        `id`, `title`, `amount`, `typeString`, `categoryId`, `categoryName`, `categoryIcon`,
                        `assetId`, `assetName`, `pocketId`, `pocketName`, `targetAssetId`, `targetAssetName`,
                        `dateMillis`, `note`, `receiptImageUrl`
                    )
                    SELECT 
                        `id`, `title`, `amount`, `typeString`, `categoryId`, `categoryName`, `categoryIcon`,
                        `pocketId`, `pocketName`, NULL, NULL, `targetPocketId`, `targetPocketName`,
                        `dateMillis`, `note`, `receiptImageUrl`
                    FROM `transactions`
                """.trimIndent())

                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): SakuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SakuDatabase::class.java,
                    "saku_database.db"
                )
                .addMigrations(MIGRATION_1_2)
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

            // Default Assets (Actual money owned by user)
            val defaultAssets = listOf(
                AssetEntity(
                    id = "asset_bca",
                    name = "BCA",
                    typeString = "BANK",
                    balance = 15000000.0,
                    currency = "IDR",
                    isActive = true
                ),
                AssetEntity(
                    id = "asset_bri",
                    name = "BRI",
                    typeString = "BANK",
                    balance = 10000000.0,
                    currency = "IDR",
                    isActive = true
                ),
                AssetEntity(
                    id = "asset_cash",
                    name = "Dompet Tunai",
                    typeString = "CASH",
                    balance = 4150000.0,
                    currency = "IDR",
                    isActive = true
                )
            )
            database.assetDao().insertAssets(defaultAssets)

            // Default Pockets (Allocated planned budget from specific assets)
            val defaultPockets = listOf(
                PocketEntity(
                    id = "pocket_needs",
                    name = "Kebutuhan Pokok",
                    assetId = "asset_bca",
                    allocatedAmount = 3450000.0,
                    targetAmount = 5000000.0,
                    icon = "shopping_cart",
                    color = "#1F594D",
                    isActive = true
                ),
                PocketEntity(
                    id = "pocket_savings",
                    name = "Tabungan Masa Depan",
                    assetId = "asset_bri",
                    allocatedAmount = 8200000.0,
                    targetAmount = 15000000.0,
                    icon = "savings",
                    color = "#C89535",
                    isActive = true
                ),
                PocketEntity(
                    id = "pocket_emergency",
                    name = "Dana Darurat",
                    assetId = "asset_bca",
                    allocatedAmount = 5000000.0,
                    targetAmount = 10000000.0,
                    icon = "health_and_safety",
                    color = "#286F60",
                    isActive = true
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
                    assetId = "asset_bca",
                    assetName = "BCA",
                    pocketId = null,
                    pocketName = null,
                    targetAssetId = null,
                    targetAssetName = null,
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
                    assetId = "asset_bca",
                    assetName = "BCA",
                    pocketId = "pocket_needs",
                    pocketName = "Kebutuhan Pokok",
                    targetAssetId = null,
                    targetAssetName = null,
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
                    assetId = "asset_cash",
                    assetName = "Dompet Tunai",
                    pocketId = "pocket_needs",
                    pocketName = "Kebutuhan Pokok",
                    targetAssetId = null,
                    targetAssetName = null,
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
                    assetId = "asset_bca",
                    assetName = "BCA",
                    pocketId = null,
                    pocketName = null,
                    targetAssetId = null,
                    targetAssetName = null,
                    dateMillis = now - (3 * day),
                    note = "Isi full tank",
                    receiptImageUrl = null
                ),
                TransactionEntity(
                    id = "tx_5",
                    title = "Pindah Dana ke BRI",
                    amount = 1500000.0,
                    typeString = "TRANSFER",
                    categoryId = "cat_other_exp",
                    categoryName = "Transfer Dana",
                    categoryIcon = "swap_horiz",
                    assetId = "asset_bca",
                    assetName = "BCA",
                    pocketId = null,
                    pocketName = null,
                    targetAssetId = "asset_bri",
                    targetAssetName = "BRI",
                    dateMillis = now - (2 * day),
                    note = "Menyisihkan dana ke rekening BRI",
                    receiptImageUrl = null
                )
            )
            database.transactionDao().insertTransactions(defaultTransactions)
        }
    }
}
