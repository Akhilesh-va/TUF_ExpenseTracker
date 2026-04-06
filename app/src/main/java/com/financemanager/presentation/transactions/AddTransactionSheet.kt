package com.financemanager.presentation.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financemanager.core.ui.components.AnimatedPillSwitcher
import com.financemanager.core.ui.components.AppButton
import com.financemanager.core.ui.components.CategoryChip
import com.financemanager.core.ui.theme.AppRadius
import com.financemanager.core.ui.utils.formatTransactionDate
import com.financemanager.domain.model.TransactionType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        AddTransactionContent(
            viewModel = viewModel,
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionContent(
    viewModel: AddTransactionViewModel,
    onDismiss: () -> Unit,
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val categories by viewModel.categoriesForType.collectAsStateWithLifecycle()
    val screen by viewModel.screenState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    LaunchedEffect(screen) {
        if (screen is AddTransactionUiState.Success) {
            delay(750)
            viewModel.consumeSuccess()
            onDismiss()
        }
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = form.dateMillis)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .imePadding()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Add transaction",
            style = MaterialTheme.typography.headlineSmall,
        )
        AnimatedPillSwitcher(
            options = listOf(
                TransactionType.INCOME to "Income",
                TransactionType.EXPENSE to "Expense",
            ),
            selected = form.transactionType,
            onSelect = { viewModel.setType(it) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.amountText,
            onValueChange = viewModel::setAmount,
            label = { Text("Amount") },
            isError = form.amountError != null,
            supportingText = form.amountError?.let { msg -> { Text(msg) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(AppRadius.Field),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(),
        )
        Text("Category", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { c ->
                CategoryChip(
                    category = c,
                    isSelected = form.selectedCategoryId == c.id,
                    onClick = { viewModel.selectCategory(c.id) },
                )
            }
        }
        if (form.categoryError != null) {
            Text(
                text = form.categoryError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        AppButton(
            text = formatTransactionDate(form.dateMillis),
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = form.note,
            onValueChange = viewModel::setNote,
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.Field),
        )
        val showSuccess = screen is AddTransactionUiState.Success
        val scaleSuccess by animateFloatAsState(
            targetValue = if (showSuccess) 1f else 0f,
            animationSpec = tween(400),
            label = "success",
        )
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(tween(350), initialScale = 0.7f) + fadeIn(tween(350)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scaleSuccess
                        scaleY = scaleSuccess
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Saved",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        if (screen is AddTransactionUiState.Error) {
            Text(
                text = (screen as AddTransactionUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
            )
        }
        AppButton(
            text = "Save",
            onClick = { viewModel.save() },
            modifier = Modifier.fillMaxWidth(),
            isLoading = form.isSaving,
        )
        Spacer(Modifier.height(24.dp))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
