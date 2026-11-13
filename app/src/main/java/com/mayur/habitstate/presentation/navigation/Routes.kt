package com.mayur.habitstate.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {
    @Serializable
    data object Login : Routes

    @Serializable
    data object Signup : Routes

    @Serializable
    data object ForgotPassword : Routes

    @Serializable
    data object Home : Routes

    @Serializable
    data object Search : Routes

    @Serializable
    data object Profile : Routes

    @Serializable
    data object HabitScore : Routes

    @Serializable
    data class AddEditHabit(val habitId: Long = -1L) : Routes
}

@Serializable
sealed interface Graphs {
    @Serializable
    data object Auth : Graphs

    @Serializable
    data object Main : Graphs
}
