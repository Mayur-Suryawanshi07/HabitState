package com.mayur.habitstate.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HabitFrequencyType {
    EVERYDAY,
    WEEK_DAYS,
    MONTH_DAYS,
    PERIOD
}

/**
 * Domain entity representing a habit to track.
 * Completely free of Android framework dependencies.
 */
data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "fitness",
    val colorHex: Long = 0xFF6A1B9A, // Default Purple Focus ARGB
    val category: String = "General",
    val timeOfDay: String = "ANYTIME", // ANYTIME, MORNING, AFTERNOON, EVENING
    val frequencyType: HabitFrequencyType = HabitFrequencyType.WEEK_DAYS,
    val frequencyDays: Set<Int> = (1..7).toSet(), // week days 1-7 or month days 1-31
    val periodTarget: Int = 3,
    val periodLengthDays: Int = 7,
    val reminderHour: Int? = null, // 0..23
    val reminderMinute: Int? = null, // 0..59
    val isReminderEnabled: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if this habit is scheduled to run on the given day of the week.
     * @param dayOfWeek 1 for Monday through 7 for Sunday.
     */
    fun isScheduledForDay(dayOfWeek: Int): Boolean {
        return when (frequencyType) {
            HabitFrequencyType.EVERYDAY, HabitFrequencyType.PERIOD, HabitFrequencyType.MONTH_DAYS -> true
            HabitFrequencyType.WEEK_DAYS -> frequencyDays.isEmpty() || frequencyDays.contains(dayOfWeek)
        }
    }

    /**
     * Checks if this habit is scheduled on a concrete calendar date.
     * Period habits can be completed on any day in the period.
     */
    fun isScheduledForDate(date: LocalDate): Boolean {
        return when (frequencyType) {
            HabitFrequencyType.EVERYDAY, HabitFrequencyType.PERIOD -> true
            HabitFrequencyType.WEEK_DAYS -> frequencyDays.isEmpty() || frequencyDays.contains(date.dayOfWeek.value)
            HabitFrequencyType.MONTH_DAYS -> frequencyDays.contains(date.dayOfMonth)
        }
    }

    fun createdOnDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return Instant.ofEpochMilli(createdAt).atZone(zoneId).toLocalDate()
    }

    /**
     * Checks if this habit runs every day of the week.
     */
    fun isDaily(): Boolean {
        return frequencyType == HabitFrequencyType.EVERYDAY ||
            ((frequencyType == HabitFrequencyType.WEEK_DAYS) && (frequencyDays.size == 7 || frequencyDays.isEmpty()))
    }

    /**
     * Resolves effective time of day bucket for filtering (MORNING, AFTERNOON, EVENING, ANYTIME).
     */
    fun effectiveTimeOfDay(): String {
        if (timeOfDay.isNotBlank() && timeOfDay != "ANYTIME") {
            return timeOfDay.uppercase(Locale.ROOT)
        }
        val hour = reminderHour ?: return "ANYTIME"
        return when (hour) {
            in 5..11 -> "MORNING"
            in 12..16 -> "AFTERNOON"
            else -> "EVENING"
        }
    }

    /**
     * Formats reminder time as e.g. "08:30 AM" or null if not set.
     */
    fun formattedReminderTime(): String? {
        if (reminderHour == null || reminderMinute == null) return null
        val time = LocalTime.of(reminderHour, reminderMinute)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        return time.format(formatter)
    }

    /**
     * Formats frequency days as a human readable string like "Daily" or "Mon, Wed, Fri".
     */
    fun formattedFrequency(): String {
        return when (frequencyType) {
            HabitFrequencyType.EVERYDAY -> "Every day"
            HabitFrequencyType.PERIOD -> "$periodTarget days / $periodLengthDays days"
            HabitFrequencyType.MONTH_DAYS -> {
                if (frequencyDays.isEmpty()) "Specific days of month"
                else frequencyDays.sorted().joinToString(", ") { day ->
                    "$day${ordinalSuffix(day)}"
                }
            }
            HabitFrequencyType.WEEK_DAYS -> {
                if (isDaily()) return "Every day"
                if (frequencyDays == setOf(1, 2, 3, 4, 5)) return "Weekdays"
                if (frequencyDays == setOf(6, 7)) return "Weekends"
                val dayNames = mapOf(
                    1 to "Mon",
                    2 to "Tue",
                    3 to "Wed",
                    4 to "Thu",
                    5 to "Fri",
                    6 to "Sat",
                    7 to "Sun"
                )
                frequencyDays.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
            }
        }
    }

    fun encodeFrequency(): String {
        return when (frequencyType) {
            HabitFrequencyType.EVERYDAY -> "W:1,2,3,4,5,6,7"
            HabitFrequencyType.WEEK_DAYS -> "W:" + frequencyDays.sorted().joinToString(",")
            HabitFrequencyType.MONTH_DAYS -> "M:" + frequencyDays.sorted().joinToString(",")
            HabitFrequencyType.PERIOD -> "P:$periodTarget,$periodLengthDays"
        }
    }

    companion object {
        fun ordinalSuffix(day: Int): String {
            return when {
                day in 11..13 -> "th"
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }
        }

        fun decodeFrequency(raw: String): DecodedFrequency {
            val value = raw.trim()
            return when {
                value.startsWith("W:") -> {
                    val days = parseInts(value.removePrefix("W:"))
                    val resolved = days.ifEmpty { (1..7).toSet() }
                    val type = if (resolved.size == 7) HabitFrequencyType.EVERYDAY else HabitFrequencyType.WEEK_DAYS
                    DecodedFrequency(type, resolved, 3, 7)
                }
                value.startsWith("M:") -> {
                    val days = parseInts(value.removePrefix("M:")).filter { it in 1..31 }.toSet()
                    DecodedFrequency(
                        type = HabitFrequencyType.MONTH_DAYS,
                        days = days.ifEmpty { setOf(1) },
                        periodTarget = 3,
                        periodLengthDays = 7
                    )
                }
                value.startsWith("P:") -> {
                    val parts = parseInts(value.removePrefix("P:")).toList()
                    val target = parts.getOrNull(0)?.coerceIn(1, 30) ?: 3
                    val length = parts.getOrNull(1)?.coerceIn(1, 90) ?: 7
                    DecodedFrequency(
                        type = HabitFrequencyType.PERIOD,
                        days = emptySet(),
                        periodTarget = target,
                        periodLengthDays = length
                    )
                }
                else -> {
                    val days = parseInts(value)
                    val resolved = days.ifEmpty { (1..7).toSet() }
                    val type = if (resolved.size == 7) HabitFrequencyType.EVERYDAY else HabitFrequencyType.WEEK_DAYS
                    DecodedFrequency(type, resolved, 3, 7)
                }
            }
        }

        private fun parseInts(raw: String): Set<Int> {
            if (raw.isBlank()) return emptySet()
            return raw.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }
    }
}

data class DecodedFrequency(
    val type: HabitFrequencyType,
    val days: Set<Int>,
    val periodTarget: Int,
    val periodLengthDays: Int
)
