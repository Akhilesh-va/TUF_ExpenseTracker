package com.financemanager.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financemanager.data.local.dao.CategoryDao
import com.financemanager.data.local.dao.TransactionDao
import com.financemanager.data.local.database.AppDatabase
import com.financemanager.data.local.database.CategorySeeds
import com.financemanager.data.local.entity.CategoryEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DB_NAME = "finance_manager.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .addCallback(CategorySeedCallback)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    private object CategorySeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CategorySeeds.All.forEach { c -> insertCategory(db, c) }
        }
    }
}

private fun insertCategory(db: SupportSQLiteDatabase, c: CategoryEntity) {
    val stmt = db.compileStatement(
        """
        INSERT OR REPLACE INTO categories (id, name, icon, color, type)
        VALUES (?, ?, ?, ?, ?)
        """.trimIndent(),
    )
    try {
        stmt.bindString(1, c.id)
        stmt.bindString(2, c.name)
        stmt.bindString(3, c.icon)
        stmt.bindLong(4, c.color)
        stmt.bindString(5, c.type)
        stmt.executeInsert()
    } finally {
        stmt.close()
    }
}
