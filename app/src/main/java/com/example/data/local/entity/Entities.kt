package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Asset
import com.example.domain.model.AssetType
import com.example.domain.model.Category
import com.example.domain.model.ExpenseAllocation
import com.example.domain.model.Pocket
import com.example.domain.model.PocketAllocation
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.User

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val typeString: String,
    val balance: Double,
    val currency: String = "IDR",
    val isActive: Boolean = true
) {
    fun toDomain(): Asset = Asset(
        id = id,
        name = name,
        type = try { AssetType.valueOf(typeString) } catch (e: Exception) { AssetType.BANK },
        balance = balance
    )

    companion object {
        fun fromDomain(asset: Asset): AssetEntity = AssetEntity(
            id = asset.id,
            name = asset.name,
            typeString = asset.type.name,
            balance = asset.balance,
            currency = "IDR",
            isActive = true
        )
    }
}

@Entity(tableName = "pockets")
data class PocketEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmount: Double,
    val color: String,
    val icon: String,
    val isActive: Boolean = true,
    val completed: Boolean = false,
    val archived: Boolean = false,
    val description: String = ""
) {
    fun toDomain(): Pocket = Pocket(
        id = id,
        name = name,
        targetAmount = targetAmount,
        color = color,
        icon = icon,
        isActive = isActive,
        completed = completed,
        archived = archived,
        description = description
    )

    companion object {
        fun fromDomain(pocket: Pocket): PocketEntity = PocketEntity(
            id = pocket.id,
            name = pocket.name,
            targetAmount = pocket.targetAmount,
            color = pocket.color,
            icon = pocket.icon,
            isActive = pocket.isActive,
            completed = pocket.completed,
            archived = pocket.archived,
            description = pocket.description
        )
    }
}

/**
 * Planned allocation relationship: Asset -> Pocket.
 * Represents planned funding intent. Not actual money.
 */
@Entity(
    tableName = "pocket_allocations",
    indices = [
        Index("assetId"),
        Index("pocketId"),
        Index(value = ["assetId", "pocketId"], unique = true)
    ]
)
data class PocketAllocationEntity(
    @PrimaryKey val id: String,
    val assetId: String,
    val pocketId: String,
    val allocatedAmount: Double
) {
    fun toDomain(): PocketAllocation = PocketAllocation(
        id = id,
        assetId = assetId,
        pocketId = pocketId,
        allocatedAmount = allocatedAmount
    )

    companion object {
        fun fromDomain(pa: PocketAllocation): PocketAllocationEntity = PocketAllocationEntity(
            id = pa.id,
            assetId = pa.assetId,
            pocketId = pa.pocketId,
            allocatedAmount = pa.allocatedAmount
        )
    }
}

/**
 * Expense allocation relationship: Expense -> Pocket.
 * Source of Pocket Realization.
 */
@Entity(
    tableName = "expense_allocations",
    indices = [
        Index("transactionId"),
        Index("pocketId")
    ]
)
data class ExpenseAllocationEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val pocketId: String,
    val amount: Double
) {
    fun toDomain(): ExpenseAllocation = ExpenseAllocation(
        id = id,
        transactionId = transactionId,
        pocketId = pocketId,
        amount = amount
    )

    companion object {
        fun fromDomain(ea: ExpenseAllocation): ExpenseAllocationEntity = ExpenseAllocationEntity(
            id = ea.id,
            transactionId = ea.transactionId,
            pocketId = ea.pocketId,
            amount = ea.amount
        )
    }
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val typeString: String,
    val iconName: String,
    val colorHex: String
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        type = try { TransactionType.valueOf(typeString) } catch (e: Exception) { TransactionType.EXPENSE },
        icon = iconName,
        colorHex = colorHex
    )

    companion object {
        fun fromDomain(cat: Category): CategoryEntity = CategoryEntity(
            id = cat.id,
            name = cat.name,
            typeString = cat.type.name,
            iconName = cat.icon,
            colorHex = cat.colorHex
        )
    }
}

@Entity(
    tableName = "transactions",
    indices = [
        Index("assetId"),
        Index("dateMillis")
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val typeString: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val assetId: String,
    val assetName: String,
    val targetAssetId: String?,
    val targetAssetName: String?,
    val dateMillis: Long,
    val note: String,
    val receiptImageUrl: String? = null,
    // Deprecated legacy fields retained for safe migration read/write
    @Deprecated("Legacy field - use ExpenseAllocation for pocket assignment. Remove in Phase 4.3.")
    val pocketId: String? = null,
    @Deprecated("Legacy field - use ExpenseAllocation for pocket assignment. Remove in Phase 4.3.")
    val pocketName: String? = null,
    @Deprecated("Pocket-to-Pocket transfer removed. Use Asset-to-Asset transfer.")
    val targetPocketId: String? = null,
    @Deprecated("Pocket-to-Pocket transfer removed. Use Asset-to-Asset transfer.")
    val targetPocketName: String? = null
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        title = title,
        amount = amount,
        type = try { TransactionType.valueOf(typeString) } catch (e: Exception) { TransactionType.EXPENSE },
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        assetId = assetId,
        assetName = assetName,
        targetAssetId = targetAssetId,
        targetAssetName = targetAssetName,
        dateMillis = dateMillis,
        note = note,
        pocketId = pocketId,
        pocketName = pocketName,
        targetPocketId = targetPocketId,
        targetPocketName = targetPocketName
    )

    companion object {
        fun fromDomain(t: Transaction): TransactionEntity = TransactionEntity(
            id = t.id,
            title = t.title,
            amount = t.amount,
            typeString = t.type.name,
            categoryId = t.categoryId,
            categoryName = t.categoryName,
            categoryIcon = t.categoryIcon,
            assetId = t.assetId,
            assetName = t.assetName,
            targetAssetId = t.targetAssetId,
            targetAssetName = t.targetAssetName,
            dateMillis = t.dateMillis,
            note = t.note,
            receiptImageUrl = null,
            pocketId = t.pocketId,
            pocketName = t.pocketName,
            targetPocketId = t.targetPocketId,
            targetPocketName = t.targetPocketName
        )
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val iconName: String = "person",
    val colorHex: String = "#153E35",
    val isBiometricEnabled: Boolean = false,
    val currencyCode: String = "IDR",
    val joinedDateMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        iconName = iconName,
        colorHex = colorHex,
        isBiometricEnabled = isBiometricEnabled
    )

    companion object {
        fun fromDomain(u: User): UserEntity = UserEntity(
            id = u.id,
            name = u.name,
            email = u.email,
            iconName = u.iconName,
            colorHex = u.colorHex,
            isBiometricEnabled = u.isBiometricEnabled,
            currencyCode = "IDR",
            joinedDateMillis = System.currentTimeMillis()
        )
    }
}
