package com.mayur.habitstate.presentation.screens.main.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mayur.habitstate.data.local.database.HabitDatabase
import com.mayur.habitstate.data.reminder.HabitReminderSchedulerImpl
import com.mayur.habitstate.data.repository.HabitRepositoryImpl
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.usecase.AddHabitUseCase
import com.mayur.habitstate.domain.usecase.CalculateHabitScoreUseCase
import com.mayur.habitstate.domain.usecase.CalculateStreakUseCase
import com.mayur.habitstate.domain.usecase.DeleteHabitUseCase
import com.mayur.habitstate.domain.usecase.GetTodayHabitsUseCase
import com.mayur.habitstate.domain.usecase.GetWeekCompletionUseCase
import com.mayur.habitstate.domain.usecase.ToggleHabitCompletionUseCase
import com.mayur.habitstate.domain.usecase.UpdateHabitUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    application: Application,
    private val getTodayHabitsUseCase: GetTodayHabitsUseCase,
    private val getWeekCompletionUseCase: GetWeekCompletionUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val addHabitUseCase: AddHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val calculateHabitScoreUseCase: CalculateHabitScoreUseCase
) : AndroidViewModel(application) {

    /**
     * Fallback secondary constructor when instantiated directly via default ViewModelProvider.Factory
     * without Hilt injection. This ensures zero friction with the current navigation setup.
     */
    constructor(application: Application) : this(
        application = application,
        getTodayHabitsUseCase = createDefaultGetTodayHabitsUseCase(application),
        getWeekCompletionUseCase = createDefaultGetWeekCompletionUseCase(application),
        toggleHabitCompletionUseCase = createDefaultToggleHabitCompletionUseCase(application),
        addHabitUseCase = createDefaultAddHabitUseCase(application),
        updateHabitUseCase = createDefaultUpdateHabitUseCase(application),
        deleteHabitUseCase = createDefaultDeleteHabitUseCase(application),
        calculateHabitScoreUseCase = createDefaultCalculateHabitScoreUseCase(application)
    )

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val initialDate: LocalDate = LocalDate.now()
    private val _state = MutableStateFlow(
        HomeState(
            isLoading = true,
            selectedDate = initialDate,
            weekDates = computeWeekDates(initialDate)
        )
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var habitsObservationJob: Job? = null
    private var weekCompletionJob: Job? = null

    init {
        loadUserData()
        observeHabitsForDate(_state.value.selectedDate)
        observeWeekCompletion(_state.value.weekDates)
        observeHabitScore()
    }

    fun loadUserData() {
        val currentUser = auth?.currentUser
        val name = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Habit Tracker"
        val email = currentUser?.email ?: ""

        _state.update {
            it.copy(
                userName = name,
                userEmail = email
            )
        }
    }

    fun selectDate(date: LocalDate) {
        if (_state.value.selectedDate == date) return

        val currentWeekDates = _state.value.weekDates
        val newWeekDates = if (date in currentWeekDates) {
            currentWeekDates
        } else {
            computeWeekDates(date)
        }

        _state.update {
            it.copy(
                selectedDate = date,
                weekDates = newWeekDates
            )
        }

        observeHabitsForDate(date)
        if (newWeekDates != currentWeekDates) {
            observeWeekCompletion(newWeekDates)
        }
    }

    fun previousWeek() {
        val firstDay = _state.value.weekDates.firstOrNull() ?: _state.value.selectedDate
        val newDate = firstDay.minusDays(7)
        selectDate(newDate)
    }

    fun nextWeek() {
        val firstDay = _state.value.weekDates.firstOrNull() ?: _state.value.selectedDate
        val newDate = firstDay.plusDays(7)
        selectDate(newDate)
    }

    fun resetToToday() {
        selectDate(LocalDate.now())
    }

    fun setFilter(filter: HabitListFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    private fun observeHabitsForDate(date: LocalDate) {
        habitsObservationJob?.cancel()
        habitsObservationJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getTodayHabitsUseCase(date)
                .catch { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage ?: "Failed to load habits"
                        )
                    }
                }
                .collect { habitsWithStatus ->
                    _state.update {
                        it.copy(
                            habits = habitsWithStatus,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun observeHabitScore() {
        viewModelScope.launch {
            calculateHabitScoreUseCase()
                .catch { /* keep last known score */ }
                .collect { summary ->
                    _state.update { it.copy(habitScore = summary.overallScore) }
                }
        }
    }

    private fun observeWeekCompletion(dates: List<LocalDate>) {
        weekCompletionJob?.cancel()
        weekCompletionJob = viewModelScope.launch {
            getWeekCompletionUseCase(dates)
                .catch { /* ignore error in week completion */ }
                .collect { statusMap ->
                    _state.update { it.copy(weekCompletionStatus = statusMap) }
                }
        }
    }

    fun toggleHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            try {
                toggleHabitCompletionUseCase(habitId, _state.value.selectedDate)
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Failed to update habit")
                }
            }
        }
    }

    fun addHabit(
        name: String,
        description: String = "",
        iconName: String = "fitness",
        colorHex: Long = 0xFF4CAF50,
        category: String = "General",
        timeOfDay: String = "ANYTIME",
        frequencyDays: Set<Int> = (1..7).toSet(),
        reminderHour: Int? = null,
        reminderMinute: Int? = null,
        isReminderEnabled: Boolean = false,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                iconName = iconName,
                colorHex = colorHex,
                category = category,
                timeOfDay = timeOfDay,
                frequencyDays = frequencyDays,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                isReminderEnabled = isReminderEnabled,
                isActive = isActive
            )
            val result = addHabitUseCase(habit)
            if (result.isSuccess) {
                closeAddEditDialog()
            } else {
                _state.update {
                    it.copy(
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                            ?: "Failed to add habit"
                    )
                }
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            val result = updateHabitUseCase(habit)
            if (result.isSuccess) {
                closeAddEditDialog()
            } else {
                _state.update {
                    it.copy(
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                            ?: "Failed to update habit"
                    )
                }
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                deleteHabitUseCase(habit)
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Failed to delete habit")
                }
            }
        }
    }

    fun togglePauseHabit(habit: Habit) {
        val updated = habit.copy(isActive = !habit.isActive)
        updateHabit(updated)
    }

    fun openAddHabitDialog() {
        _state.update { it.copy(isAddEditDialogOpen = true, habitToEdit = null) }
    }

    fun openEditHabitDialog(habit: Habit) {
        _state.update { it.copy(isAddEditDialogOpen = true, habitToEdit = habit) }
    }

    fun closeAddEditDialog() {
        _state.update { it.copy(isAddEditDialogOpen = false, habitToEdit = null) }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun logout(onSuccess: () -> Unit) {
        try {
            auth?.signOut()
        } catch (_: Exception) {
        }
        onSuccess()
    }

    companion object {
        fun computeWeekDates(referenceDate: LocalDate): List<LocalDate> {
            val sunday = referenceDate.minusDays((referenceDate.dayOfWeek.value % 7).toLong())
            return (0L..6L).map { sunday.plusDays(it) }
        }
    }
}

private fun createDefaultGetTodayHabitsUseCase(application: Application): GetTodayHabitsUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    val streak = CalculateStreakUseCase()
    return GetTodayHabitsUseCase(repo, streak)
}

private fun createDefaultGetWeekCompletionUseCase(application: Application): GetWeekCompletionUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    return GetWeekCompletionUseCase(repo)
}

private fun createDefaultToggleHabitCompletionUseCase(application: Application): ToggleHabitCompletionUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    val scheduler = HabitReminderSchedulerImpl(application)
    return ToggleHabitCompletionUseCase(repo, scheduler)
}

private fun createDefaultAddHabitUseCase(application: Application): AddHabitUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    val scheduler = HabitReminderSchedulerImpl(application)
    return AddHabitUseCase(repo, scheduler)
}

private fun createDefaultUpdateHabitUseCase(application: Application): UpdateHabitUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    val scheduler = HabitReminderSchedulerImpl(application)
    return UpdateHabitUseCase(repo, scheduler)
}

private fun createDefaultDeleteHabitUseCase(application: Application): DeleteHabitUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    val scheduler = HabitReminderSchedulerImpl(application)
    return DeleteHabitUseCase(repo, scheduler)
}

private fun createDefaultCalculateHabitScoreUseCase(application: Application): CalculateHabitScoreUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    return CalculateHabitScoreUseCase(repo)
}
