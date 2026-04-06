package com.financemanager.data.mapper

import com.financemanager.data.local.entity.TransactionEntity
import com.financemanager.data.local.entity.TransactionWithCategory
import com.financemanager.domain.model.Transaction
import com.financemanager.domain.model.TransactionType

fun TransactionWithCategory.toDomain(): Transaction = Transaction(
    id = transaction.id,
    amount = transaction.amount,
    type = TransactionType.fromStorage(transaction.type),
    category = category.toDomain(),
    note = transaction.note,
    date = transaction.date,
    createdAt = transaction.createdAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    categoryId = category.id,
    note = note,
    date = date,
    createdAt = createdAt,
)
