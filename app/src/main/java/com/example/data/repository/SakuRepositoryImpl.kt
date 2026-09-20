package com.example.data.repository

import androidx.room.withTransaction
import com.example.core.Formatters
import com.example.data.local.SakuDatabase
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PocketEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Asset
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

    private val assetDao = database.assetDao()
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

    // --- Asset Operations ---

    override fun getAllAssets(): Flow<List<Asset>> {
        return assetDao.getAllAssets().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveAssets(): Flow<List<Asset>> {
        return assetDao.getActiveAssets().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAssetById(id: String): Asset? = withContext(Dispatchers.IO) {
        assetDao.getAssetById(id)?.toDomain()
    }

    override suspend fun getAssetAvailableBalance(assetId: String): Double = withContext(Dispatchers.IO) {
        val asset = assetDao.getAssetById(assetId) ?: return@withContext 0.0
        val pockets = pocketDao.getPocketsByAssetSync(assetId).filter { it.isActive }
        val allocated = pockets.sumOf { it.allocatedAmount }
        (asset.balance - allocated).coerceAtLeast(0.0)
    }

    override suspend fun insertAsset(asset: Asset) = withContext(Dispatchers.IO) {
        require(asset.balance >= 0) { "Saldo aset tidak boleh negatif" }
        require(asset.name.isNotBlank()) { "Nama aset tidak boleh kosong" }
        assetDao.insertAsset(AssetEntity.fromDomain(asset))
    }

    override suspend fun updateAsset(asset: Asset) = withContext(Dispatchers.IO) {
        require(asset.balance >= 0) { "Saldo aset tidak boleh negatif" }
        require(asset.name.isNotBlank()) { "Nama aset tidak boleh kosong" }
        database.withTransaction {
            val pockets = pocketDao.getPocketsByAssetSync(asset.id).filter { it.isActive }
            val currentAllocated = pockets.sumOf { it.allocatedAmount }
            if (asset.balance < currentAllocated) {
                throw IllegalArgumentException(
                    "Saldo baru (${Formatters.formatRupiah(asset.balance)}) tidak boleh lebih kecil dari total dana yang dialokasikan (${Formatters.formatRupiah(currentAllocated)})"
                )
            }
            assetDao.updateAsset(AssetEntity.fromDomain(asset))
        }
    }

    override suspend fun deleteAsset(assetId: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val pockets = pocketDao.getPocketsByAssetSync(assetId).filter { it.isActive }
            if (pockets.isNotEmpty()) {
                throw IllegalStateException("Tidak dapat menghapus aset yang masih memiliki kantong alokasi aktif")
            }
            assetDao.deleteAssetById(assetId)
        }
    }

    // --- Pocket Operations ---

    override fun getAllPockets(): Flow<List<Pocket>> {
        return combine(
            pocketDao.getAllPockets(),
            assetDao.getAllAssets()
        ) { pockets, assets ->
            val assetMap = assets.associate { it.id to it.name }
            pockets.map { it.toDomain(assetName = assetMap[it.assetId] ?: "") }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getPocketById(id: String): Pocket? = withContext(Dispatchers.IO) {
        val entity = pocketDao.getPocketById(id) ?: return@withContext null
        val assetName = assetDao.getAssetById(entity.assetId)?.name ?: ""
        entity.toDomain(assetName = assetName)
    }

    override suspend fun insertPocket(pocket: Pocket) = withContext(Dispatchers.IO) {
        require(pocket.allocatedAmount >= 0) { "Dana dialokasikan tidak boleh negatif" }
        require(pocket.name.isNotBlank()) { "Nama kantong tidak boleh kosong" }
        database.withTransaction {
            val asset = assetDao.getAssetById(pocket.assetId)
                ?: throw IllegalArgumentException("Aset sumber tidak ditemukan")
            val existingPockets = pocketDao.getPocketsByAssetSync(pocket.assetId).filter { it.isActive }
            val currentAllocated = existingPockets.sumOf { it.allocatedAmount }
            val available = asset.balance - currentAllocated
            if (pocket.allocatedAmount > available + 0.0001) {
                throw IllegalArgumentException(
                    "Dana dialokasikan (${Formatters.formatRupiah(pocket.allocatedAmount)}) melebihi saldo tersedia dari aset (${Formatters.formatRupiah(available)})"
                )
            }
            pocketDao.insertPocket(PocketEntity.fromDomain(pocket))
        }
    }

    override suspend fun updatePocket(pocket: Pocket) = withContext(Dispatchers.IO) {
        require(pocket.allocatedAmount >= 0) { "Dana dialokasikan tidak boleh negatif" }
        require(pocket.name.isNotBlank()) { "Nama kantong tidak boleh kosong" }
        database.withTransaction {
            val existing = pocketDao.getPocketById(pocket.id)
                ?: throw IllegalArgumentException("Kantong tidak ditemukan")
            val targetAsset = assetDao.getAssetById(pocket.assetId)
                ?: throw IllegalArgumentException("Aset sumber tidak ditemukan")

            val otherPockets = pocketDao.getPocketsByAssetSync(pocket.assetId)
                .filter { it.id != pocket.id && it.isActive }
            val currentAllocatedByOthers = otherPockets.sumOf { it.allocatedAmount }
            val availableForThis = targetAsset.balance - currentAllocatedByOthers

            if (pocket.allocatedAmount > availableForThis + 0.0001) {
                throw IllegalArgumentException(
                    "Alokasi baru (${Formatters.formatRupiah(pocket.allocatedAmount)}) melebihi saldo tersedia dari aset (${Formatters.formatRupiah(availableForThis)})"
                )
            }
            pocketDao.updatePocket(PocketEntity.fromDomain(pocket))
        }
    }

    override suspend fun deletePocket(pocketId: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            // Deleting a pocket releases its allocation back to the asset's available balance
            pocketDao.deletePocketById(pocketId)
        }
    }

    override suspend fun movePocketAllocation(
        pocketId: String,
        targetAssetId: String,
        newAllocatedAmount: Double
    ) = withContext(Dispatchers.IO) {
        require(newAllocatedAmount >= 0) { "Alokasi tidak boleh negatif" }
        database.withTransaction {
            val pocket = pocketDao.getPocketById(pocketId)
                ?: throw IllegalArgumentException("Kantong tidak ditemukan")
            val targetAsset = assetDao.getAssetById(targetAssetId)
                ?: throw IllegalArgumentException("Aset tujuan tidak ditemukan")

            val otherPocketsOnTarget = pocketDao.getPocketsByAssetSync(targetAssetId)
                .filter { it.id != pocketId && it.isActive }
            val availableOnTarget = targetAsset.balance - otherPocketsOnTarget.sumOf { it.allocatedAmount }

            if (newAllocatedAmount > availableOnTarget + 0.0001) {
                throw IllegalArgumentException(
                    "Alokasi melebihi saldo tersedia pada aset tujuan (${Formatters.formatRupiah(availableOnTarget)})"
                )
            }
            val updated = pocket.copy(assetId = targetAssetId, allocatedAmount = newAllocatedAmount)
            pocketDao.updatePocket(updated)
        }
    }

    // --- Category Operations ---

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }
    }

    override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(CategoryEntity.fromDomain(category))
    }

    // --- Transaction Operations ---

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun getTransactionsByPocket(pocketId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByPocket(pocketId).map { list -> list.map { it.toDomain() } }
    }

    override fun getTransactionsByAsset(assetId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByAsset(assetId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTransactionById(id: String): Transaction? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        require(transaction.amount > 0) { "Jumlah transaksi harus lebih besar dari nol" }
        database.withTransaction {
            val sourceAsset = assetDao.getAssetById(transaction.assetId)
                ?: throw IllegalArgumentException("Aset tidak ditemukan")

            when (transaction.type) {
                TransactionType.INCOME -> {
                    // Income increases the selected Asset balance
                    val updatedAsset = sourceAsset.copy(balance = sourceAsset.balance + transaction.amount)
                    assetDao.updateAsset(updatedAsset)
                }
                TransactionType.EXPENSE -> {
                    // Expense decreases the selected Asset balance
                    if (sourceAsset.balance < transaction.amount) {
                        throw IllegalArgumentException("Saldo aset tidak mencukupi untuk transaksi ini")
                    }
                    val updatedAsset = sourceAsset.copy(balance = sourceAsset.balance - transaction.amount)
                    assetDao.updateAsset(updatedAsset)

                    // If an Expense has a pocketId, also decrease that Pocket's allocatedAmount by the expense amount
                    transaction.pocketId?.let { pId ->
                        val pocket = pocketDao.getPocketById(pId)
                        if (pocket != null) {
                            val updatedPocket = pocket.copy(
                                allocatedAmount = (pocket.allocatedAmount - transaction.amount).coerceAtLeast(0.0)
                            )
                            pocketDao.updatePocket(updatedPocket)
                        }
                    }
                }
                TransactionType.TRANSFER -> {
                    // Transfer moves money between Assets without changing total Saldo Asli
                    val targetAssetId = transaction.targetAssetId
                        ?: throw IllegalArgumentException("Aset tujuan transfer harus dipilih")
                    val targetAsset = assetDao.getAssetById(targetAssetId)
                        ?: throw IllegalArgumentException("Aset tujuan transfer tidak ditemukan")

                    if (sourceAsset.balance < transaction.amount) {
                        throw IllegalArgumentException("Saldo aset sumber tidak mencukupi untuk transfer")
                    }

                    val updatedSource = sourceAsset.copy(balance = sourceAsset.balance - transaction.amount)
                    val updatedTarget = targetAsset.copy(balance = targetAsset.balance + transaction.amount)
                    assetDao.updateAsset(updatedSource)
                    assetDao.updateAsset(updatedTarget)
                }
            }

            // Populate readable names for denormalization
            val assetName = if (transaction.assetName.isNotBlank()) transaction.assetName else sourceAsset.name
            val pocketName = if (!transaction.pocketId.isNullOrBlank()) {
                if (!transaction.pocketName.isNullOrBlank()) transaction.pocketName else pocketDao.getPocketById(transaction.pocketId)?.name
            } else null
            val targetAssetName = if (!transaction.targetAssetId.isNullOrBlank()) {
                if (!transaction.targetAssetName.isNullOrBlank()) transaction.targetAssetName else assetDao.getAssetById(transaction.targetAssetId)?.name
            } else null

            val completeTx = transaction.copy(
                assetName = assetName,
                pocketName = pocketName,
                targetAssetName = targetAssetName
            )
            transactionDao.insertTransaction(TransactionEntity.fromDomain(completeTx))
        }
    }

    override suspend fun deleteTransaction(transactionId: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val txEntity = transactionDao.getTransactionById(transactionId) ?: return@withTransaction
            val transaction = txEntity.toDomain()

            val sourceAsset = assetDao.getAssetById(transaction.assetId)
            if (sourceAsset != null) {
                when (transaction.type) {
                    TransactionType.EXPENSE -> {
                        // Reversing expense increases Asset balance
                        val updatedAsset = sourceAsset.copy(balance = sourceAsset.balance + transaction.amount)
                        assetDao.updateAsset(updatedAsset)

                        // And reverses Pocket allocation if pocketId was specified
                        transaction.pocketId?.let { pId ->
                            val pocket = pocketDao.getPocketById(pId)
                            if (pocket != null) {
                                val updatedPocket = pocket.copy(allocatedAmount = pocket.allocatedAmount + transaction.amount)
                                pocketDao.updatePocket(updatedPocket)
                            }
                        }
                    }
                    TransactionType.INCOME -> {
                        // Reversing income decreases Asset balance
                        val updatedAsset = sourceAsset.copy(balance = (sourceAsset.balance - transaction.amount).coerceAtLeast(0.0))
                        assetDao.updateAsset(updatedAsset)
                    }
                    TransactionType.TRANSFER -> {
                        // Reversing transfer refunds source and debits target
                        val updatedSource = sourceAsset.copy(balance = sourceAsset.balance + transaction.amount)
                        assetDao.updateAsset(updatedSource)

                        transaction.targetAssetId?.let { targetId ->
                            val targetAsset = assetDao.getAssetById(targetId)
                            if (targetAsset != null) {
                                val updatedTarget = targetAsset.copy(balance = (targetAsset.balance - transaction.amount).coerceAtLeast(0.0))
                                assetDao.updateAsset(updatedTarget)
                            }
                        }
                    }
                }
            }

            transactionDao.deleteTransactionById(transactionId)
        }
    }

    // --- Financial Summary & Reports ---

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(
            assetDao.getAllAssets(),
            pocketDao.getAllPockets(),
            transactionDao.getAllTransactions()
        ) { assets, pockets, transactions ->
            val activeAssets = assets.filter { it.isActive }
            val activePockets = pockets.filter { it.isActive }

            val totalActualBalance = activeAssets.sumOf { it.balance }
            val totalAllocatedAmount = activePockets.sumOf { it.allocatedAmount }
            val totalAvailableBalance = totalActualBalance - totalAllocatedAmount

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
                totalActualBalance = totalActualBalance,
                totalAllocatedAmount = totalAllocatedAmount,
                totalAvailableBalance = totalAvailableBalance,
                totalIncomeThisMonth = monthIncome,
                totalExpenseThisMonth = monthExpense,
                netSavingsThisMonth = netSavings,
                savingsRatePercentage = rate
            )
        }.flowOn(Dispatchers.IO)
    }

    override fun getFinancialReport(periodMonth: Int, periodYear: Int): Flow<FinancialReport> {
        return combine(
            assetDao.getAllAssets(),
            pocketDao.getAllPockets(),
            transactionDao.getAllTransactions()
        ) { assets, pockets, transactions ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, periodYear)
            cal.set(Calendar.MONTH, periodMonth)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startMillis = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val endMillis = cal.timeInMillis - 1

            val filtered = transactions.filter { it.dateMillis in startMillis..endMillis }

            var income = 0.0
            var expense = 0.0
            val expenseByCategory = mutableMapOf<String, Double>()
            val categoryInfo = mutableMapOf<String, Pair<String, String>>()

            filtered.forEach { tx ->
                when (tx.typeString) {
                    TransactionType.INCOME.name -> income += tx.amount
                    TransactionType.EXPENSE.name -> {
                        expense += tx.amount
                        expenseByCategory[tx.categoryName] = (expenseByCategory[tx.categoryName] ?: 0.0) + tx.amount
                        categoryInfo[tx.categoryName] = Pair(tx.categoryIcon, "#153E35")
                    }
                }
            }

            val breakdown = expenseByCategory.map { (catName, total) ->
                val pct = if (expense > 0) ((total / expense) * 100).toFloat() else 0f
                val info = categoryInfo[catName] ?: Pair("receipt_long", "#153E35")
                CategoryExpenseSummary(
                    categoryName = catName,
                    categoryIcon = info.first,
                    totalAmount = total,
                    percentage = pct,
                    colorHex = info.second
                )
            }.sortedByDescending { it.totalAmount }

            val totalActual = assets.filter { it.isActive }.sumOf { it.balance }
            val totalAllocated = pockets.filter { it.isActive }.sumOf { it.allocatedAmount }
            val totalAvailable = totalActual - totalAllocated

            val net = income - expense
            val rate = if (income > 0) ((net / income) * 100).toInt().coerceIn(0, 100) else 0

            val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
            val title = "${monthNames.getOrElse(periodMonth) { "" }} $periodYear"

            FinancialReport(
                periodTitle = title,
                summary = FinancialSummary(
                    totalActualBalance = totalActual,
                    totalAllocatedAmount = totalAllocated,
                    totalAvailableBalance = totalAvailable,
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

    // --- Export and Restore ---

    override suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("ID,Tanggal,Judul,Nominal,Tipe,Kategori,Aset,Kantong,Catatan\n")
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM transactions ORDER BY dateMillis DESC")
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val date = Formatters.formatShortDateIndo(cursor.getLong(cursor.getColumnIndexOrThrow("dateMillis")))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title")).replace(",", ";")
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("typeString"))
            val cat = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"))
            val asset = cursor.getString(cursor.getColumnIndexOrThrow("assetName"))
            val pocketIdx = cursor.getColumnIndexOrThrow("pocketName")
            val pocket = if (!cursor.isNull(pocketIdx)) cursor.getString(pocketIdx) else "-"
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note")).replace(",", ";")
            sb.append("$id,$date,$title,$amount,$type,$cat,$asset,$pocket,$note\n")
        }
        cursor.close()
        sb.toString()
    }

    override suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("appName", "Saku")
        root.put("version", "2.0")
        root.put("exportDate", System.currentTimeMillis())

        // Assets
        val assetsArray = JSONArray()
        val aCursor = database.openHelper.readableDatabase.query("SELECT * FROM assets")
        while (aCursor.moveToNext()) {
            val aObj = JSONObject()
            aObj.put("id", aCursor.getString(aCursor.getColumnIndexOrThrow("id")))
            aObj.put("name", aCursor.getString(aCursor.getColumnIndexOrThrow("name")))
            aObj.put("typeString", aCursor.getString(aCursor.getColumnIndexOrThrow("typeString")))
            aObj.put("balance", aCursor.getDouble(aCursor.getColumnIndexOrThrow("balance")))
            aObj.put("currency", aCursor.getString(aCursor.getColumnIndexOrThrow("currency")))
            aObj.put("isActive", aCursor.getInt(aCursor.getColumnIndexOrThrow("isActive")) == 1)
            assetsArray.put(aObj)
        }
        aCursor.close()
        root.put("assets", assetsArray)

        // Pockets
        val pocketsArray = JSONArray()
        val pCursor = database.openHelper.readableDatabase.query("SELECT * FROM pockets")
        while (pCursor.moveToNext()) {
            val pObj = JSONObject()
            pObj.put("id", pCursor.getString(pCursor.getColumnIndexOrThrow("id")))
            pObj.put("name", pCursor.getString(pCursor.getColumnIndexOrThrow("name")))
            pObj.put("assetId", pCursor.getString(pCursor.getColumnIndexOrThrow("assetId")))
            pObj.put("allocatedAmount", pCursor.getDouble(pCursor.getColumnIndexOrThrow("allocatedAmount")))
            pObj.put("targetAmount", pCursor.getDouble(pCursor.getColumnIndexOrThrow("targetAmount")))
            pObj.put("color", pCursor.getString(pCursor.getColumnIndexOrThrow("color")))
            pObj.put("icon", pCursor.getString(pCursor.getColumnIndexOrThrow("icon")))
            pObj.put("isActive", pCursor.getInt(pCursor.getColumnIndexOrThrow("isActive")) == 1)
            pocketsArray.put(pObj)
        }
        pCursor.close()
        root.put("pockets", pocketsArray)

        // Categories
        val catArray = JSONArray()
        val cCursor = database.openHelper.readableDatabase.query("SELECT * FROM categories")
        while (cCursor.moveToNext()) {
            val cObj = JSONObject()
            cObj.put("id", cCursor.getString(cCursor.getColumnIndexOrThrow("id")))
            cObj.put("name", cCursor.getString(cCursor.getColumnIndexOrThrow("name")))
            cObj.put("typeString", cCursor.getString(cCursor.getColumnIndexOrThrow("typeString")))
            cObj.put("iconName", cCursor.getString(cCursor.getColumnIndexOrThrow("iconName")))
            cObj.put("colorHex", cCursor.getString(cCursor.getColumnIndexOrThrow("colorHex")))
            catArray.put(cObj)
        }
        cCursor.close()
        root.put("categories", catArray)

        // Transactions
        val txArray = JSONArray()
        val tCursor = database.openHelper.readableDatabase.query("SELECT * FROM transactions")
        while (tCursor.moveToNext()) {
            val tObj = JSONObject()
            tObj.put("id", tCursor.getString(tCursor.getColumnIndexOrThrow("id")))
            tObj.put("title", tCursor.getString(tCursor.getColumnIndexOrThrow("title")))
            tObj.put("amount", tCursor.getDouble(tCursor.getColumnIndexOrThrow("amount")))
            tObj.put("typeString", tCursor.getString(tCursor.getColumnIndexOrThrow("typeString")))
            tObj.put("categoryId", tCursor.getString(tCursor.getColumnIndexOrThrow("categoryId")))
            tObj.put("categoryName", tCursor.getString(tCursor.getColumnIndexOrThrow("categoryName")))
            tObj.put("categoryIcon", tCursor.getString(tCursor.getColumnIndexOrThrow("categoryIcon")))
            tObj.put("assetId", tCursor.getString(tCursor.getColumnIndexOrThrow("assetId")))
            tObj.put("assetName", tCursor.getString(tCursor.getColumnIndexOrThrow("assetName")))

            val pocketIdIdx = tCursor.getColumnIndexOrThrow("pocketId")
            if (!tCursor.isNull(pocketIdIdx)) tObj.put("pocketId", tCursor.getString(pocketIdIdx))
            val pocketNameIdx = tCursor.getColumnIndexOrThrow("pocketName")
            if (!tCursor.isNull(pocketNameIdx)) tObj.put("pocketName", tCursor.getString(pocketNameIdx))

            val targetAssetIdIdx = tCursor.getColumnIndexOrThrow("targetAssetId")
            if (!tCursor.isNull(targetAssetIdIdx)) tObj.put("targetAssetId", tCursor.getString(targetAssetIdIdx))
            val targetAssetNameIdx = tCursor.getColumnIndexOrThrow("targetAssetName")
            if (!tCursor.isNull(targetAssetNameIdx)) tObj.put("targetAssetName", tCursor.getString(targetAssetNameIdx))

            tObj.put("dateMillis", tCursor.getLong(tCursor.getColumnIndexOrThrow("dateMillis")))
            tObj.put("note", tCursor.getString(tCursor.getColumnIndexOrThrow("note")))
            val receiptImageUrlIdx = tCursor.getColumnIndexOrThrow("receiptImageUrl")
            if (!tCursor.isNull(receiptImageUrlIdx)) {
                tObj.put("receiptImageUrl", tCursor.getString(receiptImageUrlIdx))
            }
            txArray.put(tObj)
        }
        tCursor.close()
        root.put("transactions", txArray)

        root.toString(2)
    }

    override suspend fun restoreDataFromJson(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (json.isBlank()) return@withContext false
            val root = JSONObject(json)

            val restoredAssets = mutableListOf<AssetEntity>()
            val assetIds = mutableSetOf<String>()

            // If new format has assets
            if (root.has("assets")) {
                val assetsArray = root.optJSONArray("assets") ?: return@withContext false
                for (i in 0 until assetsArray.length()) {
                    val a = assetsArray.optJSONObject(i) ?: return@withContext false
                    val id = a.optString("id", "")
                    val name = a.optString("name", "")
                    if (id.isBlank() || name.isBlank()) return@withContext false
                    val balance = a.optDouble("balance", Double.NaN)
                    if (balance.isNaN() || balance < 0) return@withContext false

                    restoredAssets.add(
                        AssetEntity(
                            id = id,
                            name = name,
                            typeString = a.optString("typeString", "BANK"),
                            balance = balance,
                            currency = a.optString("currency", "IDR"),
                            isActive = a.optBoolean("isActive", true)
                        )
                    )
                    assetIds.add(id)
                }
            } else if (root.has("pockets")) {
                // Backward compatibility fallback: if old backup only has pockets, safely create an asset for each old pocket
                val pocketsArray = root.optJSONArray("pockets") ?: return@withContext false
                for (i in 0 until pocketsArray.length()) {
                    val p = pocketsArray.optJSONObject(i) ?: return@withContext false
                    val id = p.optString("id", "asset_$i")
                    val name = p.optString("name", "Aset $i")
                    val balance = p.optDouble("balance", 0.0)
                    restoredAssets.add(
                        AssetEntity(
                            id = id,
                            name = name,
                            typeString = if (p.optBoolean("isMain", false)) "BANK" else "OTHER",
                            balance = balance,
                            currency = "IDR",
                            isActive = true
                        )
                    )
                    assetIds.add(id)
                }
            }

            val restoredPockets = mutableListOf<PocketEntity>()
            if (root.has("pockets")) {
                val pocketsArray = root.optJSONArray("pockets") ?: return@withContext false
                for (i in 0 until pocketsArray.length()) {
                    val p = pocketsArray.optJSONObject(i) ?: return@withContext false
                    val name = p.optString("name", "")
                    if (name.isBlank()) return@withContext false

                    val id = p.optString("id", "pocket_${System.currentTimeMillis()}_$i")
                    var assetId = p.optString("assetId", "")
                    if (assetId.isBlank() || !assetIds.contains(assetId)) {
                        assetId = restoredAssets.firstOrNull()?.id ?: ""
                    }
                    if (assetId.isBlank()) return@withContext false

                    val allocated = if (p.has("allocatedAmount")) {
                        p.optDouble("allocatedAmount", 0.0)
                    } else 0.0 // from old pocket model, allocations start clean or 0

                    if (allocated < 0) return@withContext false

                    restoredPockets.add(
                        PocketEntity(
                            id = id,
                            name = name,
                            assetId = assetId,
                            allocatedAmount = allocated,
                            targetAmount = p.optDouble("targetAmount", 0.0),
                            color = p.optString("color", p.optString("colorHex", "#153E35")),
                            icon = p.optString("icon", p.optString("iconName", "account_balance_wallet")),
                            isActive = p.optBoolean("isActive", true)
                        )
                    )
                }
            }

            // Validate pocket allocations do not exceed asset balances
            val totalAllocatedByAsset = restoredPockets.filter { it.isActive }.groupBy { it.assetId }
            for ((aId, pList) in totalAllocatedByAsset) {
                val asset = restoredAssets.find { it.id == aId } ?: return@withContext false
                val totalAlloc = pList.sumOf { it.allocatedAmount }
                if (totalAlloc > asset.balance + 0.0001) {
                    return@withContext false // Validation failure: allocation exceeds asset balance
                }
            }

            val restoredCategories = mutableListOf<CategoryEntity>()
            if (root.has("categories")) {
                val catArray = root.optJSONArray("categories")
                if (catArray != null) {
                    for (i in 0 until catArray.length()) {
                        val c = catArray.optJSONObject(i) ?: continue
                        val id = c.optString("id", "")
                        val name = c.optString("name", "")
                        if (id.isNotBlank() && name.isNotBlank()) {
                            restoredCategories.add(
                                CategoryEntity(
                                    id = id,
                                    name = name,
                                    typeString = c.optString("typeString", "EXPENSE"),
                                    iconName = c.optString("iconName", "category"),
                                    colorHex = c.optString("colorHex", "#153E35")
                                )
                            )
                        }
                    }
                }
            }

            val restoredTransactions = mutableListOf<TransactionEntity>()
            if (root.has("transactions")) {
                val txArray = root.optJSONArray("transactions") ?: return@withContext false
                for (i in 0 until txArray.length()) {
                    val t = txArray.optJSONObject(i) ?: return@withContext false
                    val title = t.optString("title", "")
                    if (title.isBlank()) return@withContext false
                    val amount = t.optDouble("amount", Double.NaN)
                    if (amount.isNaN() || amount < 0) return@withContext false

                    val typeString = t.optString("typeString", "EXPENSE")
                    var assetId = t.optString("assetId", t.optString("pocketId", ""))
                    if (!assetIds.contains(assetId)) {
                        assetId = restoredAssets.firstOrNull()?.id ?: ""
                    }
                    if (assetId.isBlank()) return@withContext false

                    val assetName = restoredAssets.find { it.id == assetId }?.name ?: ""
                    val pocketId = if (t.has("pocketId") && !t.isNull("pocketId") && root.has("assets")) t.optString("pocketId") else null
                    val pocketName = if (!pocketId.isNullOrBlank()) restoredPockets.find { it.id == pocketId }?.name else null

                    val targetAssetId = if (t.has("targetAssetId") && !t.isNull("targetAssetId")) {
                        t.optString("targetAssetId")
                    } else if (t.has("targetPocketId") && !t.isNull("targetPocketId")) {
                        t.optString("targetPocketId")
                    } else null
                    val targetAssetName = if (!targetAssetId.isNullOrBlank()) restoredAssets.find { it.id == targetAssetId }?.name else null

                    restoredTransactions.add(
                        TransactionEntity(
                            id = t.optString("id", "tx_${System.currentTimeMillis()}_$i"),
                            title = title,
                            amount = amount,
                            typeString = typeString,
                            categoryId = t.optString("categoryId", "cat_other_exp"),
                            categoryName = t.optString("categoryName", "Lainnya"),
                            categoryIcon = t.optString("categoryIcon", "receipt_long"),
                            assetId = assetId,
                            assetName = assetName,
                            pocketId = pocketId,
                            pocketName = pocketName,
                            targetAssetId = targetAssetId,
                            targetAssetName = targetAssetName,
                            dateMillis = t.optLong("dateMillis", System.currentTimeMillis()),
                            note = t.optString("note", ""),
                            receiptImageUrl = if (t.has("receiptImageUrl") && !t.isNull("receiptImageUrl")) t.optString("receiptImageUrl") else null
                        )
                    )
                }
            }

            // ATOMIC DATABASE UPDATE
            database.withTransaction {
                if (restoredAssets.isNotEmpty()) {
                    assetDao.clearAll()
                    assetDao.insertAssets(restoredAssets)
                }
                if (restoredPockets.isNotEmpty()) {
                    pocketDao.clearAll()
                    pocketDao.insertPockets(restoredPockets)
                }
                if (restoredCategories.isNotEmpty()) {
                    categoryDao.clearAll()
                    categoryDao.insertCategories(restoredCategories)
                }
                if (restoredTransactions.isNotEmpty()) {
                    transactionDao.clearAll()
                    transactionDao.insertTransactions(restoredTransactions)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun resetToDefaultData() = withContext(Dispatchers.IO) {
        database.withTransaction {
            assetDao.clearAll()
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

            val defaultAssets = listOf(
                AssetEntity("asset_bca", "BCA", "BANK", 15000000.0, "IDR", true),
                AssetEntity("asset_bri", "BRI", "BANK", 10000000.0, "IDR", true),
                AssetEntity("asset_cash", "Dompet Tunai", "CASH", 4150000.0, "IDR", true)
            )
            assetDao.insertAssets(defaultAssets)

            val defaultPockets = listOf(
                PocketEntity("pocket_needs", "Kebutuhan Pokok", "asset_bca", 3450000.0, 5000000.0, "#1F594D", "shopping_cart", true),
                PocketEntity("pocket_savings", "Tabungan Masa Depan", "asset_bri", 8200000.0, 15000000.0, "#C89535", "savings", true),
                PocketEntity("pocket_emergency", "Dana Darurat", "asset_bca", 5000000.0, 10000000.0, "#286F60", "health_and_safety", true)
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
                TransactionEntity("tx_1", "Gaji Bulanan", 12000000.0, "INCOME", "cat_salary", "Gaji Pokok", "payments", "asset_bca", "BCA", null, null, null, null, now - (2 * day), "Gaji bulanan masuk", null),
                TransactionEntity("tx_2", "Belanja Mingguan Supermarket", 650000.0, "EXPENSE", "cat_shopping", "Belanja Bulanan", "shopping_bag", "asset_bca", "BCA", "pocket_needs", "Kebutuhan Pokok", null, null, now - (1 * day), "Bahan makanan mingguan", null),
                TransactionEntity("tx_3", "Makan Siang Resto", 45000.0, "EXPENSE", "cat_food", "Makanan & Minuman", "restaurant", "asset_cash", "Dompet Tunai", "pocket_needs", "Kebutuhan Pokok", null, null, now - (4 * 3600 * 1000), "Makan siang kantor", null)
            )
            transactionDao.insertTransactions(defaultTransactions)
        }
    }
}
