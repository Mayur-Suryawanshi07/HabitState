package com.mayur.habitstate.presentation.screens.main.home

import android.app.Application
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.usecase.AddHabitUseCase
import com.mayur.habitstate.domain.usecase.CalculateStreakUseCase
import com.mayur.habitstate.domain.usecase.DeleteHabitUseCase
import com.mayur.habitstate.domain.usecase.FakeHabitReminderScheduler
import com.mayur.habitstate.domain.usecase.FakeHabitRepository
import com.mayur.habitstate.domain.usecase.GetTodayHabitsUseCase
import com.mayur.habitstate.domain.usecase.GetWeekCompletionUseCase
import com.mayur.habitstate.domain.usecase.ToggleHabitCompletionUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: FakeHabitRepository
    private lateinit var scheduler: FakeHabitReminderScheduler
    private lateinit var getTodayHabitsUseCase: GetTodayHabitsUseCase
    private lateinit var getWeekCompletionUseCase: GetWeekCompletionUseCase
    private lateinit var toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
    private lateinit var addHabitUseCase: AddHabitUseCase
    private lateinit var updateHabitUseCase: UpdateHabitUseCase
    private lateinit var deleteHabitUseCase: DeleteHabitUseCase
    private lateinit var calculateHabitScoreUseCase: com.mayur.habitstate.domain.usecase.CalculateHabitScoreUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHabitRepository()
        scheduler = FakeHabitReminderScheduler()
        val streak = CalculateStreakUseCase()

        getTodayHabitsUseCase = GetTodayHabitsUseCase(repository, streak)
        getWeekCompletionUseCase = GetWeekCompletionUseCase(repository)
        toggleHabitCompletionUseCase = ToggleHabitCompletionUseCase(repository, scheduler)
        addHabitUseCase = AddHabitUseCase(repository, scheduler)
        updateHabitUseCase = UpdateHabitUseCase(repository, scheduler)
        deleteHabitUseCase = DeleteHabitUseCase(repository, scheduler)
        calculateHabitScoreUseCase = com.mayur.habitstate.domain.usecase.CalculateHabitScoreUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        val mockApp = object : Application() {}
        return HomeViewModel(
            application = mockApp,
            getTodayHabitsUseCase = getTodayHabitsUseCase,
            getWeekCompletionUseCase = getWeekCompletionUseCase,
            toggleHabitCompletionUseCase = toggleHabitCompletionUseCase,
            addHabitUseCase = addHabitUseCase,
            updateHabitUseCase = updateHabitUseCase,
            deleteHabitUseCase = deleteHabitUseCase,
            calculateHabitScoreUseCase = calculateHabitScoreUseCase
        )
    }

    @Test
    fun `initial state shows loading then emits habits from repository`() = runTest {
        val habit = Habit(id = 1, name = "Morning Journal", frequencyDays = (1..7).toSet())
        repository.insertHabit(habit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.habits.size)
        assertEquals("Morning Journal", state.habits.first().habit.name)
        assertEquals(0, state.completedCount)
        assertEquals(1, state.totalCount)
        assertEquals(0f, state.progressFraction)
    }

    @Test
    fun `toggle completion updates habit status and progress fraction`() = runTest {
        val habit = Habit(id = 1, name = "Read 10 Pages", frequencyDays = (1..7).toSet())
        repository.insertHabit(habit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.completedCount)

        viewModel.toggleHabitCompletion(1)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.completedCount)
        assertEquals(1.0f, viewModel.state.value.progressFraction)
        assertTrue(viewModel.state.value.habits.first().isCompletedToday)
    }

    @Test
    fun `filter filters habits correctly`() = runTest {
        val habit1 = Habit(id = 1, name = "Habit 1", frequencyDays = (1..7).toSet())
        val habit2 = Habit(id = 2, name = "Habit 2", frequencyDays = (1..7).toSet())

        repository.insertHabit(habit1)
        repository.insertHabit(habit2)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.filteredHabits.size)

        viewModel.toggleHabitCompletion(1)
        advanceUntilIdle()

        viewModel.setFilter(HabitListFilter.DUE)
        assertEquals(1, viewModel.state.value.filteredHabits.size)
        assertEquals("Habit 2", viewModel.state.value.filteredHabits.first().habit.name)

        viewModel.setFilter(HabitListFilter.ALL)
        assertEquals(2, viewModel.state.value.filteredHabits.size)
    }

    @Test
    fun `selectDate updates selectedDate and observes habits for that date`() = runTest {
        val monday = LocalDate.of(2026, 11, 9)
        val sunday = LocalDate.of(2026, 11, 8)

        val mondayOnlyHabit = Habit(
            id = 1,
            name = "Monday Sprint",
            frequencyDays = setOf(1) // Monday only
        )
        repository.insertHabit(mondayOnlyHabit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectDate(sunday)
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.habits.size)

        viewModel.selectDate(monday)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.habits.size)
        assertEquals("Monday Sprint", viewModel.state.value.habits.first().habit.name)
    }

    @Test
    fun `addHabit with blank name sets error message`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addHabit(name = "   ")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `addHabit with valid data closes dialog and adds to habits list`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.openAddHabitDialog()
        assertTrue(viewModel.state.value.isAddEditDialogOpen)

        viewModel.addHabit(
            name = "Evening Run",
            category = "Fitness",
            timeOfDay = "EVENING"
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAddEditDialogOpen)
        assertEquals(1, viewModel.state.value.habits.size)
        assertEquals("Evening Run", viewModel.state.value.habits.first().habit.name)
        assertEquals("Fitness", viewModel.state.value.habits.first().habit.category)
        assertEquals("EVENING", viewModel.state.value.habits.first().habit.timeOfDay)
    }
}
