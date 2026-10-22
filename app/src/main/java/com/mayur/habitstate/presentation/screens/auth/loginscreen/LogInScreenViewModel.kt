package com.mayur.habitstate.presentation.screens.auth.loginscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogInScreenViewModel : ViewModel() {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val _state = MutableStateFlow(LogInScreenState())
    val state: StateFlow<LogInScreenState> = _state.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _state.value
        if (!emailRegex.matches(currentState.email.trim())) {
            _state.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (currentState.password.isEmpty()) {
            _state.update { it.copy(errorMessage = "Password cannot be empty.") }
            return
        }

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            _state.update {
                it.copy(errorMessage = "Firebase is not initialized. Please add google-services.json to the app module.")
            }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(currentState.email.trim(), currentState.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _state.update { it.copy(isLoading = false, isSuccess = true) }
                        } else {
                            val msg = task.exception?.localizedMessage ?: "Login failed. Please try again."
                            _state.update { it.copy(isLoading = false, errorMessage = msg) }
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "An unexpected error occurred.")
                }
            }
        }
    }

    fun resetState() {
        _state.value = LogInScreenState()
    }
}
