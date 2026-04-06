package com.financemanager.domain.usecase

import com.financemanager.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(
    private val preferences: UserPreferencesRepository,
) {
    suspend operator fun invoke(mode: String) {
        preferences.setThemeMode(mode)
    }
}
