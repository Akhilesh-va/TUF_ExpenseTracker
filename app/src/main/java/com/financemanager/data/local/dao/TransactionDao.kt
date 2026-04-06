package com.financemanager.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financemanager.data.local.entity.TransactionEntity
import com.financemanager.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        WHERE (:typeFilter IS NULL OR type = :typeFilter)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY date DESC, createdAt DESC
        """,
    )
    fun pagingSource(typeFilter: String?, categoryId: String?): PagingSource<Int, TransactionWithCategory>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions 
        ORDER BY date DESC, createdAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<TransactionWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'INCOME'
        """,
    )
    fun observeTotalIncome(): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE'
        """,
    )
    fun observeTotalExpense(): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'INCOME' AND date BETWEEN :start AND :end
        """,
    )
    fun observeIncomeBetween(start: Long, end: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end
        """,
    )
    fun observeExpenseBetween(start: Long, end: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'INCOME'
        AND strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') =
            strftime('%Y-%m', 'now', 'localtime')
        """,
    )
    fun observeIncomeCurrentCalendarMonth(): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = 'EXPENSE'
        AND strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') =
            strftime('%Y-%m', 'now', 'localtime')
        """,
    )
    fun observeExpenseCurrentCalendarMonth(): Flow<Double>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :start AND :end
        ORDER BY date DESC
        """,
    )
    fun observeBetween(start: Long, end: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE type = 'EXPENSE' AND date >= :startMillis
        ORDER BY date ASC
        """,
    )
    fun observeExpensesFrom(startMillis: Long): Flow<List<TransactionWithCategory>>
}
