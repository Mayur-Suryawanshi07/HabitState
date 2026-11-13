package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.repository.HabitRepository
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler

/**
 * Use case to update an existing habit and adjust its reminder state accordingly.
 */
class UpdateHabitUseCase(
    private val repository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler
) {
    suspend operator fun invoke(habit: Habit): Result<Unit> {
        val trimmedName = habit.name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit name cannot be blank"))
        }

        val frequency = if (habit.frequencyDays.isEmpty()) (1..7).toSet() else habit.frequencyDays
        val updatedHabit = habit.copy(name = trimmedName, frequencyDays = frequency)

        repository.updateHabit(updatedHabit)

        if (updatedHabit.isActive && updatedHabit.isReminderEnabled) {
            reminderScheduler.scheduleReminder(updatedHabit)
        } else {
            reminderScheduler.cancelReminder(updatedHabit.id)
        }

        return Result.success(Unit)
    }
}
