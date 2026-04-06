package com.financemanager.domain.usecase

import com.financemanager.domain.model.ExpenseCategoryRow
import com.financemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExpenseCategoryComparisonUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(
        currentStart: Long,
        currentEnd: Long,
        previousStart: Long,
        previousEnd: Long,
    ): Flow<List<ExpenseCategoryRow>> =
        repository.observeExpenseCategoryComparison(
            currentStart,
            currentEnd,
            previousStart,
            previousEnd,
        )
}
