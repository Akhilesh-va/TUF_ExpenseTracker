package com.financemanager.domain.usecase

import com.financemanager.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOnboardingCompleteUseCase @Inject constructor(
    private val preferences: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = preferences.observeOnboardingComplete()
}
