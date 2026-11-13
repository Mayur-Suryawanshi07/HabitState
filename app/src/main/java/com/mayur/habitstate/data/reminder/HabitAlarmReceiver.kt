package com.mayur.habitstate.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class HabitAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(HabitNotificationManager.EXTRA_HABIT_ID, -1L)
        if (habitId <= 0L) return

        val inputData = Data.Builder()
            .putLong(HabitNotificationManager.EXTRA_HABIT_ID, habitId)
            .build()

        val reminderWorkRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInputData(inputData)
            .addTag("habit_reminder_$habitId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "habit_reminder_work_$habitId",
            ExistingWorkPolicy.REPLACE,
            reminderWorkRequest
        )
    }
}
