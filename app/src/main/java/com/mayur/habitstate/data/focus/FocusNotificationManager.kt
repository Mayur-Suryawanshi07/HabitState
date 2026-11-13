package com.mayur.habitstate.data.focus

import android.annotation.SuppressLint
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

class FocusNotificationManager(private val context: Context) {

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showFocusActiveNotification(
        isOnBreak: Boolean = false,
        breakRemainingMinutes: Int = 0,
        distractingAppsCount: Int = 0
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, "focus")
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_TAP,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Turn Off Action
        val turnOffIntent = Intent(context, FocusActionReceiver::class.java).apply {
            action = ACTION_TURN_OFF_FOCUS
        }
        val turnOffPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TURN_OFF,
            turnOffIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(tapPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        if (isOnBreak) {
            builder.setContentTitle("Focus is on (Break active)")
            val breakText = if (breakRemainingMinutes > 0) {
                "Break active: $breakRemainingMinutes min remaining"
            } else {
                "Break ending soon"
            }
            builder.setContentText(breakText)

            // Action to Resume Focus
            val resumeIntent = Intent(context, FocusActionReceiver::class.java).apply {
                action = ACTION_RESUME_FOCUS
            }
            val resumePendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_RESUME,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_play, "Resume focus", resumePendingIntent)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Turn off now", turnOffPendingIntent)
        } else {
            builder.setContentTitle("Focus is on")
            val subtitle = if (distractingAppsCount > 0) {
                "Distracting apps are paused"
            } else {
                "Focus mode active"
            }
            builder.setContentText(subtitle)

            // Action to Take a Break (5 mins)
            val breakIntent = Intent(context, FocusActionReceiver::class.java).apply {
                action = ACTION_TAKE_BREAK
                putExtra(EXTRA_BREAK_MINUTES, 5)
            }
            val breakPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BREAK,
                breakIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_popup_reminder, "Take a break", breakPendingIntent)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Turn off now", turnOffPendingIntent)
        }

        try {
            NotificationManagerCompat.from(context).notify(FOCUS_NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Notifications permission might be disabled by user
        }
    }

    fun dismissFocusNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(FOCUS_NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "focus_mode_channel"
        const val CHANNEL_NAME = "Focus Mode"
        const val CHANNEL_DESCRIPTION = "Displays status and quick controls while Focus Mode is active"

        const val FOCUS_NOTIFICATION_ID = 9001
        private const val REQUEST_CODE_TAP = 9002
        private const val REQUEST_CODE_TURN_OFF = 9003
        private const val REQUEST_CODE_BREAK = 9004
        private const val REQUEST_CODE_RESUME = 9005

        const val ACTION_TURN_OFF_FOCUS = "com.mayur.habitstate.ACTION_TURN_OFF_FOCUS"
        const val ACTION_TAKE_BREAK = "com.mayur.habitstate.ACTION_TAKE_BREAK"
        const val ACTION_RESUME_FOCUS = "com.mayur.habitstate.ACTION_RESUME_FOCUS"
        const val ACTION_FOCUS_STATE_CHANGED = "com.mayur.habitstate.ACTION_FOCUS_STATE_CHANGED"

        const val EXTRA_BREAK_MINUTES = "extra_break_minutes"
        const val EXTRA_DESTINATION = "extra_destination"
    }
}
