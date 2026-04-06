package com.financemanager.domain.usecase

import com.financemanager.domain.model.BalanceDashboard
import com.financemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBalanceDashboardUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<BalanceDashboard> = repository.observeBalanceDashboard()
}
