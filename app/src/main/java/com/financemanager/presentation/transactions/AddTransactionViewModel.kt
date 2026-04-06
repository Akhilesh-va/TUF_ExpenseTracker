package com.financemanager.presentation.transactions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.domain.model.Category
import com.financemanager.domain.model.TransactionType
import com.financemanager.domain.usecase.AddTransactionUseCase
import com.financemanager.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddTransactionFormState(
    val amountText: String = "",
    val amountError: String? = null,
    val selectedCategoryId: String? = null,
    val categoryError: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val dateError: String? = null,
    val note: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val isSaving: Boolean = false,
)

sealed interface AddTransactionUiState {
    data object Editing : AddTransactionUiState
    data object Success : AddTransactionUiState
    data class Error(val message: String) : AddTransactionUiState
}

sealed interface AddTransactionUiEvent {
    data object NavigateBack : AddTransactionUiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {

    private val _form = MutableStateFlow(AddTransactionFormState())
    val form: StateFlow<AddTransactionFormState> = _form.asStateFlow()

    private val _screen = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Editing)
    val screenState: StateFlow<AddTransactionUiState> = _screen.asStateFlow()

    private val _events = MutableSharedFlow<AddTransactionUiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    val categoriesForType: StateFlow<List<Category>> = _form
        .map { it.transactionType }
        .distinctUntilChanged()
        .flatMapLatest { type -> getCategoriesUseCase.byType(type) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setType(type: TransactionType) {
        _form.value = _form.value.copy(
            transactionType = type,
            selectedCategoryId = null,
            categoryError = null,
        )
    }

    fun setAmount(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }
        _form.value = _form.value.copy(amountText = filtered, amountError = null)
    }

    fun selectCategory(id: String) {
        _form.value = _form.value.copy(selectedCategoryId = id, categoryError = null)
    }

    fun setDate(millis: Long) {
        _form.value = _form.value.copy(dateMillis = millis, dateError = null)
    }

    fun setNote(note: String) {
        _form.value = _form.value.copy(note = note)
    }

    fun save() {
        val current = _form.value
        val parsedAmount = current.amountText.toDoubleOrNull()
        var amountErr: String? = null
        var catErr: String? = null
        if (parsedAmount == null || parsedAmount <= 0) {
            amountErr = "Enter a valid amount"
        }
        if (current.selectedCategoryId.isNullOrBlank()) {
            catErr = "Choose a category"
        }
        if (amountErr != null || catErr != null) {
            _form.value = current.copy(amountError = amountErr, categoryError = catErr)
            return
        }
        viewModelScope.launch {
            _form.value = current.copy(isSaving = true, amountError = null, categoryError = null)
            runCatching {
                addTransactionUseCase(
                    amount = parsedAmount!!,
                    type = current.transactionType,
                    categoryId = current.selectedCategoryId!!,
                    note = current.note.ifBlank { null },
                    date = current.dateMillis,
                )
                _form.value = _form.value.copy(isSaving = false)
                _screen.value = AddTransactionUiState.Success
            }.onFailure { e ->
                _screen.value = AddTransactionUiState.Error(e.message ?: "Could not save")
                _form.value = _form.value.copy(isSaving = false)
            }
        }
    }

    fun consumeSuccess() {
        _screen.value = AddTransactionUiState.Editing
        _form.value = AddTransactionFormState()
    }
}
