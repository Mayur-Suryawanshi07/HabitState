package com.mayur.habitstate.presentation.screens.main.profile

import com.mayur.habitstate.data.theme.ThemeMode

data class ProfileState(
    val userName: String = "Habit Tracker",
    val userEmail: String = "No email registered",
    val streakDays: Int = 12,
    val activeHabitsCount: Int = 3,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isEditNameDialogOpen: Boolean = false,
    val isSavingName: Boolean = false
)
