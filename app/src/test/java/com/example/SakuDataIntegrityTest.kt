package com.example

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SakuDatabase
import com.example.data.repository.SakuRepositoryImpl
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SakuDataIntegrityTest {

    private lateinit var database: SakuDatabase
    private lateinit var repository: SakuRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SakuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SakuRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testExpenseInsertionDecreasesPocketBalance() = runBlocking {
        val pocket = Pocket(id = "p_main", name = "Utama", balance = 1000000.0)
        repository.insertPocket(pocket)

        val tx = Transaction(
            id = "tx_expense_1",
            title = "Belanja",
            amount = 200000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_shop",
            categoryName = "Belanja",
            categoryIcon = "shopping_bag",
            pocketId = "p_main",
            pocketName = "Utama",
            targetPocketId = null,
            targetPocketName = null,
            dateMillis = System.currentTimeMillis(),
            note = "Belanja mingguan"
        )
        repository.insertTransaction(tx)

        val updatedPocket = repository.getPocketById("p_main")
        assertNotNull(updatedPocket)
        assertEquals(800000.0, updatedPocket!!.balance, 0.001)

        val savedTx = repository.getTransactionById("tx_expense_1")
        assertNotNull(savedTx)
        assertEquals(200000.0, savedTx!!.amount, 0.001)
    }

    @Test
    fun testIncomeInsertionIncreasesPocketBalance() = runBlocking {
        val pocket = Pocket(id = "p_income", name = "Utama", balance = 800000.0)
        repository.insertPocket(pocket)

        val tx = Transaction(
            id = "tx_income_1",
            title = "Gaji",
            amount = 500000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_salary",
            categoryName = "Gaji",
            categoryIcon = "payments",
            pocketId = "p_income",
            pocketName = "Utama",
            targetPocketId = null,
            targetPocketName = null,
            dateMillis = System.currentTimeMillis(),
            note = "Bonus"
        )
        repository.insertTransaction(tx)

        val updatedPocket = repository.getPocketById("p_income")
        assertNotNull(updatedPocket)
        assertEquals(1300000.0, updatedPocket!!.balance, 0.001)
    }

    @Test
    fun testDeletingExpenseRestoresBalance() = runBlocking {
        val pocket = Pocket(id = "p_exp_del", name = "Utama", balance = 1000000.0)
        repository.insertPocket(pocket)

        val tx = Transaction(
            id = "tx_exp_to_del",
            title = "Makan",
            amount = 150000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_food",
            categoryName = "Makan",
            categoryIcon = "restaurant",
            pocketId = "p_exp_del",
            pocketName = "Utama",
            targetPocketId = null,
            targetPocketName = null,
            dateMillis = System.currentTimeMillis(),
            note = "Restoran"
        )
        repository.insertTransaction(tx)
        assertEquals(850000.0, repository.getPocketById("p_exp_del")!!.balance, 0.001)

        repository.deleteTransaction("tx_exp_to_del")

        // Balance refunded back to 1.000.000
        val refundedPocket = repository.getPocketById("p_exp_del")
        assertNotNull(refundedPocket)
        assertEquals(1000000.0, refundedPocket!!.balance, 0.001)

        // Transaction is deleted
        assertNull(repository.getTransactionById("tx_exp_to_del"))
    }

    @Test
    fun testDeletingIncomeRestoresBalance() = runBlocking {
        val pocket = Pocket(id = "p_inc_del", name = "Utama", balance = 500000.0)
        repository.insertPocket(pocket)

        val tx = Transaction(
            id = "tx_inc_to_del",
            title = "Dividen",
            amount = 250000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_invest",
            categoryName = "Investasi",
            categoryIcon = "trending_up",
            pocketId = "p_inc_del",
            pocketName = "Utama",
            targetPocketId = null,
            targetPocketName = null,
            dateMillis = System.currentTimeMillis(),
            note = "Dividen saham"
        )
        repository.insertTransaction(tx)
        assertEquals(750000.0, repository.getPocketById("p_inc_del")!!.balance, 0.001)

        repository.deleteTransaction("tx_inc_to_del")

        // Balance subtracted back to 500.000
        val restoredPocket = repository.getPocketById("p_inc_del")
        assertNotNull(restoredPocket)
        assertEquals(500000.0, restoredPocket!!.balance, 0.001)

        // Transaction is deleted
        assertNull(repository.getTransactionById("tx_inc_to_del"))
    }

    @Test
    fun testTransferDecreasesSourceAndIncreasesDestination() = runBlocking {
        val source = Pocket(id = "p_src", name = "Kantong Asal", balance = 1000000.0)
        val target = Pocket(id = "p_dest", name = "Kantong Tujuan", balance = 400000.0)
        repository.insertPocket(source)
        repository.insertPocket(target)

        val transferTx = Transaction(
            id = "tx_transfer_1",
            title = "Transfer Antar Kantong",
            amount = 300000.0,
            type = TransactionType.TRANSFER,
            categoryId = "cat_transfer",
            categoryName = "Transfer Antar Kantong",
            categoryIcon = "swap_horiz",
            pocketId = "p_src",
            pocketName = "Kantong Asal",
            targetPocketId = "p_dest",
            targetPocketName = "Kantong Tujuan",
            dateMillis = System.currentTimeMillis(),
            note = "Pindah tabungan"
        )
        repository.insertTransaction(transferTx)

        val updatedSource = repository.getPocketById("p_src")
        val updatedTarget = repository.getPocketById("p_dest")
        assertNotNull(updatedSource)
        assertNotNull(updatedTarget)
        assertEquals(700000.0, updatedSource!!.balance, 0.001)
        assertEquals(700000.0, updatedTarget!!.balance, 0.001)
    }

    @Test
    fun testDeletingTransferReversesBothBalances() = runBlocking {
        val source = Pocket(id = "p_src_del", name = "Kantong Asal", balance = 1000000.0)
        val target = Pocket(id = "p_dest_del", name = "Kantong Tujuan", balance = 500000.0)
        repository.insertPocket(source)
        repository.insertPocket(target)

        val transferTx = Transaction(
            id = "tx_transfer_to_del",
            title = "Transfer Antar Kantong",
            amount = 350000.0,
            type = TransactionType.TRANSFER,
            categoryId = "cat_transfer",
            categoryName = "Transfer",
            categoryIcon = "swap_horiz",
            pocketId = "p_src_del",
            pocketName = "Kantong Asal",
            targetPocketId = "p_dest_del",
            targetPocketName = "Kantong Tujuan",
            dateMillis = System.currentTimeMillis(),
            note = "Pindah dana"
        )
        repository.insertTransaction(transferTx)
        assertEquals(650000.0, repository.getPocketById("p_src_del")!!.balance, 0.001)
        assertEquals(850000.0, repository.getPocketById("p_dest_del")!!.balance, 0.001)

        repository.deleteTransaction("tx_transfer_to_del")

        val reversedSource = repository.getPocketById("p_src_del")
        val reversedTarget = repository.getPocketById("p_dest_del")
        assertNotNull(reversedSource)
        assertNotNull(reversedTarget)
        // Source refunded back to 1.000.000
        assertEquals(1000000.0, reversedSource!!.balance, 0.001)
        // Target subtracted back to 500.000
        assertEquals(500000.0, reversedTarget!!.balance, 0.001)
        // Transaction is deleted
        assertNull(repository.getTransactionById("tx_transfer_to_del"))
    }

    @Test
    fun testFailedAtomicOperationLeavesNoPartialUpdate() = runBlocking {
        val initialPocket = Pocket(id = "p_atomic", name = "Pocket Atomic", balance = 500000.0)
        repository.insertPocket(initialPocket)

        // Attempt an atomic operation that fails mid-way
        try {
            database.withTransaction {
                val p = database.pocketDao().getPocketById("p_atomic")!!
                database.pocketDao().updatePocket(p.copy(balance = 200000.0))
                throw IllegalStateException("Simulated mid-transaction failure")
            }
        } catch (e: Exception) {
            // Expected
        }

        // Verify balance was NOT partially updated
        val verifiedPocket = repository.getPocketById("p_atomic")
        assertNotNull(verifiedPocket)
        assertEquals(500000.0, verifiedPocket!!.balance, 0.001)

        // Also verify restoreDataFromJson with corrupt/invalid data does not mutate DB
        val invalidJson = """{"transactions": [{"title": "", "amount": -999}]}"""
        val result = repository.restoreDataFromJson(invalidJson)
        assertFalse(result)
        val pocketAfterInvalidRestore = repository.getPocketById("p_atomic")
        assertEquals(500000.0, pocketAfterInvalidRestore!!.balance, 0.001)
    }

    @Test
    fun testJsonBackupAndRestoreRecoversPocketsAndTransactions() = runBlocking {
        // Setup initial pockets & transactions
        val pocket1 = Pocket(
            id = "p_bk_1",
            name = "Kantong Operasional",
            balance = 2500000.0,
            targetAmount = 5000000.0,
            iconName = "wallet",
            colorHex = "#153E35",
            isMain = true,
            description = "Harian"
        )
        val pocket2 = Pocket(
            id = "p_bk_2",
            name = "Kantong Investasi",
            balance = 7500000.0,
            targetAmount = 10000000.0,
            iconName = "trending_up",
            colorHex = "#C89535",
            isMain = false,
            description = "Portofolio"
        )
        repository.insertPocket(pocket1)
        repository.insertPocket(pocket2)

        val tx1 = Transaction(
            id = "tx_bk_1",
            title = "Beli Buku",
            amount = 125000.0,
            type = TransactionType.EXPENSE,
            categoryId = "cat_edu",
            categoryName = "Pendidikan",
            categoryIcon = "menu_book",
            pocketId = "p_bk_1",
            pocketName = "Kantong Operasional",
            targetPocketId = null,
            targetPocketName = null,
            dateMillis = 1700000000000L,
            note = "Buku Kotlin"
        )
        val tx2 = Transaction(
            id = "tx_bk_2",
            title = "Dividen Masuk",
            amount = 450000.0,
            type = TransactionType.INCOME,
            categoryId = "cat_div",
            categoryName = "Investasi",
            categoryIcon = "trending_up",
            pocketId = "p_bk_2",
            pocketName = "Kantong Investasi",
            targetPocketId = null,
            targetPocketName = null,
            dateMillis = 1700000500000L,
            note = "Dividen Q3"
        )
        repository.insertTransaction(tx1)
        repository.insertTransaction(tx2)

        // Export JSON
        val exportedJson = repository.exportDataAsJson()
        assertTrue(exportedJson.contains("Kantong Operasional"))
        assertTrue(exportedJson.contains("Kantong Investasi"))
        assertTrue(exportedJson.contains("Beli Buku"))
        assertTrue(exportedJson.contains("Dividen Masuk"))

        // Clear all data from DB
        database.pocketDao().clearAll()
        database.transactionDao().clearAll()
        assertEquals(0, repository.getAllPockets().first().size)
        assertEquals(0, repository.getAllTransactions().first().size)

        // Restore from JSON
        val restoreSuccess = repository.restoreDataFromJson(exportedJson)
        assertTrue(restoreSuccess)

        // Verify both pockets are recovered
        val restoredPockets = repository.getAllPockets().first()
        assertEquals(2, restoredPockets.size)
        val restoredP1 = repository.getPocketById("p_bk_1")
        val restoredP2 = repository.getPocketById("p_bk_2")
        assertNotNull(restoredP1)
        assertNotNull(restoredP2)
        assertEquals("Kantong Operasional", restoredP1!!.name)
        assertEquals("Kantong Investasi", restoredP2!!.name)
        assertEquals(5000000.0, restoredP1.targetAmount, 0.001)

        // Verify both transactions are recovered
        val restoredTxs = repository.getAllTransactions().first()
        assertEquals(2, restoredTxs.size)
        val restoredTx1 = repository.getTransactionById("tx_bk_1")
        val restoredTx2 = repository.getTransactionById("tx_bk_2")
        assertNotNull(restoredTx1)
        assertNotNull(restoredTx2)
        assertEquals("Beli Buku", restoredTx1!!.title)
        assertEquals(125000.0, restoredTx1.amount, 0.001)
        assertEquals(TransactionType.EXPENSE, restoredTx1.type)
        assertEquals("p_bk_1", restoredTx1.pocketId)
        assertEquals("Kantong Operasional", restoredTx1.pocketName)

        assertEquals("Dividen Masuk", restoredTx2!!.title)
        assertEquals(450000.0, restoredTx2.amount, 0.001)
        assertEquals(TransactionType.INCOME, restoredTx2.type)
        assertEquals("p_bk_2", restoredTx2.pocketId)
        assertEquals("Kantong Investasi", restoredTx2.pocketName)
    }
}
