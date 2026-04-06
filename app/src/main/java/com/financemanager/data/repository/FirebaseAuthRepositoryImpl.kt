package com.financemanager.data.repository

import android.util.Log
import com.financemanager.domain.model.AuthUser
import com.financemanager.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
) : AuthRepository {
    private companion object {
        const val TAG = "FirebaseAuthRepo"
    }

    override fun observeAuthUser(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val current = auth.currentUser
            trySend(
                current?.let { user ->
                    AuthUser(
                        uid = user.uid,
                        displayName = user.displayName,
                        email = user.email,
                    )
                },
            )
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String) {
        val safeEmail = email.trim()
        runCatching {
            firebaseAuth.signInWithEmailAndPassword(safeEmail, password.trim()).await()
        }.onFailure { e ->
            Log.e(TAG, "signIn failed email=$safeEmail message=${e.message}", e)
            throw e
        }
    }

    override suspend fun signUp(fullName: String, email: String, password: String) {
        val safeEmail = email.trim()
        runCatching {
            firebaseAuth.createUserWithEmailAndPassword(safeEmail, password.trim()).await()
        }.onFailure { e ->
            Log.e(TAG, "signUp createUser failed email=$safeEmail message=${e.message}", e)
            throw e
        }
        val user = firebaseAuth.currentUser ?: error("Could not get created user")
        runCatching {
            user.updateProfileAwait(fullName.trim())
        }.onFailure { e ->
            Log.e(TAG, "signUp updateProfile failed uid=${user.uid} message=${e.message}", e)
            throw e
        }
        val payload = mapOf(
            "uid" to user.uid,
            "name" to fullName.trim(),
            "email" to (user.email ?: safeEmail),
            "createdAt" to System.currentTimeMillis(),
        )
        runCatching {
            firebaseDatabase.reference
                .child("users")
                .child(user.uid)
                .setValue(payload)
                .await()
        }.onFailure { e ->
            Log.e(TAG, "signUp writeUserNode failed uid=${user.uid} message=${e.message}", e)
            throw e
        }
    }

    override suspend fun updateProfile(
        fullName: String,
        email: String,
        newPassword: String?,
    ) {
        val user = firebaseAuth.currentUser ?: error("No authenticated user")
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()

        runCatching {
            user.updateProfileAwait(trimmedName)
        }.onFailure { e ->
            Log.e(TAG, "updateProfile displayName failed uid=${user.uid} message=${e.message}", e)
            throw e
        }
        if (trimmedEmail.isNotBlank() && user.email != trimmedEmail) {
            runCatching {
                user.updateEmail(trimmedEmail).await()
            }.onFailure { e ->
                Log.e(TAG, "updateProfile email failed uid=${user.uid} message=${e.message}", e)
                throw e
            }
        }
        val newPwd = newPassword?.trim().orEmpty()
        if (newPwd.isNotBlank()) {
            runCatching {
                user.updatePassword(newPwd).await()
            }.onFailure { e ->
                Log.e(TAG, "updateProfile password failed uid=${user.uid} message=${e.message}", e)
                throw e
            }
        }
        val payload = mapOf(
            "uid" to user.uid,
            "name" to trimmedName,
            "email" to (firebaseAuth.currentUser?.email ?: trimmedEmail),
            "updatedAt" to System.currentTimeMillis(),
        )
        runCatching {
            firebaseDatabase.reference
                .child("users")
                .child(user.uid)
                .updateChildren(payload)
                .await()
        }.onFailure { e ->
            Log.e(TAG, "updateProfile writeUserNode failed uid=${user.uid} message=${e.message}", e)
            throw e
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

private suspend fun com.google.firebase.auth.FirebaseUser.updateProfileAwait(displayName: String) {
    val req = com.google.firebase.auth.UserProfileChangeRequest.Builder()
        .setDisplayName(displayName)
        .build()
    suspendCancellableCoroutine { cont ->
        updateProfile(req)
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}
