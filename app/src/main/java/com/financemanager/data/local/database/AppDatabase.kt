package com.financemanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.financemanager.data.local.dao.CategoryDao
import com.financemanager.data.local.dao.TransactionDao
import com.financemanager.data.local.entity.CategoryEntity
import com.financemanager.data.local.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
