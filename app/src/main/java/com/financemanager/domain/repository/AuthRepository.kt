package com.financemanager.domain.repository

import com.financemanager.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthUser(): Flow<AuthUser?>
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(fullName: String, email: String, password: String)
    suspend fun updateProfile(
        fullName: String,
        email: String,
        newPassword: String?,
    )
    suspend fun signOut()
}
