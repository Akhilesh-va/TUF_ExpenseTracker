package com.financemanager.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class SessionState {
    CHECKING,
    AUTHENTICATED,
    UNAUTHENTICATED,
}

@HiltViewModel
class MainUiViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = authRepository.observeAuthUser()
        .map { if (it != null) SessionState.AUTHENTICATED else SessionState.UNAUTHENTICATED }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.CHECKING,
        )
}
