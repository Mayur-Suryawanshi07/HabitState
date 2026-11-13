package com.mayur.habitstate.domain.scheduler

import com.mayur.habitstate.domain.model.Habit

/**
 * Domain interface for scheduling, rescheduling, and canceling local habit reminders.
 */
interface HabitReminderScheduler {
    fun scheduleReminder(habit: Habit)
    fun cancelReminder(habitId: Long)
    suspend fun rescheduleAllReminders()
}
