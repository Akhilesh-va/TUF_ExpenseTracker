package com.financemanager.domain.usecase

import androidx.paging.PagingData
import com.financemanager.domain.model.Transaction
import com.financemanager.domain.model.TransactionType
import com.financemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsPagedUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(
        typeFilter: TransactionType?,
        categoryId: String?,
    ): Flow<PagingData<Transaction>> =
        repository.getTransactionsPaged(typeFilter, categoryId)
}
