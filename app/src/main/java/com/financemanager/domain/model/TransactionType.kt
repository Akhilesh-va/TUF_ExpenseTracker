package com.financemanager.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    ;

    companion object {
        fun fromStorage(value: String): TransactionType =
            entries.firstOrNull { it.name == value } ?: EXPENSE
    }
}
