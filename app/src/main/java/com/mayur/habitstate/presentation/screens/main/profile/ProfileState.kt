package com.mayur.habitstate.presentation.screens.main.profile

data class ProfileState(
    val userName: String = "Habit Tracker",
    val userEmail: String = "No email registered",
    val streakDays: Int = 12,
    val activeHabitsCount: Int = 3
)
