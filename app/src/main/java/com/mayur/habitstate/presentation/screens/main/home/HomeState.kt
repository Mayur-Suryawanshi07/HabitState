package com.mayur.habitstate.presentation.screens.main.home

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitWithStatus
import java.time.LocalDate

enum class HabitListFilter(val label: String) {
    ALL("All"),
    DUE("Due")
}

data class HomeState(
    val userName: String = "Habit Tracker",
    val userEmail: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDates: List<LocalDate> = emptyList(),
    val weekCompletionStatus: Map<LocalDate, Float> = emptyMap(),
    val selectedFilter: HabitListFilter = HabitListFilter.ALL,
    val habits: List<HabitWithStatus> = emptyList(),
    val habitScore: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddEditDialogOpen: Boolean = false,
    val habitToEdit: Habit? = null
) {
    val totalCount: Int
        get() = habits.size

    val completedCount: Int
        get() = habits.count { it.isCompletedToday }

    val dueCount: Int
        get() = habits.count { !it.isCompletedToday }

    val progressFraction: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

    val progressPercentage: Int
        get() = (progressFraction * 100).toInt()

    val filteredHabits: List<HabitWithStatus>
        get() = if (selectedFilter == HabitListFilter.ALL) {
            habits
        } else {
            habits.filter { !it.isCompletedToday }
        }

    val isTodaySelected: Boolean
        get() = selectedDate == LocalDate.now()
}
