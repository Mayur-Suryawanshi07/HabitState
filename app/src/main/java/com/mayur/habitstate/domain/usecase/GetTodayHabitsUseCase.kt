package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.HabitHeatmapCell
import com.mayur.habitstate.domain.model.HabitWithStatus
import com.mayur.habitstate.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * Use case to observe the list of active habits scheduled for a date,
 * combined with their completion status, streak, and consistency heatmap.
 */
class GetTodayHabitsUseCase(
    private val repository: HabitRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<List<HabitWithStatus>> {
        val heatmapDates = (HEATMAP_DAYS - 1 downTo 0).map { date.minusDays(it.toLong()) }

        return combine(
            repository.getActiveHabits(),
            repository.getAllLogs()
        ) { activeHabits, allLogs ->
            val logsByHabit = allLogs.groupBy { it.habitId }
            val completedOnDate = allLogs.filter { it.date == date.toString() }.map { it.habitId }.toSet()

            val todayHabits = activeHabits.filter { it.isScheduledForDate(date) }

            todayHabits.map { habit ->
                val habitLogs = logsByHabit[habit.id].orEmpty()
                val completedDates = habitLogs.map { it.date }.toSet()
                val streak = calculateStreakUseCase(habit, habitLogs, date)
                val heatmap = heatmapDates.map { cellDate ->
                    HabitHeatmapCell(
                        date = cellDate,
                        isCompleted = completedDates.contains(cellDate.toString()),
                        isScheduled = habit.isScheduledForDate(cellDate)
                    )
                }

                HabitWithStatus(
                    habit = habit,
                    isCompletedToday = completedOnDate.contains(habit.id),
                    currentStreak = streak,
                    heatmap = heatmap
                )
            }
        }
    }

    companion object {
        const val HEATMAP_DAYS = 70
    }
}
