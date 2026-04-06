package com.financemanager.di

import com.financemanager.data.preferences.UserPreferencesDataStore
import com.financemanager.data.repository.CategoryRepositoryImpl
import com.financemanager.data.repository.FirebaseAuthRepositoryImpl
import com.financemanager.data.repository.TransactionRepositoryImpl
import com.financemanager.domain.repository.AuthRepository
import com.financemanager.domain.repository.CategoryRepository
import com.financemanager.domain.repository.TransactionRepository
import com.financemanager.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl,
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl,
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferences(
        impl: UserPreferencesDataStore,
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepositoryImpl,
    ): AuthRepository
}
