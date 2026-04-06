package com.financemanager.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.core.ui.utils.currentMonthYear
import com.financemanager.domain.model.MonthlySummary
import com.financemanager.domain.usecase.GetMonthlySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar
import java.util.Locale

data class MonthSelection(val year: Int, val monthIndex: Int)

sealed interface SummaryUiState {
    data object Loading : SummaryUiState
    data class Success(val summary: MonthlySummary) : SummaryUiState
    data class Error(val message: String) : SummaryUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
) : ViewModel() {

    private val _month = MutableStateFlow(run {
        val (y, m) = currentMonthYear()
        MonthSelection(y, m)
    })
    val month: StateFlow<MonthSelection> = _month.asStateFlow()

    val uiState: StateFlow<SummaryUiState> = _month
        .flatMapLatest { sel ->
            getMonthlySummaryUseCase(sel.year, sel.monthIndex).map { data ->
                SummaryUiState.Success(data) as SummaryUiState
            }
        }
        .catch {
            emit(SummaryUiState.Error(it.message ?: "Failed to load summary"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SummaryUiState.Loading,
        )

    fun previousMonth() {
        _month.value = _month.value.shiftByMonths(-1)
    }

    fun nextMonth() {
        _month.value = _month.value.shiftByMonths(1)
    }

    private fun MonthSelection.shiftByMonths(delta: Int): MonthSelection {
        val cal = Calendar.getInstance(Locale.getDefault()).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        return MonthSelection(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }
}
