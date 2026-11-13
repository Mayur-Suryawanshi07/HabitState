package com.mayur.habitstate.presentation.screens.main.score

import com.mayur.habitstate.domain.model.HabitScoreSummary

data class HabitScoreState(
    val summary: HabitScoreSummary = HabitScoreSummary(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
