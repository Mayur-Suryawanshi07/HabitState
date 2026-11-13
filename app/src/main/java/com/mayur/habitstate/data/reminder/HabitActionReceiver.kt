package com.mayur.habitstate.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mayur.habitstate.data.local.database.HabitDatabase
import com.mayur.habitstate.data.local.entity.HabitLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(HabitNotificationManager.EXTRA_HABIT_ID, -1L)
        val notificationId = intent.getIntExtra(
            HabitNotificationManager.EXTRA_NOTIFICATION_ID,
            habitId.toInt()
        )
        val habitName = intent.getStringExtra(HabitNotificationManager.EXTRA_HABIT_NAME) ?: "Habit"

        val notificationManager = HabitNotificationManager(context)
        notificationManager.dismissNotification(notificationId)

        when (intent.action) {
            HabitNotificationManager.ACTION_MARK_DONE -> {
                if (habitId > 0L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val database = HabitDatabase.getInstance(context)
                            val todayStr = LocalDate.now().toString()
                            database.habitLogDao().insertLog(
                                HabitLogEntity(
                                    habitId = habitId,
                                    date = todayStr,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            HabitNotificationManager.ACTION_SNOOZE -> {
                if (habitId > 0L) {
                    val scheduler = HabitReminderSchedulerImpl(context)
                    scheduler.scheduleSnooze(habitId, habitName, 10 * 60 * 1000L)
                }
            }
        }
    }
}
