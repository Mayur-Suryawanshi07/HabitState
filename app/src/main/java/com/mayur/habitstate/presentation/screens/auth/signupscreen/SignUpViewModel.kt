package com.mayur.habitstate.presentation.screens.auth.signupscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    fun onFullNameChange(fullName: String) {
        _state.update { it.copy(fullName = fullName, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun signup() {
        val currentState = _state.value
        if (currentState.fullName.trim().isEmpty()) {
            _state.update { it.copy(errorMessage = "Please enter your full name.") }
            return
        }
        if (!emailRegex.matches(currentState.email.trim())) {
            _state.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (currentState.password.length < 6) {
            _state.update { it.copy(errorMessage = "Password must be at least 6 characters long.") }
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(errorMessage = "Passwords do not match.") }
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
                firebaseAuth.createUserWithEmailAndPassword(currentState.email.trim(), currentState.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(currentState.fullName.trim())
                                .build()
                            user?.updateProfile(profileUpdates)

                            _state.update { it.copy(isLoading = false, isSuccess = true) }
                        } else {
                            val msg = task.exception?.localizedMessage ?: "Account creation failed. Please try again."
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
        _state.value = SignUpState()
    }
}
