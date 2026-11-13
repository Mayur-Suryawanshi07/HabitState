package com.mayur.habitstate.presentation.screens.main.habit

import android.app.Application
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.usecase.AddHabitUseCase
import com.mayur.habitstate.domain.usecase.FakeHabitReminderScheduler
import com.mayur.habitstate.domain.usecase.FakeHabitRepository
import com.mayur.habitstate.domain.usecase.UpdateHabitUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditHabitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: FakeHabitRepository
    private lateinit var scheduler: FakeHabitReminderScheduler
    private lateinit var addHabitUseCase: AddHabitUseCase
    private lateinit var updateHabitUseCase: UpdateHabitUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHabitRepository()
        scheduler = FakeHabitReminderScheduler()
        addHabitUseCase = AddHabitUseCase(repository, scheduler)
        updateHabitUseCase = UpdateHabitUseCase(repository, scheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AddEditHabitViewModel {
        val mockApp = object : Application() {}
        return AddEditHabitViewModel(
            application = mockApp,
            repository = repository,
            addHabitUseCase = addHabitUseCase,
            updateHabitUseCase = updateHabitUseCase
        )
    }

    @Test
    fun `initial state defaults to new habit mode with id -1`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadHabit(-1L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(-1L, state.habitId)
        assertFalse(state.isEditMode)
        assertFalse(state.isSavedSuccessfully)
        assertNull(state.nameError)
    }

    @Test
    fun `loadHabit with valid existing habit populates state in edit mode`() = runTest {
        val habit = Habit(
            id = 42L,
            name = "Morning Stretch",
            description = "15 mins every day",
            category = "Fitness",
            timeOfDay = "MORNING",
            colorHex = 0xFF2196F3,
            iconName = "fitness",
            frequencyDays = setOf(1, 3, 5),
            isReminderEnabled = true,
            reminderHour = 7,
            reminderMinute = 30
        )
        repository.insertHabit(habit)

        val viewModel = createViewModel()
        viewModel.loadHabit(42L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(42L, state.habitId)
        assertTrue(state.isEditMode)
        assertEquals("Morning Stretch", state.name)
        assertEquals("15 mins every day", state.description)
        assertEquals("Fitness", state.category)
        assertEquals("MORNING", state.timeOfDay)
        assertEquals(0xFF2196F3, state.selectedColor)
        assertEquals(setOf(1, 3, 5), state.frequencyDays)
        assertTrue(state.isReminderEnabled)
        assertEquals(7, state.reminderHour)
        assertEquals(30, state.reminderMinute)
    }

    @Test
    fun `loadHabit with non-existent habit id sets error`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadHabit(999L)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.generalError)
        assertEquals("Habit not found", state.generalError)
    }

    @Test
    fun `saveHabit with blank name sets nameError and does not save`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadHabit(-1L)
        advanceUntilIdle()

        viewModel.onNameChanged("   ")
        viewModel.saveHabit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.nameError)
        assertEquals("Habit name cannot be blank", state.nameError)
        assertFalse(state.isSavedSuccessfully)
    }

    @Test
    fun `saveHabit creates new habit and marks isSavedSuccessfully`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadHabit(-1L)
        advanceUntilIdle()

        viewModel.onNameChanged("Drink 2L Water")
        viewModel.onCategorySelected("Health")
        viewModel.onTimeOfDaySelected("MORNING")
        viewModel.onReminderEnabledChanged(true)
        viewModel.onReminderTimeChanged(8, 0)
        viewModel.saveHabit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isSavedSuccessfully)
        assertNull(state.generalError)

        val inserted = repository.getHabitByIdOnce(1L)
        assertNotNull(inserted)
        assertEquals("Drink 2L Water", inserted?.name)
        assertEquals("Health", inserted?.category)
        assertEquals("MORNING", inserted?.timeOfDay)
    }

    @Test
    fun `saveHabit in edit mode updates existing habit`() = runTest {
        val habit = Habit(id = 10L, name = "Old Name", category = "General")
        repository.insertHabit(habit)

        val viewModel = createViewModel()
        viewModel.loadHabit(10L)
        advanceUntilIdle()

        viewModel.onNameChanged("Updated Habit Name")
        viewModel.onCategorySelected("Productivity")
        viewModel.saveHabit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isSavedSuccessfully)

        val updated = repository.getHabitByIdOnce(10L)
        assertNotNull(updated)
        assertEquals("Updated Habit Name", updated?.name)
        assertEquals("Productivity", updated?.category)
    }

    @Test
    fun `applyPreset updates form fields`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadHabit(-1L)
        advanceUntilIdle()

        val preset = HabitPresetItem(
            title = "Read 30 Mins",
            category = "Learning",
            iconName = "book",
            colorHex = 0xFFFF9800,
            timeOfDay = "EVENING",
            defaultHour = 20
        )
        viewModel.applyPreset(preset)

        val state = viewModel.state.value
        assertEquals("Read 30 Mins", state.name)
        assertEquals("Learning", state.category)
        assertEquals("book", state.selectedIcon)
        assertEquals(0xFFFF9800, state.selectedColor)
        assertEquals("EVENING", state.timeOfDay)
        assertEquals(20, state.reminderHour)
    }

    @Test
    fun `toggleDay modifies frequencyDays correctly`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadHabit(-1L)
        advanceUntilIdle()

        // Defaults to all 7 days (1..7)
        assertEquals((1..7).toSet(), viewModel.state.value.frequencyDays)

        // Toggle Monday off
        viewModel.toggleDay(1)
        assertEquals((2..7).toSet(), viewModel.state.value.frequencyDays)

        // Toggle Monday back on
        viewModel.toggleDay(1)
        assertEquals((1..7).toSet(), viewModel.state.value.frequencyDays)
    }
}
