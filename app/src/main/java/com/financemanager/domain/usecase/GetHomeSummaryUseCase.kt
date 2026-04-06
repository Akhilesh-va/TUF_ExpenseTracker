package com.financemanager.domain.usecase

import com.financemanager.domain.model.HomeSummary
import com.financemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHomeSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<HomeSummary> = repository.observeHomeSummary()
}
