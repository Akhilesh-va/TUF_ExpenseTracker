package com.financemanager.data.repository

import com.financemanager.data.local.dao.CategoryDao
import com.financemanager.data.mapper.toDomain
import com.financemanager.domain.model.Category
import com.financemanager.domain.model.TransactionType
import com.financemanager.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeCategories().map { list -> list.map { it.toDomain() } }

    override fun observeCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.observeByType(type.name).map { list -> list.map { it.toDomain() } }
}
