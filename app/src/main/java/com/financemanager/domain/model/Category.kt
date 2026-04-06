package com.financemanager.domain.model

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val color: Long,
    val type: TransactionType,
)
