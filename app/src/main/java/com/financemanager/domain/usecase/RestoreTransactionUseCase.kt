package com.financemanager.domain.usecase

import com.financemanager.domain.model.Transaction
import com.financemanager.domain.repository.TransactionRepository
import javax.inject.Inject

class RestoreTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.restoreTransaction(transaction)
    }
}
