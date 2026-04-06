package com.financemanager.domain.usecase

import com.financemanager.domain.model.MonthlySummary
import com.financemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonthlySummaryUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(year: Int, month: Int): Flow<MonthlySummary> =
        repository.getMonthlySummary(year, month)
}
