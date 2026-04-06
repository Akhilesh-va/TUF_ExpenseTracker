package com.financemanager.domain.usecase

import com.financemanager.domain.model.Category
import com.financemanager.domain.model.TransactionType
import com.financemanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = repository.observeCategories()

    fun byType(type: TransactionType): Flow<List<Category>> =
        repository.observeCategoriesByType(type)
}
