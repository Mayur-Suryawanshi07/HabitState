package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetWeekCompletionUseCaseTest {

    private lateinit var repository: FakeHabitRepository
    private lateinit var getWeekCompletionUseCase: GetWeekCompletionUseCase

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
        getWeekCompletionUseCase = GetWeekCompletionUseCase(repository)
    }

    @Test
    fun `calculates completion ratio for each day of week`() = runBlocking {
        // Monday Nov 9, 2026 (dayOfWeek = 1)
        // Tuesday Nov 10, 2026 (dayOfWeek = 2)
        val monday = LocalDate.of(2026, 11, 9)
        val tuesday = LocalDate.of(2026, 11, 10)
        val wednesday = LocalDate.of(2026, 11, 11)

        val mondayHabit = Habit(id = 1, name = "Daily Habit", frequencyDays = (1..7).toSet())
        val tuesdayOnlyHabit = Habit(id = 2, name = "Tuesday Routine", frequencyDays = setOf(2))

        repository.insertHabit(mondayHabit)
        repository.insertHabit(tuesdayOnlyHabit)

        // Monday: 1 habit scheduled, 1 completed -> 1.0f
        repository.insertHabitLog(HabitLog(habitId = 1, date = "2026-11-09"))
        // Tuesday: 2 habits scheduled (daily + tuesday only), 1 completed -> 0.5f
        repository.insertHabitLog(HabitLog(habitId = 1, date = "2026-11-10"))
        // Wednesday: 1 habit scheduled, 0 completed -> 0.0f

        val result = getWeekCompletionUseCase(listOf(monday, tuesday, wednesday)).first()

        assertEquals(1.0f, result[monday] ?: 0f, 0.01f)
        assertEquals(0.5f, result[tuesday] ?: 0f, 0.01f)
        assertEquals(0.0f, result[wednesday] ?: 0f, 0.01f)
    }
}
