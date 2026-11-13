package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalculateStreakUseCaseTest {

    private lateinit var calculateStreakUseCase: CalculateStreakUseCase

    @Before
    fun setUp() {
        calculateStreakUseCase = CalculateStreakUseCase()
    }

    @Test
    fun `when no logs exist, streak is zero`() {
        val habit = Habit(id = 1, name = "Workout")
        val streak = calculateStreakUseCase(habit, emptyList())
        assertEquals(0, streak)
    }

    @Test
    fun `when completed today and yesterday for daily habit, streak is 2`() {
        val today = LocalDate.of(2026, 11, 5) // Thursday
        val habit = Habit(id = 1, name = "Read", frequencyDays = (1..7).toSet())

        val logs = listOf(
            HabitLog(id = 1, habitId = 1, date = "2026-11-05"),
            HabitLog(id = 2, habitId = 1, date = "2026-11-04")
        )

        val streak = calculateStreakUseCase(habit, logs, referenceDate = today)
        assertEquals(2, streak)
    }

    @Test
    fun `when today not yet completed, yesterday streak is preserved`() {
        val today = LocalDate.of(2026, 11, 5)
        val habit = Habit(id = 1, name = "Meditation", frequencyDays = (1..7).toSet())

        val logs = listOf(
            HabitLog(id = 1, habitId = 1, date = "2026-11-04"),
            HabitLog(id = 2, habitId = 1, date = "2026-11-03")
        )

        val streak = calculateStreakUseCase(habit, logs, referenceDate = today)
        assertEquals(2, streak)
    }

    @Test
    fun `when a scheduled day is missed, streak breaks`() {
        val today = LocalDate.of(2026, 11, 5) // Thursday
        val habit = Habit(id = 1, name = "Water", frequencyDays = (1..7).toSet())

        val logs = listOf(
            HabitLog(id = 1, habitId = 1, date = "2026-11-05"),
            // missed 2026-11-04!
            HabitLog(id = 2, habitId = 1, date = "2026-11-03"),
            HabitLog(id = 3, habitId = 1, date = "2026-11-02")
        )

        val streak = calculateStreakUseCase(habit, logs, referenceDate = today)
        assertEquals(1, streak)
    }

    @Test
    fun `non-scheduled days do not break streak`() {
        // Monday = 1, Tuesday = 2, Wednesday = 3, Thursday = 4, Friday = 5
        // Saturday = 6, Sunday = 7
        // Monday Nov 9, 2026
        val monday = LocalDate.of(2026, 11, 9)
        val weekdayHabit = Habit(
            id = 1,
            name = "Code",
            frequencyDays = setOf(1, 2, 3, 4, 5) // Weekdays only
        )

        // User completed Friday Nov 6 and Monday Nov 9 (Sat & Sun not scheduled)
        val logs = listOf(
            HabitLog(id = 1, habitId = 1, date = "2026-11-09"), // Monday
            HabitLog(id = 2, habitId = 1, date = "2026-11-06")  // Friday
        )

        val streak = calculateStreakUseCase(weekdayHabit, logs, referenceDate = monday)
        assertEquals(2, streak)
    }
}
