package com.financemanager.domain.usecase

import com.financemanager.domain.model.TransactionType
import com.financemanager.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        note: String?,
        date: Long,
    ) {
        require(amount > 0) { "Amount must be positive" }
        repository.addTransaction(amount, type, categoryId, note, date)
    }
}
