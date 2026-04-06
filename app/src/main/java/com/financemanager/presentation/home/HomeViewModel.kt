package com.financemanager.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.core.ui.utils.HomeExpensePeriod
import com.financemanager.core.ui.utils.rangesForHomeExpensePeriod
import com.financemanager.domain.model.ExpenseCategoryRow
import com.financemanager.domain.model.HomeSummary
import com.financemanager.domain.usecase.GetHomeSummaryUseCase
import com.financemanager.domain.usecase.ObserveExpenseCategoryComparisonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class HomeContent(
    val summary: HomeSummary,
    val expenseRows: List<ExpenseCategoryRow>,
)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val content: HomeContent) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    getHomeSummaryUseCase: GetHomeSummaryUseCase,
    observeExpenseCategoryComparisonUseCase: ObserveExpenseCategoryComparisonUseCase,
) : ViewModel() {

    private val _expensePeriod = MutableStateFlow(HomeExpensePeriod.WEEKLY)
    val expensePeriod: StateFlow<HomeExpensePeriod> = _expensePeriod.asStateFlow()

    fun setExpensePeriod(period: HomeExpensePeriod) {
        _expensePeriod.value = period
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getHomeSummaryUseCase(),
        _expensePeriod.flatMapLatest { period ->
            val (cur, prev) = rangesForHomeExpensePeriod(period)
            observeExpenseCategoryComparisonUseCase(
                cur.start,
                cur.end,
                prev.start,
                prev.end,
            )
        },
    ) { summary, expenseRows ->
        HomeUiState.Success(HomeContent(summary, expenseRows)) as HomeUiState
    }
        .catch { emit(HomeUiState.Error(it.message ?: "Unable to load home")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}
