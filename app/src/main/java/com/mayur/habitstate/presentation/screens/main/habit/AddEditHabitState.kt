package com.mayur.habitstate.presentation.screens.main.habit

import com.mayur.habitstate.domain.model.HabitFrequencyType
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * High-level habit types inspired by reference design.
 */
enum class HabitKind(val label: String, val description: String) {
    REGULAR(
        label = "REGULAR",
        description = "Related to your daily routine. Check it in a regular and repeated way. E.g. Do yoga three times a week"
    ),
    NEGATIVE(
        label = "NEGATIVE",
        description = "Break an unwanted habit. Keep track of urges and resist them"
    ),
    ONE_TIME(
        label = "ONE-TIME TODO",
        description = "Single task or milestone to accomplish"
    )
}

data class HabitPresetItem(
    val title: String,
    val category: String,
    val iconName: String,
    val colorHex: Long,
    val timeOfDay: String,
    val defaultHour: Int
)

data class AddEditHabitState(
    val habitId: Long = -1L,
    val isEditMode: Boolean = false,
    val name: String = "",
    val description: String = "",
    val category: String = "General",
    val habitKind: HabitKind = HabitKind.REGULAR,
    val timeOfDay: String = "ANYTIME", // ANYTIME, MORNING, AFTERNOON, EVENING
    val selectedColor: Long = 0xFF6A1B9A, // HabitState Purple Focus
    val selectedIcon: String = "fitness",
    val frequencyType: HabitFrequencyType = HabitFrequencyType.EVERYDAY,
    val frequencyDays: Set<Int> = (1..7).toSet(),
    val periodTarget: Int = 3,
    val periodLengthDays: Int = 7,
    val isReminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val isActive: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val generalError: String? = null,
    val isSavedSuccessfully: Boolean = false
) {
    val isDaily: Boolean get() = frequencyDays.size == 7

    val formattedReminderTime: String
        get() {
            val time = LocalTime.of(reminderHour, reminderMinute)
            return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
        }
}
