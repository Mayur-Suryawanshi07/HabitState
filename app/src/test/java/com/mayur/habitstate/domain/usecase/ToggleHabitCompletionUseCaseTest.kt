package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ToggleHabitCompletionUseCaseTest {

    private lateinit var repository: FakeHabitRepository
    private lateinit var scheduler: FakeHabitReminderScheduler
    private lateinit var toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
        scheduler = FakeHabitReminderScheduler()
        toggleHabitCompletionUseCase = ToggleHabitCompletionUseCase(repository, scheduler)
    }

    @Test
    fun `when habit is not completed, toggle marks completed and cancels reminder for today`() = runBlocking {
        val today = LocalDate.of(2026, 11, 5)
        val habit = Habit(id = 1, name = "Reading", isReminderEnabled = true)
        repository.insertHabit(habit)

        val result = toggleHabitCompletionUseCase(habitId = 1, date = today)

        assertTrue(result)
        assertTrue(repository.isHabitCompletedOnDate(1, "2026-11-05"))
        assertTrue(scheduler.canceledHabitIds.contains(1L))
    }

    @Test
    fun `when habit is already completed, toggle removes completion log and reschedules reminder`() = runBlocking {
        val today = LocalDate.of(2026, 11, 5)
        val habit = Habit(id = 1, name = "Workout", isReminderEnabled = true, isActive = true)
        repository.insertHabit(habit)

        // First toggle: completes it
        toggleHabitCompletionUseCase(habitId = 1, date = today)
        assertTrue(repository.isHabitCompletedOnDate(1, "2026-11-05"))

        // Second toggle: un-completes it
        val result = toggleHabitCompletionUseCase(habitId = 1, date = today)

        assertFalse(result)
        assertFalse(repository.isHabitCompletedOnDate(1, "2026-11-05"))
        assertTrue(scheduler.scheduledHabitIds.contains(1L))
    }
}
