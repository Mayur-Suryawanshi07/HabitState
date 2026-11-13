package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * Use case to compute habit completion progress (0.0f..1.0f) for a given set of dates
 * (such as the 7 days of the current week).
 */
class GetWeekCompletionUseCase(
    private val repository: HabitRepository
) {
    operator fun invoke(dates: List<LocalDate>): Flow<Map<LocalDate, Float>> {
        val dateStrings = dates.map { it.toString() }

        return combine(
            repository.getActiveHabits(),
            repository.getHabitLogsForDates(dateStrings)
        ) { activeHabits, logs ->
            val logsByDate = logs.groupBy { it.date }

            dates.associateWith { date ->
                val scheduledHabits = activeHabits.filter { it.isScheduledForDate(date) }
                if (scheduledHabits.isEmpty()) {
                    0f
                } else {
                    val dateStr = date.toString()
                    val completedIds = logsByDate[dateStr]?.map { it.habitId }?.toSet().orEmpty()
                    val completedCount = scheduledHabits.count { completedIds.contains(it.id) }
                    (completedCount.toFloat() / scheduledHabits.size).coerceIn(0f, 1f)
                }
            }
        }
    }
}
