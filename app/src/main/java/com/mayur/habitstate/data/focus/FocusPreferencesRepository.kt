package com.mayur.habitstate.data.focus

import android.content.Context
import android.content.SharedPreferences
import com.mayur.habitstate.presentation.screens.main.focus.FocusSchedule
import java.util.Calendar

class FocusPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("habitstate_focus_prefs", Context.MODE_PRIVATE)

    fun isFocusActive(): Boolean {
        val manualActive = prefs.getBoolean(KEY_FOCUS_ACTIVE, false)
        if (manualActive) return true
        return isScheduleActiveNow()
    }

    fun isManualFocusActive(): Boolean {
        return prefs.getBoolean(KEY_FOCUS_ACTIVE, false)
    }

    fun setFocusActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_FOCUS_ACTIVE, active).apply()
    }

    fun getDistractingApps(): Set<String> {
        return prefs.getStringSet(KEY_DISTRACTING_APPS, emptySet()) ?: emptySet()
    }

    fun setDistractingApps(apps: Set<String>) {
        prefs.edit().putStringSet(KEY_DISTRACTING_APPS, apps).apply()
    }

    fun toggleDistractingApp(packageName: String) {
        val current = getDistractingApps().toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        setDistractingApps(current)
    }

    fun getBreakEndTimestamp(): Long {
        return prefs.getLong(KEY_BREAK_END_TIMESTAMP, 0L)
    }

    fun setBreakEndTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_BREAK_END_TIMESTAMP, timestamp).apply()
    }

    fun isOnBreak(): Boolean {
        val breakEnd = getBreakEndTimestamp()
        return System.currentTimeMillis() < breakEnd
    }

    fun getSchedule(): FocusSchedule {
        val isEnabled = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
        val startHour = prefs.getInt(KEY_SCHEDULE_START_HOUR, 9)
        val startMinute = prefs.getInt(KEY_SCHEDULE_START_MINUTE, 0)
        val endHour = prefs.getInt(KEY_SCHEDULE_END_HOUR, 17)
        val endMinute = prefs.getInt(KEY_SCHEDULE_END_MINUTE, 0)
        val daysString = prefs.getString(KEY_SCHEDULE_DAYS, "2,3,4,5,6") ?: "2,3,4,5,6"
        val activeDays = daysString.split(",").mapNotNull { it.toIntOrNull() }.toSet()

        return FocusSchedule(
            isEnabled = isEnabled,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            activeDays = activeDays
        )
    }

    fun saveSchedule(schedule: FocusSchedule) {
        val daysString = schedule.activeDays.joinToString(",")
        prefs.edit()
            .putBoolean(KEY_SCHEDULE_ENABLED, schedule.isEnabled)
            .putInt(KEY_SCHEDULE_START_HOUR, schedule.startHour)
            .putInt(KEY_SCHEDULE_START_MINUTE, schedule.startMinute)
            .putInt(KEY_SCHEDULE_END_HOUR, schedule.endHour)
            .putInt(KEY_SCHEDULE_END_MINUTE, schedule.endMinute)
            .putString(KEY_SCHEDULE_DAYS, daysString)
            .apply()
    }

    fun isScheduleActiveNow(): Boolean {
        val schedule = getSchedule()
        if (!schedule.isEnabled) return false

        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK) // 1=Sunday..7=Saturday
        if (!schedule.activeDays.contains(currentDay)) return false

        val currentMinutes: Int = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes: Int = schedule.startHour * 60 + schedule.startMinute
        val endMinutes: Int = schedule.endHour * 60 + schedule.endMinute

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    companion object {
        private const val KEY_FOCUS_ACTIVE = "key_focus_active"
        private const val KEY_DISTRACTING_APPS = "key_distracting_apps"
        private const val KEY_BREAK_END_TIMESTAMP = "key_break_end_timestamp"
        private const val KEY_SCHEDULE_ENABLED = "key_schedule_enabled"
        private const val KEY_SCHEDULE_START_HOUR = "key_schedule_start_hour"
        private const val KEY_SCHEDULE_START_MINUTE = "key_schedule_start_minute"
        private const val KEY_SCHEDULE_END_HOUR = "key_schedule_end_hour"
        private const val KEY_SCHEDULE_END_MINUTE = "key_schedule_end_minute"
        private const val KEY_SCHEDULE_DAYS = "key_schedule_days"
    }
}
