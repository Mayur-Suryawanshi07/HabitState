package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetTodayHabitsUseCaseTest {

    private lateinit var repository: FakeHabitRepository
    private lateinit var calculateStreakUseCase: CalculateStreakUseCase
    private lateinit var getTodayHabitsUseCase: GetTodayHabitsUseCase

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
        calculateStreakUseCase = CalculateStreakUseCase()
        getTodayHabitsUseCase = GetTodayHabitsUseCase(repository, calculateStreakUseCase)
    }

    @Test
    fun `returns only active habits scheduled for specified date`() = runBlocking {
        // Monday = 1
        val monday = LocalDate.of(2026, 11, 9)

        val mondayHabit = Habit(
            id = 1,
            name = "Monday Routine",
            frequencyDays = setOf(1), // Monday only
            isActive = true
        )
        val weekendHabit = Habit(
            id = 2,
            name = "Weekend Routine",
            frequencyDays = setOf(6, 7), // Sat/Sun only
            isActive = true
        )
        val pausedMondayHabit = Habit(
            id = 3,
            name = "Paused Routine",
            frequencyDays = setOf(1),
            isActive = false // Inactive
        )

        repository.insertHabit(mondayHabit)
        repository.insertHabit(weekendHabit)
        repository.insertHabit(pausedMondayHabit)

        val result = getTodayHabitsUseCase(monday).first()

        assertEquals(1, result.size)
        assertEquals("Monday Routine", result.first().habit.name)
        assertFalse(result.first().isCompletedToday)
    }

    @Test
    fun `marks habit completed and calculates streak when log exists for today`() = runBlocking {
        val today = LocalDate.of(2026, 11, 5)

        val habit = Habit(
            id = 1,
            name = "Hydrate",
            frequencyDays = (1..7).toSet(),
            isActive = true
        )
        repository.insertHabit(habit)
        repository.insertHabitLog(HabitLog(habitId = 1, date = "2026-11-05"))
        repository.insertHabitLog(HabitLog(habitId = 1, date = "2026-11-04"))

        val result = getTodayHabitsUseCase(today).first()

        assertEquals(1, result.size)
        assertTrue(result.first().isCompletedToday)
        assertEquals(2, result.first().currentStreak)
    }
}
