package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Category
import com.example.domain.model.Pocket
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.User

@Entity(tableName = "pockets")
data class PocketEntity(
    @PrimaryKey val id: String,
    val name: String,
    val balance: Double,
    val targetAmount: Double,
    val iconName: String,
    val colorHex: String,
    val isMain: Boolean,
    val description: String
) {
    fun toDomain(): Pocket = Pocket(
        id = id,
        name = name,
        balance = balance,
        targetAmount = targetAmount,
        iconName = iconName,
        colorHex = colorHex,
        isMain = isMain,
        description = description
    )

    companion object {
        fun fromDomain(pocket: Pocket): PocketEntity = PocketEntity(
            id = pocket.id,
            name = pocket.name,
            balance = pocket.balance,
            targetAmount = pocket.targetAmount,
            iconName = pocket.iconName,
            colorHex = pocket.colorHex,
            isMain = pocket.isMain,
            description = pocket.description
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
    val pocketId: String,
    val pocketName: String,
    val targetPocketId: String?,
    val targetPocketName: String?,
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
        pocketId = pocketId,
        pocketName = pocketName,
        targetPocketId = targetPocketId,
        targetPocketName = targetPocketName,
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
            pocketId = t.pocketId,
            pocketName = t.pocketName,
            targetPocketId = t.targetPocketId,
            targetPocketName = t.targetPocketName,
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
