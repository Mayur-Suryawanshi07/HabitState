package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddHabitUseCaseTest {

    private lateinit var repository: FakeHabitRepository
    private lateinit var scheduler: FakeHabitReminderScheduler
    private lateinit var addHabitUseCase: AddHabitUseCase

    @Before
    fun setUp() {
        repository = FakeHabitRepository()
        scheduler = FakeHabitReminderScheduler()
        addHabitUseCase = AddHabitUseCase(repository, scheduler)
    }

    @Test
    fun `adding habit with blank name returns failure`() = runBlocking {
        val habit = Habit(name = "   ")
        val result = addHabitUseCase(habit)

        assertTrue(result.isFailure)
        assertEquals("Habit name cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun `adding active habit with reminder schedules reminder`() = runBlocking {
        val habit = Habit(
            name = "Morning Walk",
            reminderHour = 7,
            reminderMinute = 0,
            isReminderEnabled = true,
            isActive = true
        )

        val result = addHabitUseCase(habit)

        assertTrue(result.isSuccess)
        val habitId = result.getOrThrow()
        assertTrue(habitId > 0)
        assertTrue(scheduler.scheduledHabitIds.contains(habitId))
    }

    @Test
    fun `adding inactive habit does not schedule reminder`() = runBlocking {
        val habit = Habit(
            name = "Yoga",
            isReminderEnabled = true,
            isActive = false
        )

        val result = addHabitUseCase(habit)

        assertTrue(result.isSuccess)
        assertTrue(scheduler.scheduledHabitIds.isEmpty())
    }
}
