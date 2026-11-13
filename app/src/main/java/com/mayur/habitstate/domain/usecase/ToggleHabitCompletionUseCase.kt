package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.HabitLog
import com.mayur.habitstate.domain.repository.HabitRepository
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler
import java.time.LocalDate

/**
 * Use case to toggle completion of a habit for a specific date (defaults to today).
 * If habit is marked completed for today, pending reminder notification can be dismissed.
 */
class ToggleHabitCompletionUseCase(
    private val repository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler
) {
    suspend operator fun invoke(
        habitId: Long,
        date: LocalDate = LocalDate.now()
    ): Boolean {
        val dateStr = date.toString()
        val isCompleted = repository.isHabitCompletedOnDate(habitId, dateStr)

        return if (isCompleted) {
            repository.deleteHabitLogForDate(habitId, dateStr)
            // If undone and reminder is active, re-schedule reminder if appropriate
            val habit = repository.getHabitByIdOnce(habitId)
            if (habit != null && habit.isActive && habit.isReminderEnabled) {
                reminderScheduler.scheduleReminder(habit)
            }
            false
        } else {
            repository.insertHabitLog(
                HabitLog(
                    habitId = habitId,
                    date = dateStr,
                    completedAt = System.currentTimeMillis()
                )
            )
            // Cancel/dismiss reminder since user completed habit for today
            reminderScheduler.cancelReminder(habitId)
            true
        }
    }
}
