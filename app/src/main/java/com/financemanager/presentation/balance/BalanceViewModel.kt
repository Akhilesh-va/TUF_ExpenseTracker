package com.financemanager.presentation.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.domain.model.BalanceDashboard
import com.financemanager.domain.usecase.ObserveBalanceDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface BalanceUiState {
    data object Loading : BalanceUiState
    data class Success(val data: BalanceDashboard) : BalanceUiState
    data class Error(val message: String) : BalanceUiState
}

@HiltViewModel
class BalanceViewModel @Inject constructor(
    observeBalanceDashboardUseCase: ObserveBalanceDashboardUseCase,
) : ViewModel() {

    val uiState: StateFlow<BalanceUiState> = observeBalanceDashboardUseCase()
        .map { BalanceUiState.Success(it) as BalanceUiState }
        .catch { emit(BalanceUiState.Error(it.message ?: "Could not load balances")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BalanceUiState.Loading,
        )
}
