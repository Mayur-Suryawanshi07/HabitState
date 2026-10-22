package com.mayur.habitstate.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.mayur.habitstate.data.focus.FocusPreferencesRepository

class FocusAccessibilityService : AccessibilityService() {

    private lateinit var prefsRepository: FocusPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        prefsRepository = FocusPreferencesRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Do not block own app or system UI
        if (packageName == applicationContext.packageName ||
            packageName.contains("systemui")
        ) {
            return
        }

        val isFocusActive = prefsRepository.isFocusActive()
        val isOnBreak = prefsRepository.isOnBreak()
        val distractingApps = prefsRepository.getDistractingApps()

        if (isFocusActive && !isOnBreak && distractingApps.contains(packageName)) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            Toast.makeText(
                applicationContext,
                "Focus Mode Active! App paused.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onInterrupt() {}

    companion object {
        fun isServiceEnabled(context: Context): Boolean {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val expectedService = "${context.packageName}/${FocusAccessibilityService::class.java.canonicalName}"
            return enabledServices.contains(expectedService) || enabledServices.contains(context.packageName)
        }
    }
}
