package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitFrequencyType
import com.mayur.habitstate.domain.model.HabitLog
import com.mayur.habitstate.domain.model.HabitScoreItem
import com.mayur.habitstate.domain.model.HabitScoreSummary
import com.mayur.habitstate.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

/**
 * Calculates per-habit and overall scores from actual completion records.
 * Future dates and unscheduled dates are excluded from the expected count.
 */
class CalculateHabitScoreUseCase(
    private val repository: HabitRepository
) {
    operator fun invoke(referenceDate: LocalDate = LocalDate.now()): Flow<HabitScoreSummary> {
        return combine(
            repository.getActiveHabits(),
            repository.getAllLogs()
        ) { habits, logs ->
            compute(habits, logs, referenceDate)
        }
    }

    fun compute(
        habits: List<Habit>,
        logs: List<HabitLog>,
        referenceDate: LocalDate = LocalDate.now()
    ): HabitScoreSummary {
        val logsByHabit = logs.groupBy { it.habitId }
        val items = habits.map { habit ->
            scoreHabit(habit, logsByHabit[habit.id].orEmpty(), referenceDate)
        }
        val overall = if (items.isEmpty()) {
            0f
        } else {
            items.map { it.scorePercent }.average().toFloat()
        }
        return HabitScoreSummary(overallScore = overall, items = items)
    }

    private fun scoreHabit(
        habit: Habit,
        logs: List<HabitLog>,
        referenceDate: LocalDate
    ): HabitScoreItem {
        val start = habit.createdOnDate()
        if (start.isAfter(referenceDate)) {
            return HabitScoreItem(habit, 0, 0, 0f)
        }

        val completedDates = logs.mapNotNull { log ->
            runCatching { LocalDate.parse(log.date) }.getOrNull()
        }.filter { date ->
            !date.isBefore(start) && !date.isAfter(referenceDate)
        }.toSet()

        val expected: Int
        val completed: Int

        if (habit.frequencyType == HabitFrequencyType.PERIOD) {
            expected = periodExpected(start, referenceDate, habit.periodTarget, habit.periodLengthDays)
            completed = completedDates.size.coerceAtMost(expected)
        } else {
            var cursor = start
            var scheduled = 0
            var done = 0
            while (!cursor.isAfter(referenceDate)) {
                if (habit.isScheduledForDate(cursor)) {
                    scheduled++
                    if (completedDates.contains(cursor)) done++
                }
                cursor = cursor.plusDays(1)
            }
            expected = scheduled
            completed = done
        }

        val percent = if (expected <= 0) 0f else (completed.toFloat() / expected.toFloat()) * 100f
        return HabitScoreItem(
            habit = habit,
            completedCount = completed,
            expectedCount = expected,
            scorePercent = percent.coerceIn(0f, 100f)
        )
    }

    private fun periodExpected(
        from: LocalDate,
        to: LocalDate,
        target: Int,
        length: Int
    ): Int {
        if (to.isBefore(from) || length <= 0 || target <= 0) return 0
        val days = ChronoUnit.DAYS.between(from, to) + 1
        val fullPeriods = (days / length).toInt()
        val remainder = (days % length).toInt()
        return fullPeriods * target + min(target, remainder)
    }
}
