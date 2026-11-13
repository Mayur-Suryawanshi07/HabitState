package com.mayur.habitstate.domain.model

import java.time.LocalDate

data class HabitHeatmapCell(
    val date: LocalDate,
    val isCompleted: Boolean,
    val isScheduled: Boolean
)

/**
 * Composite domain presentation model representing a habit along with
 * today's completion state and calculated streak.
 */
data class HabitWithStatus(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val heatmap: List<HabitHeatmapCell> = emptyList()
)
