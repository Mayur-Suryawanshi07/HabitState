package com.mayur.habitstate.domain.model

data class HabitScoreItem(
    val habit: Habit,
    val completedCount: Int,
    val expectedCount: Int,
    val scorePercent: Float
)

data class HabitScoreSummary(
    val overallScore: Float = 0f,
    val items: List<HabitScoreItem> = emptyList()
)
