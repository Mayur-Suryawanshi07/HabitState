package com.mayur.habitstate.domain.model

/**
 * Domain entity representing a single day's completion record for a habit.
 * [date] is formatted as "YYYY-MM-DD" (ISO-8601).
 */
data class HabitLog(
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val completedAt: Long = System.currentTimeMillis()
)
