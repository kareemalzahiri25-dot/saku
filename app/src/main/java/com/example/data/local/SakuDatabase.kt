package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseAllocationDao
import com.example.data.local.dao.PocketAllocationDao
import com.example.data.local.dao.PocketDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseAllocationEntity
import com.example.data.local.entity.PocketAllocationEntity
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
        UserEntity::class,
        PocketAllocationEntity::class,
        ExpenseAllocationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SakuDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun pocketDao(): PocketDao
    abstract fun pocketAllocationDao(): PocketAllocationDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun expenseAllocationDao(): ExpenseAllocationDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create pocket_allocations table (Asset -> Pocket planned funding)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pocket_allocations` (
                        `id` TEXT NOT NULL,
                        `assetId` TEXT NOT NULL,
                        `pocketId` TEXT NOT NULL,
                        `allocatedAmount` REAL NOT NULL,
                        PRIMARY KEY(`id`),
                        UNIQUE(`assetId`, `pocketId`)
                    )
                """.trimIndent())

                // 2. Migrate existing Pocket.assetId -> pocket_allocations
                db.execSQL("""
                    INSERT OR IGNORE INTO `pocket_allocations` (`id`, `assetId`, `pocketId`, `allocatedAmount`)
                    SELECT 
                        'pa_' || p.id || '_' || p.assetId,
                        p.assetId,
                        p.id,
                        p.allocatedAmount
                    FROM `pockets` p
                    WHERE p.allocatedAmount > 0 AND p.assetId != ''
                """.trimIndent())

                // 3. Create expense_allocations table (Expense -> Pocket realization)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `expense_allocations` (
                        `id` TEXT NOT NULL,
                        `transactionId` TEXT NOT NULL,
                        `pocketId` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 4. Migrate existing Transaction.pocketId -> expense_allocations (only EXPENSE type)
                db.execSQL("""
                    INSERT OR IGNORE INTO `expense_allocations` (`id`, `transactionId`, `pocketId`, `amount`)
                    SELECT 
                        'ea_' || t.id || '_' || t.pocketId,
                        t.id,
                        t.pocketId,
                        t.amount
                    FROM `transactions` t
                    WHERE t.typeString = 'EXPENSE' AND t.pocketId IS NOT NULL AND t.pocketId != ''
                """.trimIndent())

                // 5. Recreate pockets table without assetId/allocatedAmount, add completed/archived
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `pockets_new` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `targetAmount` REAL NOT NULL,
                        `color` TEXT NOT NULL,
                        `icon` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL DEFAULT 1,
                        `completed` INTEGER NOT NULL DEFAULT 0,
                        `archived` INTEGER NOT NULL DEFAULT 0,
                        `description` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `pockets_new` (`id`, `name`, `targetAmount`, `color`, `icon`, `isActive`, `completed`, `archived`, `description`)
                    SELECT `id`, `name`, `targetAmount`, `color`, `icon`, `isActive`, 0, 0, ''
                    FROM `pockets`
                """.trimIndent())

                db.execSQL("DROP TABLE `pockets`")
                db.execSQL("ALTER TABLE `pockets_new` RENAME TO `pockets`")

                // 6. Recreate transactions table (clear pocketId—now in expense_allocations)
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
                        `assetId`, `assetName`, `targetAssetId`, `targetAssetName`,
                        `dateMillis`, `note`, `receiptImageUrl`
                    )
                    SELECT 
                        `id`, `title`, `amount`, `typeString`, `categoryId`, `categoryName`, `categoryIcon`,
                        `assetId`, `assetName`, `targetAssetId`, `targetAssetName`,
                        `dateMillis`, `note`, `receiptImageUrl`
                    FROM `transactions`
                """.trimIndent())

                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

                // 7. Create indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transactions_assetId` ON `transactions`(`assetId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_transactions_dateMillis` ON `transactions`(`dateMillis`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_expense_allocations_transactionId` ON `expense_allocations`(`transactionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_expense_allocations_pocketId` ON `expense_allocations`(`pocketId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_pocket_allocations_assetId` ON `pocket_allocations`(`assetId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_pocket_allocations_pocketId` ON `pocket_allocations`(`pocketId`)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): SakuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SakuDatabase::class.java,
                    "saku_database.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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

            // Default Pockets (Planning layer, not tied to specific assets)
            val defaultPockets = listOf(
                PocketEntity(
                    id = "pocket_needs",
                    name = "Kebutuhan Pokok",
                    targetAmount = 5000000.0,
                    icon = "shopping_cart",
                    color = "#1F594D",
                    isActive = true,
                    completed = false,
                    archived = false,
                    description = ""
                ),
                PocketEntity(
                    id = "pocket_savings",
                    name = "Tabungan Masa Depan",
                    targetAmount = 15000000.0,
                    icon = "savings",
                    color = "#C89535",
                    isActive = true,
                    completed = false,
                    archived = false,
                    description = ""
                ),
                PocketEntity(
                    id = "pocket_emergency",
                    name = "Dana Darurat",
                    targetAmount = 10000000.0,
                    icon = "health_and_safety",
                    color = "#286F60",
                    isActive = true,
                    completed = false,
                    archived = false,
                    description = ""
                )
            )
            database.pocketDao().insertPockets(defaultPockets)

            // Planned Allocations: Asset -> Pocket funding intent
            val defaultPocketAllocations = listOf(
                PocketAllocationEntity(
                    id = "pa_pocket_needs_asset_bca",
                    assetId = "asset_bca",
                    pocketId = "pocket_needs",
                    allocatedAmount = 3450000.0
                ),
                PocketAllocationEntity(
                    id = "pa_pocket_savings_asset_bri",
                    assetId = "asset_bri",
                    pocketId = "pocket_savings",
                    allocatedAmount = 8200000.0
                ),
                PocketAllocationEntity(
                    id = "pa_pocket_emergency_asset_bca",
                    assetId = "asset_bca",
                    pocketId = "pocket_emergency",
                    allocatedAmount = 5000000.0
                )
            )
            database.pocketAllocationDao().insertAllocations(defaultPocketAllocations)

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
                    targetAssetId = "asset_bri",
                    targetAssetName = "BRI",
                    dateMillis = now - (2 * day),
                    note = "Menyisihkan dana ke rekening BRI",
                    receiptImageUrl = null
                )
            )
            database.transactionDao().insertTransactions(defaultTransactions)

            // Expense Allocations: Which Pockets were affected by each expense
            val defaultExpenseAllocations = listOf(
                ExpenseAllocationEntity(
                    id = "ea_tx_2_pocket_needs",
                    transactionId = "tx_2",
                    pocketId = "pocket_needs",
                    amount = 650000.0
                ),
                ExpenseAllocationEntity(
                    id = "ea_tx_3_pocket_needs",
                    transactionId = "tx_3",
                    pocketId = "pocket_needs",
                    amount = 45000.0
                )
            )
            database.expenseAllocationDao().insertAllocations(defaultExpenseAllocations)
        }
    }
}
