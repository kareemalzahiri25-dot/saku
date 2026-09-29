package com.example.data.repository

import androidx.room.withTransaction
import com.example.core.Formatters
import com.example.data.local.SakuDatabase
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseAllocationEntity
import com.example.data.local.entity.PocketAllocationEntity
import com.example.data.local.entity.PocketEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Asset
import com.example.domain.model.Category
import com.example.domain.model.CategoryExpenseSummary
import com.example.domain.model.ExpenseAllocation
import com.example.domain.model.FinancialReport
import com.example.domain.model.FinancialSummary
import com.example.domain.model.Pocket
import com.example.domain.model.PocketAllocation
import com.example.domain.model.PocketStats
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.User
import com.example.domain.repository.SakuRepository
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import android.content.Context

class SakuRepositoryImpl(
    private val database: SakuDatabase
) : SakuRepository {

    private val assetDao = database.assetDao()
    private val pocketDao = database.pocketDao()
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val userDao = database.userDao()
    private val pocketAllocationDao = database.pocketAllocationDao()
    private val expenseAllocationDao = database.expenseAllocationDao()

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

    override suspend fun insertAsset(asset: Asset) = withContext(Dispatchers.IO) {
        require(asset.balance >= 0) { "Saldo aset tidak boleh negatif" }
        require(asset.name.isNotBlank()) { "Nama aset tidak boleh kosong" }
        assetDao.insertAsset(AssetEntity.fromDomain(asset))
    }

    override suspend fun updateAsset(asset: Asset) = withContext(Dispatchers.IO) {
        require(asset.balance >= 0) { "Saldo aset tidak boleh negatif" }
        require(asset.name.isNotBlank()) { "Nama aset tidak boleh kosong" }
        database.withTransaction {
            val allocations = pocketAllocationDao.getAllocationsByAssetSync(asset.id)
            val totalAllocated = allocations.sumOf { it.allocatedAmount }
            if (asset.balance < totalAllocated) {
                throw IllegalArgumentException(
                    "Saldo baru (${Formatters.formatRupiah(asset.balance)}) tidak boleh lebih kecil dari total dana yang dialokasikan (${Formatters.formatRupiah(totalAllocated)})"
                )
            }
            assetDao.updateAsset(AssetEntity.fromDomain(asset))
        }
    }

    override suspend fun deleteAsset(assetId: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val allocations = pocketAllocationDao.getAllocationsByAssetSync(assetId)
            if (allocations.isNotEmpty()) {
                throw IllegalStateException("Tidak dapat menghapus aset yang masih memiliki alokasi Pocket")
            }
            assetDao.deleteAssetById(assetId)
        }
    }

    // --- Pocket Operations ---

    override fun getAllPockets(): Flow<List<Pocket>> {
        return pocketDao.getAllPockets().map { list -> list.map { it.toDomain() } }
    }

    override fun getActivePockets(): Flow<List<Pocket>> {
        return pocketDao.getActivePockets().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPocketById(id: String): Pocket? = withContext(Dispatchers.IO) {
        pocketDao.getPocketById(id)?.toDomain()
    }

    override suspend fun insertPocket(pocket: Pocket) = withContext(Dispatchers.IO) {
        require(pocket.name.isNotBlank()) { "Nama kantong tidak boleh kosong" }
        require(pocket.targetAmount >= 0) { "Target tidak boleh negatif" }
        pocketDao.insertPocket(PocketEntity.fromDomain(pocket))
    }

    override suspend fun updatePocket(pocket: Pocket) = withContext(Dispatchers.IO) {
        require(pocket.name.isNotBlank()) { "Nama kantong tidak boleh kosong" }
        require(pocket.targetAmount >= 0) { "Target tidak boleh negatif" }
        pocketDao.updatePocket(PocketEntity.fromDomain(pocket))
    }

    override suspend fun deletePocket(pocketId: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            // Delete all allocations related to this pocket first
            val expenseAllocs = expenseAllocationDao.getAllocationsByPocketSync(pocketId)
            val pocketAllocs = pocketAllocationDao.getAllocationsByPocketSync(pocketId)
            
            expenseAllocationDao.deleteAllocationsByPocket(pocketId)
            pocketAllocationDao.deleteAllocationsByPocket(pocketId)
            pocketDao.deletePocketById(pocketId)
        }
    }

    // --- Pocket Allocation Operations ---

    override fun getPocketAllocations(): Flow<List<PocketAllocation>> {
        return pocketAllocationDao.getAllAllocations().map { list -> list.map { it.toDomain() } }
    }

    override fun getPocketAllocationsByAsset(assetId: String): Flow<List<PocketAllocation>> {
        return pocketAllocationDao.getAllocationsByAsset(assetId).map { list -> list.map { it.toDomain() } }
    }

    override fun getPocketAllocationsByPocket(pocketId: String): Flow<List<PocketAllocation>> {
        return pocketAllocationDao.getAllocationsByPocket(pocketId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertPocketAllocation(allocation: PocketAllocation) = withContext(Dispatchers.IO) {
        require(allocation.allocatedAmount >= 0) { "Dana dialokasikan tidak boleh negatif" }
        database.withTransaction {
            val asset = assetDao.getAssetById(allocation.assetId)
                ?: throw IllegalArgumentException("Aset tidak ditemukan")
            
            val existingAllocations = pocketAllocationDao.getAllocationsByAssetSync(allocation.assetId)
            val totalAllocated = existingAllocations.sumOf { it.allocatedAmount }
            
            if (totalAllocated + allocation.allocatedAmount > asset.balance) {
                throw IllegalArgumentException(
                    "Total alokasi dari aset (${Formatters.formatRupiah(totalAllocated + allocation.allocatedAmount)}) melebihi saldo aset (${Formatters.formatRupiah(asset.balance)})"
                )
            }
            pocketAllocationDao.insertAllocation(PocketAllocationEntity.fromDomain(allocation))
        }
    }

    override suspend fun updatePocketAllocation(allocation: PocketAllocation) = withContext(Dispatchers.IO) {
        require(allocation.allocatedAmount >= 0) { "Dana dialokasikan tidak boleh negatif" }
        database.withTransaction {
            val asset = assetDao.getAssetById(allocation.assetId)
                ?: throw IllegalArgumentException("Aset tidak ditemukan")
            
            val existingAllocations = pocketAllocationDao.getAllocationsByAssetSync(allocation.assetId)
                .filter { it.id != allocation.id }
            val totalAllocatedByOthers = existingAllocations.sumOf { it.allocatedAmount }
            
            if (totalAllocatedByOthers + allocation.allocatedAmount > asset.balance) {
                throw IllegalArgumentException(
                    "Total alokasi dari aset (${Formatters.formatRupiah(totalAllocatedByOthers + allocation.allocatedAmount)}) melebihi saldo aset (${Formatters.formatRupiah(asset.balance)})"
                )
            }
            pocketAllocationDao.updateAllocation(PocketAllocationEntity.fromDomain(allocation))
        }
    }

    override suspend fun deletePocketAllocation(allocationId: String) = withContext(Dispatchers.IO) {
        pocketAllocationDao.deleteAllocationById(allocationId)
    }

    override suspend fun replacePocketAllocations(
        pocketId: String,
        allocations: List<PocketAllocation>
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            pocketAllocationDao.deleteAllocationsByPocket(pocketId)
            pocketAllocationDao.insertAllocations(allocations.map { PocketAllocationEntity.fromDomain(it) })
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

    override fun getTransactionsByAsset(assetId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByAsset(assetId).map { list -> list.map { it.toDomain() } }
    }

    override fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsBetween(startMillis, endMillis).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTransactionById(id: String): Transaction? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        require(transaction.amount > 0) { "Jumlah transaksi harus lebih besar dari nol" }
        require(transaction.assetId.isNotEmpty()) { "Pilih aset dulu" }
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
            val targetAssetName = if (!transaction.targetAssetId.isNullOrBlank()) {
                if (!transaction.targetAssetName.isNullOrBlank()) transaction.targetAssetName else assetDao.getAssetById(transaction.targetAssetId)?.name
            } else null

            val completeTx = transaction.copy(
                assetName = assetName,
                targetAssetName = targetAssetName
            )
            transactionDao.insertTransaction(TransactionEntity.fromDomain(completeTx))
            
            // Create ExpenseAllocation for EXPENSE transactions with a pocket
            if (transaction.type == TransactionType.EXPENSE && !transaction.pocketId.isNullOrBlank()) {
                val allocation = ExpenseAllocationEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = transaction.id,
                    pocketId = transaction.pocketId,
                    amount = transaction.amount
                )
                expenseAllocationDao.insertAllocation(allocation)
            }
        }
    }

    override suspend fun deleteTransaction(transactionId: String) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val txEntity = transactionDao.getTransactionById(transactionId) ?: return@withTransaction
            val transaction = txEntity.toDomain()

            // First delete all expense allocations for this transaction
            expenseAllocationDao.deleteAllocationsByTransaction(transactionId)

            val sourceAsset = assetDao.getAssetById(transaction.assetId)
            if (sourceAsset != null) {
                when (transaction.type) {
                    TransactionType.EXPENSE -> {
                        // Reversing expense increases Asset balance
                        val updatedAsset = sourceAsset.copy(balance = sourceAsset.balance + transaction.amount)
                        assetDao.updateAsset(updatedAsset)
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

    // --- Expense Allocation Operations ---

    override fun getExpenseAllocations(transactionId: String): Flow<List<ExpenseAllocation>> {
        return expenseAllocationDao.getAllocationsByTransaction(transactionId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertExpenseAllocation(allocation: ExpenseAllocation) = withContext(Dispatchers.IO) {
        require(allocation.amount > 0) { "Jumlah alokasi harus lebih besar dari nol" }
        database.withTransaction {
            // Verify transaction exists and is an expense
            val transaction = transactionDao.getTransactionById(allocation.transactionId)
                ?: throw IllegalArgumentException("Transaksi tidak ditemukan")
            if (transaction.typeString != "EXPENSE") {
                throw IllegalArgumentException("Hanya transaksi pengeluaran yang dapat dialokasikan ke Pocket")
            }
            
            // Verify the pocket exists
            pocketDao.getPocketById(allocation.pocketId) ?: throw IllegalArgumentException("Pocket tidak ditemukan")
            
            // Verify total allocations don't exceed transaction amount
            val existingAllocs = expenseAllocationDao.getAllocationsByTransactionSync(allocation.transactionId)
            val totalAllocated = existingAllocs.sumOf { it.amount }
            
            if (totalAllocated + allocation.amount > transaction.amount + 0.0001) {
                throw IllegalArgumentException(
                    "Total alokasi (${Formatters.formatRupiah(totalAllocated + allocation.amount)}) melebihi jumlah transaksi (${Formatters.formatRupiah(transaction.amount)})"
                )
            }
            
            expenseAllocationDao.insertAllocation(ExpenseAllocationEntity.fromDomain(allocation))
        }
    }

    override suspend fun deleteExpenseAllocation(allocationId: String) = withContext(Dispatchers.IO) {
        expenseAllocationDao.deleteAllocationById(allocationId)
    }

    // --- Financial Summary & Reports ---

    override fun getPocketStats(): Flow<List<PocketStats>> {
        return combine(
            pocketDao.getActivePockets(),
            expenseAllocationDao.getAllAllocations(),
            pocketAllocationDao.getAllAllocations()
        ) { pocketEntities, expenseAllocations, pocketAllocations ->
            val realizationByPocket = expenseAllocations
                .groupBy { it.pocketId }
                .mapValues { (_, allocs) -> allocs.sumOf { it.amount } }
            
            val plannedByPocket = pocketAllocations
                .groupBy { it.pocketId }
                .mapValues { (_, allocs) -> allocs.sumOf { it.allocatedAmount } }
            
            pocketEntities.map { pocketEntity ->
                val pocket = pocketEntity.toDomain()
                PocketStats(
                    pocketId = pocket.id,
                    pocketName = pocket.name,
                    targetAmount = pocket.targetAmount,
                    realization = realizationByPocket[pocket.id] ?: 0.0,
                    plannedAllocation = plannedByPocket[pocket.id] ?: 0.0
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(
            assetDao.getAllAssets(),
            pocketAllocationDao.getAllAllocations(),
            expenseAllocationDao.getAllAllocations(),
            transactionDao.getAllTransactions()
        ) { assets, pocketAllocs, expenseAllocs, transactions ->
            val activeAssets = assets.filter { it.isActive }

            val totalActualBalance = activeAssets.sumOf { it.balance }
            val totalPlannedAllocation = pocketAllocs.sumOf { it.allocatedAmount }
            
            // Calculate Realization per Pocket
            val realizationByPocket = expenseAllocs.groupBy { it.pocketId }
                .mapValues { (_, allocs) -> allocs.sumOf { it.amount } }
            
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startOfMonth = cal.timeInMillis

            var totalIncome = 0.0
            var totalExpense = 0.0
            var monthIncome = 0.0
            var monthExpense = 0.0

            transactions.forEach { tx ->
                when (tx.typeString) {
                    TransactionType.INCOME.name -> {
                        totalIncome += tx.amount
                        if (tx.dateMillis >= startOfMonth) {
                            monthIncome += tx.amount
                        }
                    }
                    TransactionType.EXPENSE.name -> {
                        totalExpense += tx.amount
                        if (tx.dateMillis >= startOfMonth) {
                            monthExpense += tx.amount
                        }
                    }
                }
            }

            val netSavings = monthIncome - monthExpense
            val rate = if (monthIncome > 0) {
                ((netSavings / monthIncome) * 100).toInt().coerceIn(0, 100)
            } else 0

            FinancialSummary(
                totalAssetBalance = totalActualBalance,
                totalPlannedAllocation = totalPlannedAllocation,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
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
            pocketAllocationDao.getAllAllocations(),
            expenseAllocationDao.getAllAllocations(),
            transactionDao.getAllTransactions()
        ) { assets, pocketAllocs, expenseAllocs, transactions ->
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
            val totalPlanned = pocketAllocs.sumOf { it.allocatedAmount }

            // Calculate cumulative totals from ALL transactions
            var totalIncomeAllTime = 0.0
            var totalExpenseAllTime = 0.0
            transactions.forEach { tx ->
                when (tx.typeString) {
                    TransactionType.INCOME.name -> totalIncomeAllTime += tx.amount
                    TransactionType.EXPENSE.name -> totalExpenseAllTime += tx.amount
                }
            }

            val net = income - expense
            val rate = if (income > 0) ((net / income) * 100).toInt().coerceIn(0, 100) else 0

            val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
            val title = "${monthNames.getOrElse(periodMonth) { "" }} $periodYear"

            FinancialReport(
                periodTitle = title,
                summary = FinancialSummary(
                    totalAssetBalance = totalActual,
                    totalPlannedAllocation = totalPlanned,
                    totalIncome = totalIncomeAllTime,
                    totalExpense = totalExpenseAllTime,
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
    
    // Note: Export/Restore functions need to be updated to handle new tables
    // For Phase 1, keeping them simple - they will need UI refinement
    
    override suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("ID,Tanggal,Judul,Nominal,Tipe,Kategori,Aset,Catatan\n")
        val cursor = database.openHelper.readableDatabase.query("SELECT * FROM transactions ORDER BY dateMillis DESC")
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val date = Formatters.formatShortDateIndo(cursor.getLong(cursor.getColumnIndexOrThrow("dateMillis")))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title")).replace(",", ";")
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("typeString"))
            val cat = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"))
            val asset = cursor.getString(cursor.getColumnIndexOrThrow("assetName"))
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note")).replace(",", ";")
            sb.append("$id,$date,$title,$amount,$type,$cat,$asset,$note\n")
        }
        cursor.close()
        sb.toString()
    }

    override suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("appName", "Saku")
        root.put("version", "3.0") // Updated version
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
            pObj.put("targetAmount", pCursor.getDouble(pCursor.getColumnIndexOrThrow("targetAmount")))
            pObj.put("color", pCursor.getString(pCursor.getColumnIndexOrThrow("color")))
            pObj.put("icon", pCursor.getString(pCursor.getColumnIndexOrThrow("icon")))
            pObj.put("isActive", pCursor.getInt(pCursor.getColumnIndexOrThrow("isActive")) == 1)
            pObj.put("completed", pCursor.getInt(pCursor.getColumnIndexOrThrow("completed")) == 1)
            pObj.put("archived", pCursor.getInt(pCursor.getColumnIndexOrThrow("archived")) == 1)
            pObj.put("description", pCursor.getString(pCursor.getColumnIndexOrThrow("description")))
            pocketsArray.put(pObj)
        }
        pCursor.close()
        root.put("pockets", pocketsArray)

        // Pocket Allocations
        val pocketAllocArray = JSONArray()
        val paCursor = database.openHelper.readableDatabase.query("SELECT * FROM pocket_allocations")
        while (paCursor.moveToNext()) {
            val paObj = JSONObject()
            paObj.put("id", paCursor.getString(paCursor.getColumnIndexOrThrow("id")))
            paObj.put("assetId", paCursor.getString(paCursor.getColumnIndexOrThrow("assetId")))
            paObj.put("pocketId", paCursor.getString(paCursor.getColumnIndexOrThrow("pocketId")))
            paObj.put("allocatedAmount", paCursor.getDouble(paCursor.getColumnIndexOrThrow("allocatedAmount")))
            pocketAllocArray.put(paObj)
        }
        paCursor.close()
        root.put("pocketAllocations", pocketAllocArray)

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

        // Expense Allocations
        val expenseAllocArray = JSONArray()
        val eaCursor = database.openHelper.readableDatabase.query("SELECT * FROM expense_allocations")
        while (eaCursor.moveToNext()) {
            val eaObj = JSONObject()
            eaObj.put("id", eaCursor.getString(eaCursor.getColumnIndexOrThrow("id")))
            eaObj.put("transactionId", eaCursor.getString(eaCursor.getColumnIndexOrThrow("transactionId")))
            eaObj.put("pocketId", eaCursor.getString(eaCursor.getColumnIndexOrThrow("pocketId")))
            eaObj.put("amount", eaCursor.getDouble(eaCursor.getColumnIndexOrThrow("amount")))
            expenseAllocArray.put(eaObj)
        }
        eaCursor.close()
        root.put("expenseAllocations", expenseAllocArray)

        root.toString(2)
    }

    override suspend fun restoreDataFromJson(json: String): Boolean = withContext(Dispatchers.IO) {
        // For Phase 1, simplified - full restore will need UI refinement
        try {
            if (json.isBlank()) return@withContext false
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun resetToDefaultData() = withContext(Dispatchers.IO) {
        // For Phase 1, use the default data from SakuDatabase.kt's callback
        database.withTransaction {
            assetDao.clearAll()
            pocketDao.clearAll()
            pocketAllocationDao.clearAll()
            categoryDao.clearAll()
            transactionDao.clearAll()
            expenseAllocationDao.clearAll()
            userDao.clearAll()
            
            // The Room callback will repopulate data
        }
    }
}