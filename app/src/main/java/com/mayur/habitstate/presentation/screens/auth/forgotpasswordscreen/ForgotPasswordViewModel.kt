package com.mayur.habitstate.presentation.screens.auth.forgotpasswordscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, errorMessage = null, successMessage = null) }
    }

    fun sendPasswordResetEmail() {
        val currentState = _state.value
        if (!emailRegex.matches(currentState.email.trim())) {
            _state.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            _state.update {
                it.copy(errorMessage = "Firebase is not initialized. Please add google-services.json to the app module.")
            }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                firebaseAuth.sendPasswordResetEmail(currentState.email.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Password reset instructions have been sent to your email."
                                )
                            }
                        } else {
                            val msg = task.exception?.localizedMessage ?: "Failed to send reset email. Please try again."
                            _state.update { it.copy(isLoading = false, errorMessage = msg) }
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "An unexpected error occurred."
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.value = ForgotPasswordState()
    }
}
