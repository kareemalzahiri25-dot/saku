package com.example.data.repository

import com.example.core.Formatters
import com.example.data.local.SakuDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PocketEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Category
import com.example.domain.model.CategoryExpenseSummary
import com.example.domain.model.FinancialReport
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class SakuRepositoryImpl(
    private val database: SakuDatabase
) : SakuRepository {

    private val pocketDao = database.pocketDao()
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val userDao = database.userDao()

    override fun getUser(): Flow<User?> {
        return userDao.getUser().map { it?.toDomain() }
    }

    override suspend fun saveUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(UserEntity.fromDomain(user))
    }

    override fun getAllPockets(): Flow<List<Pocket>> {
        return pocketDao.getAllPockets().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPocketById(id: String): Pocket? = withContext(Dispatchers.IO) {
        pocketDao.getPocketById(id)?.toDomain()
    }

    override suspend fun insertPocket(pocket: Pocket) = withContext(Dispatchers.IO) {
        pocketDao.insertPocket(PocketEntity.fromDomain(pocket))
    }

    override suspend fun updatePocket(pocket: Pocket) = withContext(Dispatchers.IO) {
        pocketDao.updatePocket(PocketEntity.fromDomain(pocket))
    }

    override suspend fun deletePocket(pocketId: String) = withContext(Dispatchers.IO) {
        pocketDao.deletePocketById(pocketId)
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }
    }

    override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(CategoryEntity.fromDomain(category))
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun getTransactionsByPocket(pocketId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByPocket(pocketId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        // Save transaction
        transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))

        // Update pocket balances
        val sourcePocket = pocketDao.getPocketById(transaction.pocketId)
        if (sourcePocket != null) {
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    val updated = sourcePocket.copy(balance = (sourcePocket.balance - transaction.amount).coerceAtLeast(0.0))
                    pocketDao.updatePocket(updated)
                }
                TransactionType.INCOME -> {
                    val updated = sourcePocket.copy(balance = sourcePocket.balance + transaction.amount)
                    pocketDao.updatePocket(updated)
                }
                TransactionType.TRANSFER -> {
                    val updatedSource = sourcePocket.copy(balance = (sourcePocket.balance - transaction.amount).coerceAtLeast(0.0))
                    pocketDao.updatePocket(updatedSource)

                    transaction.targetPocketId?.let { targetId ->
                        val targetPocket = pocketDao.getPocketById(targetId)
                        if (targetPocket != null) {
                            val updatedTarget = targetPocket.copy(balance = targetPocket.balance + transaction.amount)
                            pocketDao.updatePocket(updatedTarget)
                        }
                    }
                }
            }
        }
    }

    override suspend fun deleteTransaction(transactionId: String) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(transactionId)
    }

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(
            pocketDao.getAllPockets(),
            transactionDao.getAllTransactions()
        ) { pockets, transactions ->
            val totalBalance = pockets.sumOf { it.balance }

            // Current month calculation
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startOfMonth = cal.timeInMillis

            var monthIncome = 0.0
            var monthExpense = 0.0

            transactions.forEach { tx ->
                if (tx.dateMillis >= startOfMonth) {
                    when (tx.typeString) {
                        TransactionType.INCOME.name -> monthIncome += tx.amount
                        TransactionType.EXPENSE.name -> monthExpense += tx.amount
                    }
                }
            }

            val netSavings = monthIncome - monthExpense
            val rate = if (monthIncome > 0) {
                ((netSavings / monthIncome) * 100).toInt().coerceIn(0, 100)
            } else 0

            FinancialSummary(
                totalBalance = totalBalance,
                totalIncomeThisMonth = monthIncome,
                totalExpenseThisMonth = monthExpense,
                netSavingsThisMonth = netSavings,
                savingsRatePercentage = rate
            )
        }.flowOn(Dispatchers.IO)
    }

    override fun getFinancialReport(periodMonth: Int, periodYear: Int): Flow<FinancialReport> {
        return combine(
            pocketDao.getAllPockets(),
            transactionDao.getAllTransactions()
        ) { pockets, transactions ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, periodYear)
            cal.set(Calendar.MONTH, periodMonth)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startMillis = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val endMillis = cal.timeInMillis

            val filtered = transactions.filter { it.dateMillis in startMillis until endMillis }

            var income = 0.0
            var expense = 0.0
            val expenseByCategory = mutableMapOf<String, Double>()
            val categoryIcons = mutableMapOf<String, String>()

            filtered.forEach { tx ->
                when (tx.typeString) {
                    TransactionType.INCOME.name -> income += tx.amount
                    TransactionType.EXPENSE.name -> {
                        expense += tx.amount
                        val cur = expenseByCategory.getOrDefault(tx.categoryName, 0.0)
                        expenseByCategory[tx.categoryName] = cur + tx.amount
                        categoryIcons[tx.categoryName] = tx.categoryIcon
                    }
                }
            }

            val breakdown = expenseByCategory.map { (catName, amount) ->
                val pct = if (expense > 0) (amount / expense).toFloat() else 0f
                CategoryExpenseSummary(
                    categoryName = catName,
                    categoryIcon = categoryIcons[catName] ?: "category",
                    totalAmount = amount,
                    percentage = pct,
                    colorHex = "#153E35"
                )
            }.sortedByDescending { it.totalAmount }

            val totalBalance = pockets.sumOf { it.balance }
            val net = income - expense
            val rate = if (income > 0) ((net / income) * 100).toInt().coerceIn(0, 100) else 0

            val monthNames = arrayOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
            val monthLabel = if (periodMonth in 0..11) monthNames[periodMonth] else "Periode"

            FinancialReport(
                periodTitle = "$monthLabel $periodYear",
                summary = FinancialSummary(
                    totalBalance = totalBalance,
                    totalIncomeThisMonth = income,
                    totalExpenseThisMonth = expense,
                    netSavingsThisMonth = net,
                    savingsRatePercentage = rate
                ),
                categoryBreakdown = breakdown,
                transactionCount = filtered.size
            )
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val transactions = database.transactionDao().getAllTransactions()
        // Simple manual collect or query
        val sb = StringBuilder()
        sb.append("ID,Judul,Nominal,Tipe,Kategori,Kantong,Tanggal,Catatan\n")
        val allTx = database.openHelper.readableDatabase.query("SELECT * FROM transactions ORDER BY dateMillis DESC")
        while (allTx.moveToNext()) {
            val id = allTx.getString(allTx.getColumnIndexOrThrow("id"))
            val title = allTx.getString(allTx.getColumnIndexOrThrow("title")).replace(",", " ")
            val amount = allTx.getDouble(allTx.getColumnIndexOrThrow("amount"))
            val type = allTx.getString(allTx.getColumnIndexOrThrow("typeString"))
            val category = allTx.getString(allTx.getColumnIndexOrThrow("categoryName")).replace(",", " ")
            val pocket = allTx.getString(allTx.getColumnIndexOrThrow("pocketName")).replace(",", " ")
            val dateMillis = allTx.getLong(allTx.getColumnIndexOrThrow("dateMillis"))
            val note = allTx.getString(allTx.getColumnIndexOrThrow("note")).replace(",", " ")
            val dateFormatted = Formatters.formatShortDateIndo(dateMillis)
            sb.append("$id,$title,$amount,$type,$category,$pocket,$dateFormatted,$note\n")
        }
        allTx.close()
        sb.toString()
    }

    override suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("appName", "Saku")
        root.put("version", "1.0")
        root.put("exportDate", System.currentTimeMillis())

        // Pockets
        val pocketsArray = JSONArray()
        val pCursor = database.openHelper.readableDatabase.query("SELECT * FROM pockets")
        while (pCursor.moveToNext()) {
            val pObj = JSONObject()
            pObj.put("id", pCursor.getString(pCursor.getColumnIndexOrThrow("id")))
            pObj.put("name", pCursor.getString(pCursor.getColumnIndexOrThrow("name")))
            pObj.put("balance", pCursor.getDouble(pCursor.getColumnIndexOrThrow("balance")))
            pObj.put("targetAmount", pCursor.getDouble(pCursor.getColumnIndexOrThrow("targetAmount")))
            pObj.put("iconName", pCursor.getString(pCursor.getColumnIndexOrThrow("iconName")))
            pObj.put("colorHex", pCursor.getString(pCursor.getColumnIndexOrThrow("colorHex")))
            pObj.put("isMain", pCursor.getInt(pCursor.getColumnIndexOrThrow("isMain")) == 1)
            pObj.put("description", pCursor.getString(pCursor.getColumnIndexOrThrow("description")))
            pocketsArray.put(pObj)
        }
        pCursor.close()
        root.put("pockets", pocketsArray)

        // Transactions
        val txArray = JSONArray()
        val tCursor = database.openHelper.readableDatabase.query("SELECT * FROM transactions")
        while (tCursor.moveToNext()) {
            val tObj = JSONObject()
            tObj.put("id", tCursor.getString(tCursor.getColumnIndexOrThrow("id")))
            tObj.put("title", tCursor.getString(tCursor.getColumnIndexOrThrow("title")))
            tObj.put("amount", tCursor.getDouble(tCursor.getColumnIndexOrThrow("amount")))
            tObj.put("typeString", tCursor.getString(tCursor.getColumnIndexOrThrow("typeString")))
            tObj.put("categoryName", tCursor.getString(tCursor.getColumnIndexOrThrow("categoryName")))
            tObj.put("pocketName", tCursor.getString(tCursor.getColumnIndexOrThrow("pocketName")))
            tObj.put("dateMillis", tCursor.getLong(tCursor.getColumnIndexOrThrow("dateMillis")))
            tObj.put("note", tCursor.getString(tCursor.getColumnIndexOrThrow("note")))
            txArray.put(tObj)
        }
        tCursor.close()
        root.put("transactions", txArray)

        root.toString(2)
    }

    override suspend fun restoreDataFromJson(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(json)
            if (root.has("pockets")) {
                val pocketsArray = root.getJSONArray("pockets")
                val restoredPockets = mutableListOf<PocketEntity>()
                for (i in 0 until pocketsArray.length()) {
                    val p = pocketsArray.getJSONObject(i)
                    restoredPockets.add(
                        PocketEntity(
                            id = p.optString("id", "p_$i"),
                            name = p.getString("name"),
                            balance = p.getDouble("balance"),
                            targetAmount = p.optDouble("targetAmount", 0.0),
                            iconName = p.optString("iconName", "wallet"),
                            colorHex = p.optString("colorHex", "#153E35"),
                            isMain = p.optBoolean("isMain", false),
                            description = p.optString("description", "")
                        )
                    )
                }
                if (restoredPockets.isNotEmpty()) {
                    pocketDao.insertPockets(restoredPockets)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun resetToDefaultData() = withContext(Dispatchers.IO) {
        pocketDao.clearAll()
        categoryDao.clearAll()
        transactionDao.clearAll()
        userDao.clearAll()

        val defaultUser = UserEntity(
            id = "user_default",
            name = "Budi Santoso",
            email = "budi.santoso@email.com",
            isBiometricEnabled = false,
            currencyCode = "IDR",
            joinedDateMillis = System.currentTimeMillis() - (60L * 24 * 3600 * 1000)
        )
        userDao.insertUser(defaultUser)

        val defaultPockets = listOf(
            PocketEntity("pocket_main", "Kantong Utama", 12500000.0, 20000000.0, "account_balance_wallet", "#153E35", true, "Saldo operasional sehari-hari"),
            PocketEntity("pocket_needs", "Kebutuhan Pokok", 3450000.0, 5000000.0, "shopping_cart", "#1F594D", false, "Makan, belanja bulanan & tagihan"),
            PocketEntity("pocket_savings", "Tabungan Masa Depan", 8200000.0, 15000000.0, "savings", "#C89535", false, "Target liburan & dana jangka panjang"),
            PocketEntity("pocket_emergency", "Dana Darurat", 5000000.0, 10000000.0, "health_and_safety", "#286F60", false, "Cadangan darurat keluarga")
        )
        pocketDao.insertPockets(defaultPockets)

        val defaultCategories = listOf(
            CategoryEntity("cat_food", "Makanan & Minuman", "EXPENSE", "restaurant", "#D32F2F"),
            CategoryEntity("cat_transport", "Transportasi", "EXPENSE", "directions_car", "#E65100"),
            CategoryEntity("cat_shopping", "Belanja Bulanan", "EXPENSE", "shopping_bag", "#7B1FA2"),
            CategoryEntity("cat_bills", "Tagihan & Utilitas", "EXPENSE", "receipt_long", "#C2185B"),
            CategoryEntity("cat_entertainment", "Hiburan & Hobi", "EXPENSE", "movie", "#F57C00"),
            CategoryEntity("cat_salary", "Gaji Pokok", "INCOME", "payments", "#1B8A4D"),
            CategoryEntity("cat_freelance", "Freelance & Proyek", "INCOME", "laptop_mac", "#2E7D32"),
            CategoryEntity("cat_investment", "Investasi & Dividen", "INCOME", "trending_up", "#388E3C")
        )
        categoryDao.insertCategories(defaultCategories)

        val now = System.currentTimeMillis()
        val day = 24L * 3600 * 1000
        val defaultTransactions = listOf(
            TransactionEntity("tx_1", "Gaji Bulanan", 12000000.0, "INCOME", "cat_salary", "Gaji Pokok", "payments", "pocket_main", "Kantong Utama", null, null, now - (2 * day), "Gaji bulanan masuk", null),
            TransactionEntity("tx_2", "Belanja Mingguan Supermarket", 650000.0, "EXPENSE", "cat_shopping", "Belanja Bulanan", "shopping_bag", "pocket_needs", "Kebutuhan Pokok", null, null, now - (1 * day), "Bahan makanan mingguan", null),
            TransactionEntity("tx_3", "Makan Siang Resto", 45000.0, "EXPENSE", "cat_food", "Makanan & Minuman", "restaurant", "pocket_needs", "Kebutuhan Pokok", null, null, now - (4 * 3600 * 1000), "Makan siang kantor", null)
        )
        transactionDao.insertTransactions(defaultTransactions)
    }
}
