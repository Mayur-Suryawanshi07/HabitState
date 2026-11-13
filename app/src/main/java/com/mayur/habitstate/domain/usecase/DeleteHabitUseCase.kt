package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.repository.HabitRepository
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler

/**
 * Use case to delete a habit, its history, and cancel its pending reminders.
 */
class DeleteHabitUseCase(
    private val repository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler
) {
    suspend operator fun invoke(habit: Habit) {
        reminderScheduler.cancelReminder(habit.id)
        repository.deleteHabit(habit)
    }
}
