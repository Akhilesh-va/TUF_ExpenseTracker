package com.financemanager.domain.usecase

import com.financemanager.domain.model.Transaction
import com.financemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(limit: Int = 5): Flow<List<Transaction>> =
        repository.getRecentTransactions(limit)
}
