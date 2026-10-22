package com.mayur.habitstate.presentation.screens.main.home

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        val currentUser = auth?.currentUser
        val name = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Habit Tracker"
        val email = currentUser?.email ?: ""

        _state.update {
            it.copy(
                userName = name,
                userEmail = email,
                habits = sampleHabits
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
