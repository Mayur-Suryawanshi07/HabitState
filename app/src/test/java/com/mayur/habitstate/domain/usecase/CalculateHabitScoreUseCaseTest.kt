package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitFrequencyType
import com.mayur.habitstate.domain.model.HabitLog
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalculateHabitScoreUseCaseTest {

    private lateinit var repository: FakeHabitRepository
    private lateinit var useCase: CalculateHabitScoreUseCase

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
        useCase = CalculateHabitScoreUseCase(repository)
    }

    @Test
    fun `score returns zero when habit has no logs`() {
        val today = LocalDate.of(2026, 11, 11)
        val createdMillis = today.minusDays(9).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val habit = Habit(id = 1, name = "Gym", createdAt = createdMillis, frequencyType = HabitFrequencyType.EVERYDAY)

        val summary = useCase.compute(listOf(habit), emptyList(), referenceDate = today)

        assertEquals(1, summary.items.size)
        assertEquals(0, summary.items.first().completedCount)
        assertEquals(10, summary.items.first().expectedCount)
        assertEquals(0f, summary.items.first().scorePercent, 0.01f)
        assertEquals(0f, summary.overallScore, 0.01f)
    }

    @Test
    fun `score correctly calculates completion percentage`() {
        val today = LocalDate.of(2026, 11, 11)
        val createdMillis = today.minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val habit = Habit(id = 1, name = "Water", createdAt = createdMillis, frequencyType = HabitFrequencyType.EVERYDAY)

        // 4 days total (11-08 to 11-11), completed 2 days
        val logs = listOf(
            HabitLog(id = 1, habitId = 1, date = "2026-11-08"),
            HabitLog(id = 2, habitId = 1, date = "2026-11-09")
        )

        val summary = useCase.compute(listOf(habit), logs, referenceDate = today)

        assertEquals(2, summary.items.first().completedCount)
        assertEquals(4, summary.items.first().expectedCount)
        assertEquals(50f, summary.items.first().scorePercent, 0.01f)
        assertEquals(50f, summary.overallScore, 0.01f)
    }
}
