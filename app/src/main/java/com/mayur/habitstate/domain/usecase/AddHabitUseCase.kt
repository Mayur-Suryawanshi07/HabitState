package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitFrequencyType
import com.mayur.habitstate.domain.repository.HabitRepository
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler
import java.time.LocalDate

/**
 * Use case to validate and persist a new habit, scheduling its reminder if enabled.
 */
class AddHabitUseCase(
    private val repository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler
) {
    suspend operator fun invoke(habit: Habit): Result<Long> {
        val trimmedName = habit.name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit name cannot be blank"))
        }

        val frequency = when (habit.frequencyType) {
            HabitFrequencyType.PERIOD -> emptySet()
            HabitFrequencyType.MONTH_DAYS ->
                habit.frequencyDays.ifEmpty { setOf(LocalDate.now().dayOfMonth) }
            HabitFrequencyType.EVERYDAY -> (1..7).toSet()
            HabitFrequencyType.WEEK_DAYS ->
                habit.frequencyDays.ifEmpty { (1..7).toSet() }
        }
        val validatedHabit = habit.copy(name = trimmedName, frequencyDays = frequency)

        val id = repository.insertHabit(validatedHabit)
        val createdHabit = validatedHabit.copy(id = id)

        if (createdHabit.isActive && createdHabit.isReminderEnabled) {
            reminderScheduler.scheduleReminder(createdHabit)
        }

        return Result.success(id)
    }
}
