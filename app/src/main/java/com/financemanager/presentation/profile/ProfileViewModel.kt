package com.financemanager.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.BuildConfig
import com.financemanager.domain.repository.AuthRepository
import com.financemanager.domain.repository.TransactionRepository
import com.financemanager.domain.repository.UserPreferencesRepository
import com.financemanager.domain.usecase.ClearAllDataUseCase
import com.financemanager.domain.usecase.ObserveThemeModeUseCase
import com.financemanager.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileContent(
    val displayName: String,
    val email: String,
    val totalSpendings: Double,
    val balance: Double,
    val isDarkTheme: Boolean,
    val appVersion: String,
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val content: ProfileContent) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface ProfileUiEvent {
    data class Snackbar(val message: String) : ProfileUiEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository,
    observeThemeModeUseCase: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase,
) : ViewModel() {

    private val _events = MutableSharedFlow<ProfileUiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private data class ProfileIdentity(
        val name: String,
        val email: String,
    )

    private val identityFlow = combine(
        authRepository.observeAuthUser(),
        userPreferencesRepository.observeProfileDisplayName(),
        userPreferencesRepository.observeProfileEmail(),
    ) { authUser, localName, localEmail ->
        ProfileIdentity(
            name = authUser?.displayName?.takeIf { it.isNotBlank() } ?: localName,
            email = authUser?.email?.takeIf { it.isNotBlank() } ?: localEmail,
        )
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        identityFlow,
        transactionRepository.observeTotalExpenseAllTime(),
        transactionRepository.observeTotalIncomeAllTime(),
        observeThemeModeUseCase(),
    ) { identity, totalExpense, totalIncome, themeMode ->
        ProfileUiState.Success(
            ProfileContent(
                displayName = identity.name,
                email = identity.email,
                totalSpendings = totalExpense,
                balance = totalIncome - totalExpense,
                isDarkTheme = themeMode == "DARK",
                appVersion = BuildConfig.VERSION_NAME,
            ),
        ) as ProfileUiState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState.Loading,
    )

    fun updateProfileDetails(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        viewModelScope.launch {
            val p = password.trim()
            val c = confirmPassword.trim()
            if (p.isNotEmpty() || c.isNotEmpty()) {
                if (p != c) {
                    _events.emit(ProfileUiEvent.Snackbar("Passwords do not match"))
                    return@launch
                }
                if (p.length < 6) {
                    _events.emit(ProfileUiEvent.Snackbar("Password must be at least 6 characters"))
                    return@launch
                }
            }
            runCatching {
                authRepository.updateProfile(
                    fullName = displayName,
                    email = email,
                    newPassword = p.ifBlank { null },
                )
                userPreferencesRepository.setProfileDisplayName(displayName)
                userPreferencesRepository.setProfileEmail(email)
                _events.emit(ProfileUiEvent.Snackbar("Details updated"))
            }.onFailure {
                _events.emit(ProfileUiEvent.Snackbar(it.message ?: "Could not update profile"))
            }
        }
    }

    fun onDarkModeChanged(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                setThemeModeUseCase(if (enabled) "DARK" else "LIGHT")
            }.onFailure {
                _events.emit(ProfileUiEvent.Snackbar(it.message ?: "Could not update theme"))
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            runCatching {
                clearAllDataUseCase()
                _events.emit(ProfileUiEvent.Snackbar("All transactions cleared"))
            }.onFailure {
                _events.emit(ProfileUiEvent.Snackbar(it.message ?: "Clear failed"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching {
                authRepository.signOut()
                _events.emit(ProfileUiEvent.Snackbar("Logged out"))
            }.onFailure {
                _events.emit(ProfileUiEvent.Snackbar(it.message ?: "Logout failed"))
            }
        }
    }
}
