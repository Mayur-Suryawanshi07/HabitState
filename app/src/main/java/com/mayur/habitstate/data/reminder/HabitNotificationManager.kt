package com.mayur.habitstate.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mayur.habitstate.MainActivity
import com.mayur.habitstate.R

import android.annotation.SuppressLint

class HabitNotificationManager(private val context: Context) {

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showHabitReminderNotification(
        habitId: Long,
        habitName: String,
        description: String? = null
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Done Action
        val markDoneIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_NOTIFICATION_ID, habitId.toInt())
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId * 10 + 1).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze (10 min) Action
        val snoozeIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
            putExtra(EXTRA_NOTIFICATION_ID, habitId.toInt())
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (!description.isNullOrBlank()) {
            description
        } else {
            "Time to complete your habit today! Stay on track."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Habit Reminder: $habitName")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Mark Done",
                markDonePendingIntent
            )
            .addAction(
                android.R.drawable.ic_popup_reminder,
                "Snooze (10m)",
                snoozePendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(habitId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted by user
        }
    }

    fun dismissNotification(notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders_channel"
        const val CHANNEL_NAME = "Habit Reminders"
        const val CHANNEL_DESCRIPTION = "Daily notifications for scheduled habit reminders"

        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        const val ACTION_MARK_DONE = "com.mayur.habitstate.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.mayur.habitstate.ACTION_SNOOZE"
    }
}
