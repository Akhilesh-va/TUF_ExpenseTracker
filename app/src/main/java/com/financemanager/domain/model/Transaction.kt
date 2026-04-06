package com.financemanager.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val note: String?,
    val date: Long,
    val createdAt: Long,
)
