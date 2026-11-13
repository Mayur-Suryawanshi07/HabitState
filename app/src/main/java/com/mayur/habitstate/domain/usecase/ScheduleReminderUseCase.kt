package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler

/**
 * Use case to coordinate scheduling, cancelling, and bulk-rescheduling of habit reminders.
 */
class ScheduleReminderUseCase(
    private val reminderScheduler: HabitReminderScheduler
) {
    fun schedule(habit: Habit) {
        reminderScheduler.scheduleReminder(habit)
    }

    fun cancel(habitId: Long) {
        reminderScheduler.cancelReminder(habitId)
    }

    suspend fun rescheduleAll() {
        reminderScheduler.rescheduleAllReminders()
    }
}
