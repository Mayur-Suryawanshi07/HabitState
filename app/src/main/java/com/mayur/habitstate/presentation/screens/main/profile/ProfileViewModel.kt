package com.mayur.habitstate.presentation.screens.main.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        val currentUser = auth?.currentUser
        val name = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Habit Tracker"
        val email = currentUser?.email ?: "No email registered"

        _state.update {
            it.copy(
                userName = name,
                userEmail = email
            )
        }
    }

    fun logout(onSuccess: () -> Unit) {
        try {
            auth?.signOut()
        } catch (_: Exception) {
        }
        onSuccess()
    }
}
