package com.mayur.habitstate.presentation.screens.main.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.mayur.habitstate.data.theme.ThemeMode
import com.mayur.habitstate.data.theme.ThemePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val themeRepo = ThemePreferencesRepository.getInstance(application)

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfileData()
        observeThemeMode()
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
                userEmail = email,
                themeMode = themeRepo.getThemeMode()
            )
        }
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            themeRepo.themeModeState.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        themeRepo.setThemeMode(mode)
    }

    fun toggleDarkMode(enableDark: Boolean) {
        themeRepo.toggleDarkMode(enableDark)
    }

    fun openEditNameDialog() {
        _state.update { it.copy(isEditNameDialogOpen = true) }
    }

    fun closeEditNameDialog() {
        _state.update { it.copy(isEditNameDialogOpen = false) }
    }

    fun updateName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSavingName = true) }
            val currentUser = auth?.currentUser
            if (currentUser != null) {
                try {
                    val request = UserProfileChangeRequest.Builder()
                        .setDisplayName(trimmed)
                        .build()
                    currentUser.updateProfile(request)
                } catch (_: Exception) { }
            }
            _state.update {
                it.copy(
                    userName = trimmed,
                    isSavingName = false,
                    isEditNameDialogOpen = false
                )
            }
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
