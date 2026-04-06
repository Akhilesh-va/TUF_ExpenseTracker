package com.financemanager.domain.usecase

import com.financemanager.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetOnboardingCompleteUseCase @Inject constructor(
    private val preferences: UserPreferencesRepository,
) {
    suspend operator fun invoke(done: Boolean) {
        preferences.setOnboardingComplete(done)
    }
}
