package com.financemanager.domain.repository

import androidx.paging.PagingData
import com.financemanager.domain.model.Category
import com.financemanager.domain.model.BalanceDashboard
import com.financemanager.domain.model.ExpenseCategoryRow
import com.financemanager.domain.model.HomeSummary
import com.financemanager.domain.model.MonthlySummary
import com.financemanager.domain.model.Transaction
import com.financemanager.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    fun observeHomeSummary(): Flow<HomeSummary>
    fun getTransactionsPaged(
        typeFilter: TransactionType?,
        categoryId: String?,
    ): Flow<PagingData<Transaction>>

    suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        note: String?,
        date: Long,
    )

    suspend fun deleteTransaction(id: String)
    suspend fun restoreTransaction(transaction: Transaction)
    fun getMonthlySummary(year: Int, month: Int): Flow<MonthlySummary>
    suspend fun clearAllTransactions()

    fun observeExpenseCategoryComparison(
        currentStart: Long,
        currentEnd: Long,
        previousStart: Long,
        previousEnd: Long,
    ): Flow<List<ExpenseCategoryRow>>

    fun observeBalanceDashboard(): Flow<BalanceDashboard>

    /** All-time totals for profile preview. */
    fun observeTotalIncomeAllTime(): Flow<Double>
    fun observeTotalExpenseAllTime(): Flow<Double>
}

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeCategoriesByType(type: TransactionType): Flow<List<Category>>
}

interface UserPreferencesRepository {
    suspend fun setThemeMode(mode: String)
    fun observeThemeMode(): Flow<String>
    suspend fun setOnboardingComplete(done: Boolean)
    fun observeOnboardingComplete(): Flow<Boolean>
    suspend fun setProfileDisplayName(name: String)
    fun observeProfileDisplayName(): Flow<String>
    suspend fun setProfileEmail(email: String)
    fun observeProfileEmail(): Flow<String>
}
