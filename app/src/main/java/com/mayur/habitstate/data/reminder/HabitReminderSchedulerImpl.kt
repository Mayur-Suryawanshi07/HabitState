package com.mayur.habitstate.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mayur.habitstate.data.local.database.HabitDatabase
import com.mayur.habitstate.data.local.mapper.toDomain
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class HabitReminderSchedulerImpl(
    private val context: Context
) : HabitReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleReminder(habit: Habit) {
        if (!habit.isActive || !habit.isReminderEnabled) {
            cancelReminder(habit.id)
            return
        }

        val hour = habit.reminderHour ?: return
        val minute = habit.reminderMinute ?: return

        val triggerAtMillis = calculateNextTriggerMillis(
            frequencyDays = habit.frequencyDays,
            reminderHour = hour,
            reminderMinute = minute
        ) ?: return

        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            putExtra(HabitNotificationManager.EXTRA_HABIT_ID, habit.id)
            putExtra(HabitNotificationManager.EXTRA_HABIT_NAME, habit.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // In case exact alarm permission is revoked at runtime
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancelReminder(habitId: Long) {
        val intent = Intent(context, HabitAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override suspend fun rescheduleAllReminders() {
        val database = HabitDatabase.getInstance(context)
        val activeHabits = database.habitDao().getActiveHabitsOnce()
        for (entity in activeHabits) {
            val habit = entity.toDomain()
            if (habit.isReminderEnabled) {
                scheduleReminder(habit)
            }
        }
    }

    fun scheduleSnooze(habitId: Long, habitName: String, delayMillis: Long = 10 * 60 * 1000L) {
        val triggerAtMillis = System.currentTimeMillis() + delayMillis
        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            putExtra(HabitNotificationManager.EXTRA_HABIT_ID, habitId)
            putExtra(HabitNotificationManager.EXTRA_HABIT_NAME, habitName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId * 100).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    companion object {
        fun calculateNextTriggerMillis(
            frequencyDays: Set<Int>,
            reminderHour: Int,
            reminderMinute: Int,
            now: LocalDateTime = LocalDateTime.now()
        ): Long? {
            val targetDays = if (frequencyDays.isEmpty()) (1..7).toSet() else frequencyDays
            val todayDate = now.toLocalDate()
            val reminderTime = LocalTime.of(reminderHour, reminderMinute)

            // Check today first
            if (targetDays.contains(todayDate.dayOfWeek.value)) {
                val candidateDateTime = todayDate.atTime(reminderTime)
                if (candidateDateTime.isAfter(now)) {
                    return candidateDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }

            // Look ahead for the next 7 days
            for (offset in 1..7) {
                val nextDate = todayDate.plusDays(offset.toLong())
                if (targetDays.contains(nextDate.dayOfWeek.value)) {
                    val candidateDateTime = nextDate.atTime(reminderTime)
                    return candidateDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }

            return null
        }
    }
}
