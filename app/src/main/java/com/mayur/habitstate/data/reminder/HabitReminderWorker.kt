package com.mayur.habitstate.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mayur.habitstate.data.local.database.HabitDatabase
import com.mayur.habitstate.data.local.mapper.toDomain
import java.time.LocalDate

class HabitReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getLong(HabitNotificationManager.EXTRA_HABIT_ID, -1L)
        if (habitId <= 0L) return Result.success()

        val database = HabitDatabase.getInstance(applicationContext)
        val habitEntity = database.habitDao().getHabitByIdOnce(habitId) ?: return Result.success()
        val habit = habitEntity.toDomain()

        val scheduler = HabitReminderSchedulerImpl(applicationContext)

        // If habit was paused or reminders turned off, ensure reminder is canceled
        if (!habit.isActive || !habit.isReminderEnabled) {
            scheduler.cancelReminder(habit.id)
            return Result.success()
        }

        val today = LocalDate.now()
        val todayStr = today.toString()
        val dayOfWeekValue = today.dayOfWeek.value

        // Check if habit is scheduled for today
        if (habit.isScheduledForDay(dayOfWeekValue)) {
            // Check if habit is already completed today
            val isCompletedToday = database.habitLogDao().isHabitCompletedOnDate(habitId, todayStr)
            if (!isCompletedToday) {
                // Habit is active, scheduled for today, and not yet completed: Show notification!
                val notificationManager = HabitNotificationManager(applicationContext)
                notificationManager.showHabitReminderNotification(
                    habitId = habit.id,
                    habitName = habit.name,
                    description = habit.description.ifBlank { null }
                )
            }
        }

        // Schedule next occurrence
        scheduler.scheduleReminder(habit)

        return Result.success()
    }
}
