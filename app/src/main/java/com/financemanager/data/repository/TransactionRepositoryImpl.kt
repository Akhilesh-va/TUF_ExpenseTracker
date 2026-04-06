package com.financemanager.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.financemanager.core.ui.utils.currentMonthYear
import com.financemanager.core.ui.utils.monthRangeMillis
import com.financemanager.core.ui.utils.sixWeekRollingStartMillis
import com.financemanager.data.local.dao.CategoryDao
import com.financemanager.data.local.dao.TransactionDao
import com.financemanager.data.local.entity.TransactionEntity
import com.financemanager.data.local.entity.TransactionWithCategory
import com.financemanager.data.mapper.mapBalanceDashboard
import com.financemanager.data.mapper.buildMonthlySummary
import com.financemanager.data.mapper.toDomain
import com.financemanager.data.mapper.toEntity
import com.financemanager.domain.model.BalanceDashboard
import com.financemanager.domain.model.ExpenseCategoryRow
import com.financemanager.domain.model.ExpenseTrend
import com.financemanager.domain.model.HomeSummary
import com.financemanager.domain.model.MonthlySummary
import com.financemanager.domain.model.Transaction
import com.financemanager.domain.model.TransactionType
import com.financemanager.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) : TransactionRepository {

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeHomeSummary(): Flow<HomeSummary> =
        combine(
            transactionDao.observeTotalIncome(),
            transactionDao.observeTotalExpense(),
            transactionDao.observeIncomeCurrentCalendarMonth(),
            transactionDao.observeExpenseCurrentCalendarMonth(),
        ) { totalInc, totalExp, monthInc, monthExp ->
            HomeSummary(
                totalBalance = totalInc - totalExp,
                incomeThisMonth = monthInc,
                expenseThisMonth = monthExp,
            )
        }

    override fun observeTotalIncomeAllTime(): Flow<Double> =
        transactionDao.observeTotalIncome()

    override fun observeTotalExpenseAllTime(): Flow<Double> =
        transactionDao.observeTotalExpense()

    override fun getTransactionsPaged(
        typeFilter: TransactionType?,
        categoryId: String?,
    ): Flow<PagingData<Transaction>> {
        val typeStr = typeFilter?.name
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 10,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                transactionDao.pagingSource(typeStr, categoryId)
            },
        ).flow.map { paging ->
            paging.map { it.toDomain() }
        }
    }

    override suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        note: String?,
        date: Long,
    ) {
        categoryDao.getById(categoryId)
            ?: error("Unknown category")
        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type.name,
            categoryId = categoryId,
            note = note,
            date = date,
            createdAt = now,
        )
        transactionDao.insert(entity)
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteById(id)
    }

    override suspend fun restoreTransaction(transaction: Transaction) {
        transactionDao.insert(transaction.toEntity())
    }

    override fun getMonthlySummary(year: Int, month: Int): Flow<MonthlySummary> {
        val (start, end) = monthRangeMillis(year, month)
        return transactionDao.observeBetween(start, end).map { rows ->
            buildMonthlySummary(year, month, rows)
        }
    }

    override suspend fun clearAllTransactions() {
        transactionDao.deleteAll()
    }

    override fun observeExpenseCategoryComparison(
        currentStart: Long,
        currentEnd: Long,
        previousStart: Long,
        previousEnd: Long,
    ): Flow<List<ExpenseCategoryRow>> =
        combine(
            transactionDao.observeBetween(currentStart, currentEnd),
            transactionDao.observeBetween(previousStart, previousEnd),
        ) { current, previous ->
            buildExpenseCategoryRows(current, previous)
        }

    override fun observeBalanceDashboard(): Flow<BalanceDashboard> {
        val (y, m) = currentMonthYear()
        val (ms, me) = monthRangeMillis(y, m)
        val fromWeek = sixWeekRollingStartMillis()
        return combine(
            transactionDao.observeBetween(ms, me),
            transactionDao.observeExpensesFrom(fromWeek),
        ) { monthRows, weekExpenseRows ->
            mapBalanceDashboard(monthRows, weekExpenseRows, y, m)
        }
    }
}

private fun buildExpenseCategoryRows(
    current: List<TransactionWithCategory>,
    previous: List<TransactionWithCategory>,
): List<ExpenseCategoryRow> {
    fun expenseSumsByCategory(rows: List<TransactionWithCategory>): Map<String, Pair<Double, TransactionWithCategory>> =
        rows
            .asSequence()
            .filter { it.transaction.type == TransactionType.EXPENSE.name }
            .groupBy { it.transaction.categoryId }
            .mapValues { (_, list) ->
                list.sumOf { it.transaction.amount } to list.first()
            }

    val cur = expenseSumsByCategory(current)
    val prev = expenseSumsByCategory(previous)
    return cur.map { (_, pair) ->
        val (amount, row) = pair
        val cat = row.category.toDomain()
        val prevAmount = prev[row.transaction.categoryId]?.first ?: 0.0
        val trend = when {
            prevAmount == 0.0 && amount > 0.0 -> ExpenseTrend.NEW_IN_PERIOD
            amount < prevAmount -> ExpenseTrend.LESS_THAN_PREVIOUS
            amount > prevAmount -> ExpenseTrend.MORE_THAN_PREVIOUS
            else -> ExpenseTrend.SAME_AS_PREVIOUS
        }
        ExpenseCategoryRow(cat, amount, trend)
    }.sortedByDescending { it.amount }
}
