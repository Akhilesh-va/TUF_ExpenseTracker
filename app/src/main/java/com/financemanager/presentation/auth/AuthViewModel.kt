package com.financemanager.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private companion object {
        const val TAG = "AuthViewModel"
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) = _uiState.update { it.copy(mode = mode, message = null) }
    fun onFullNameChanged(v: String) = _uiState.update { it.copy(fullName = v, message = null) }
    fun onEmailChanged(v: String) = _uiState.update { it.copy(email = v, message = null) }
    fun onPasswordChanged(v: String) = _uiState.update { it.copy(password = v, message = null) }
    fun onConfirmPasswordChanged(v: String) = _uiState.update { it.copy(confirmPassword = v, message = null) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun submit() {
        val s = _uiState.value
        val email = s.email.trim()
        val password = s.password.trim()
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(message = "Email and password are required") }
            return
        }
        if (s.mode == AuthMode.SIGN_UP) {
            if (s.fullName.trim().isBlank()) {
                _uiState.update { it.copy(message = "Full name is required") }
                return
            }
            if (s.password != s.confirmPassword) {
                _uiState.update { it.copy(message = "Passwords do not match") }
                return
            }
            if (password.length < 6) {
                _uiState.update { it.copy(message = "Password should be at least 6 characters") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching {
                when (s.mode) {
                    AuthMode.SIGN_IN -> authRepository.signIn(email, password)
                    AuthMode.SIGN_UP -> authRepository.signUp(s.fullName, email, password)
                }
            }.onFailure { e ->
                val code = (e as? FirebaseAuthException)?.errorCode
                Log.e(
                    TAG,
                    "Auth submit failed mode=${s.mode} code=$code email=$email message=${e.message}",
                    e,
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = buildUserMessage(e),
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, message = null) }
            }
        }
    }

    private fun buildUserMessage(error: Throwable): String {
        val code = (error as? FirebaseAuthException)?.errorCode ?: return error.message ?: "Authentication failed"
        return when (code) {
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network issue while contacting Firebase. Check internet/VPN and try again."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many requests. Please wait a bit and retry."
            "ERROR_API_NOT_AVAILABLE" -> "Google Play services unavailable on this device/emulator."
            "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL",
            -> "Invalid email or password."
            "ERROR_USER_NOT_FOUND" -> "No account found for this email."
            else -> "Auth failed ($code): ${error.message ?: "Please try again."}"
        }
    }
}
