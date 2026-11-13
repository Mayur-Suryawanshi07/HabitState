package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog
import java.time.LocalDate

/**
 * Calculates the current consecutive streak for a given habit based on its scheduled frequency
 * and recorded completion logs.
 */
class CalculateStreakUseCase {

    /**
     * Calculates the streak of consecutive scheduled days completed up to [referenceDate].
     * If today is not yet completed, the streak from yesterday is preserved.
     */
    operator fun invoke(
        habit: Habit,
        logs: List<HabitLog>,
        referenceDate: LocalDate = LocalDate.now()
    ): Int {
        if (logs.isEmpty()) return 0

        val completedDates = logs.mapNotNull {
            try {
                LocalDate.parse(it.date)
            } catch (_: Exception) {
                null
            }
        }.toSet()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        var checkDate = referenceDate

        // Check if habit was completed today
        val completedToday = completedDates.contains(referenceDate)
        if (completedToday) {
            // Count today if today was a scheduled day
            if (habit.isScheduledForDate(referenceDate)) {
                streak++
            }
            checkDate = referenceDate.minusDays(1)
        } else {
            // Today is not completed yet; start checking from yesterday so the user's streak isn't reset early
            checkDate = referenceDate.minusDays(1)
        }

        // Iterate backwards through prior days
        var safetyCounter = 0
        val maxDaysToInspect = 365 * 3 // look back up to 3 years max

        while (safetyCounter < maxDaysToInspect) {
            safetyCounter++
            if (habit.isScheduledForDate(checkDate)) {
                if (completedDates.contains(checkDate)) {
                    streak++
                } else {
                    // Scheduled day was missed; streak is broken
                    break
                }
            }
            // If habit was not scheduled for this day, skip it without breaking streak

            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
