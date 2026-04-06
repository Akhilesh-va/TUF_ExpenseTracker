package com.financemanager.domain.usecase

import com.financemanager.domain.repository.TransactionRepository
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke() {
        transactionRepository.clearAllTransactions()
    }
}
