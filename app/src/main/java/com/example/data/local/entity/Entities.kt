package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Asset
import com.example.domain.model.AssetType
import com.example.domain.model.Category
import com.example.domain.model.Pocket
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
        balance = balance,
        currency = currency,
        isActive = isActive
    )

    companion object {
        fun fromDomain(asset: Asset): AssetEntity = AssetEntity(
            id = asset.id,
            name = asset.name,
            typeString = asset.type.name,
            balance = asset.balance,
            currency = asset.currency,
            isActive = asset.isActive
        )
    }
}

@Entity(tableName = "pockets")
data class PocketEntity(
    @PrimaryKey val id: String,
    val name: String,
    val assetId: String,
    val allocatedAmount: Double,
    val targetAmount: Double,
    val color: String,
    val icon: String,
    val isActive: Boolean = true
) {
    fun toDomain(assetName: String = ""): Pocket = Pocket(
        id = id,
        name = name,
        assetId = assetId,
        allocatedAmount = allocatedAmount,
        targetAmount = targetAmount,
        color = color,
        icon = icon,
        isActive = isActive,
        assetName = assetName
    )

    companion object {
        fun fromDomain(pocket: Pocket): PocketEntity = PocketEntity(
            id = pocket.id,
            name = pocket.name,
            assetId = pocket.assetId,
            allocatedAmount = pocket.allocatedAmount,
            targetAmount = pocket.targetAmount,
            color = pocket.color,
            icon = pocket.icon,
            isActive = pocket.isActive
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
        iconName = iconName,
        colorHex = colorHex
    )

    companion object {
        fun fromDomain(cat: Category): CategoryEntity = CategoryEntity(
            id = cat.id,
            name = cat.name,
            typeString = cat.type.name,
            iconName = cat.iconName,
            colorHex = cat.colorHex
        )
    }
}

@Entity(tableName = "transactions")
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
    val pocketId: String?,
    val pocketName: String?,
    val targetAssetId: String?,
    val targetAssetName: String?,
    val dateMillis: Long,
    val note: String,
    val receiptImageUrl: String?
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
        pocketId = pocketId,
        pocketName = pocketName,
        targetAssetId = targetAssetId,
        targetAssetName = targetAssetName,
        dateMillis = dateMillis,
        note = note,
        receiptImageUrl = receiptImageUrl
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
            pocketId = t.pocketId,
            pocketName = t.pocketName,
            targetAssetId = t.targetAssetId,
            targetAssetName = t.targetAssetName,
            dateMillis = t.dateMillis,
            note = t.note,
            receiptImageUrl = t.receiptImageUrl
        )
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val isBiometricEnabled: Boolean,
    val currencyCode: String,
    val joinedDateMillis: Long
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        isBiometricEnabled = isBiometricEnabled,
        currencyCode = currencyCode,
        joinedDateMillis = joinedDateMillis
    )

    companion object {
        fun fromDomain(u: User): UserEntity = UserEntity(
            id = u.id,
            name = u.name,
            email = u.email,
            isBiometricEnabled = u.isBiometricEnabled,
            currencyCode = u.currencyCode,
            joinedDateMillis = u.joinedDateMillis
        )
    }
}
