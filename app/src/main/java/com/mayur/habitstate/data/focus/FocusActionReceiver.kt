package com.mayur.habitstate.data.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FocusActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val prefsRepository = FocusPreferencesRepository(context)
        val notificationManager = FocusNotificationManager(context)

        when (action) {
            FocusNotificationManager.ACTION_TURN_OFF_FOCUS -> {
                prefsRepository.setFocusActive(false)
                prefsRepository.setBreakEndTimestamp(0L)
                notificationManager.dismissFocusNotification()
                broadcastFocusStateChanged(context)
            }
            FocusNotificationManager.ACTION_TAKE_BREAK -> {
                val minutes = intent.getIntExtra(FocusNotificationManager.EXTRA_BREAK_MINUTES, 5)
                val breakEnd = System.currentTimeMillis() + (minutes * 60 * 1000L)
                prefsRepository.setBreakEndTimestamp(breakEnd)
                notificationManager.showFocusActiveNotification(
                    isOnBreak = true,
                    breakRemainingMinutes = minutes,
                    distractingAppsCount = prefsRepository.getDistractingApps().size
                )
                broadcastFocusStateChanged(context)
            }
            FocusNotificationManager.ACTION_RESUME_FOCUS -> {
                prefsRepository.setBreakEndTimestamp(0L)
                notificationManager.showFocusActiveNotification(
                    isOnBreak = false,
                    distractingAppsCount = prefsRepository.getDistractingApps().size
                )
                broadcastFocusStateChanged(context)
            }
        }
    }

    private fun broadcastFocusStateChanged(context: Context) {
        val updateIntent = Intent(FocusNotificationManager.ACTION_FOCUS_STATE_CHANGED).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(updateIntent)
    }
}
