package com.mayur.habitstate.presentation.screens.main.habit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mayur.habitstate.data.local.database.HabitDatabase
import com.mayur.habitstate.data.reminder.HabitReminderSchedulerImpl
import com.mayur.habitstate.data.repository.HabitRepositoryImpl
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitFrequencyType
import com.mayur.habitstate.domain.repository.HabitRepository
import com.mayur.habitstate.domain.usecase.AddHabitUseCase
import com.mayur.habitstate.domain.usecase.UpdateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    application: Application,
    private val repository: HabitRepository,
    private val addHabitUseCase: AddHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase
) : AndroidViewModel(application) {

    /**
     * Fallback secondary constructor when instantiated directly via default ViewModelProvider.Factory
     */
    constructor(application: Application) : this(
        application = application,
        repository = createDefaultHabitRepository(application),
        addHabitUseCase = createDefaultAddHabitUseCase(application),
        updateHabitUseCase = createDefaultUpdateHabitUseCase(application)
    )

    private val _state = MutableStateFlow(AddEditHabitState())
    val state: StateFlow<AddEditHabitState> = _state.asStateFlow()

    private var loadedHabitId: Long? = null

    fun loadHabit(habitId: Long) {
        if (loadedHabitId == habitId) return
        loadedHabitId = habitId

        if (habitId <= 0) {
            _state.update {
                it.copy(
                    habitId = -1L,
                    isEditMode = false,
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, habitId = habitId, isEditMode = true) }
            val habit = repository.getHabitByIdOnce(habitId)
            if (habit != null) {
                _state.update {
                    it.copy(
                        habitId = habit.id,
                        name = habit.name,
                        description = habit.description,
                        category = habit.category,
                        timeOfDay = habit.timeOfDay,
                        selectedColor = habit.colorHex,
                        selectedIcon = habit.iconName,
                        frequencyType = habit.frequencyType,
                        frequencyDays = habit.frequencyDays,
                        periodTarget = habit.periodTarget,
                        periodLengthDays = habit.periodLengthDays,
                        isReminderEnabled = habit.isReminderEnabled,
                        reminderHour = habit.reminderHour ?: 8,
                        reminderMinute = habit.reminderMinute ?: 0,
                        isActive = habit.isActive,
                        isLoading = false,
                        isEditMode = true,
                        nameError = null,
                        generalError = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Habit not found"
                    )
                }
            }
        }
    }

    fun onNameChanged(newName: String) {
        _state.update {
            it.copy(
                name = newName,
                nameError = if (newName.isNotBlank()) null else it.nameError
            )
        }
    }

    fun onDescriptionChanged(newDesc: String) {
        _state.update { it.copy(description = newDesc) }
    }

    fun onCategorySelected(newCategory: String) {
        _state.update { it.copy(category = newCategory) }
    }

    fun onHabitKindSelected(kind: HabitKind) {
        _state.update { it.copy(habitKind = kind) }
    }

    fun onTimeOfDaySelected(timeOfDay: String) {
        _state.update { it.copy(timeOfDay = timeOfDay) }
    }

    fun onColorSelected(colorHex: Long) {
        _state.update { it.copy(selectedColor = colorHex) }
    }

    fun onIconSelected(iconName: String) {
        _state.update { it.copy(selectedIcon = iconName) }
    }

    fun onFrequencyTypeSelected(type: HabitFrequencyType) {
        _state.update { current ->
            val days = when (type) {
                HabitFrequencyType.EVERYDAY -> (1..7).toSet()
                HabitFrequencyType.WEEK_DAYS ->
                    if (current.frequencyType == HabitFrequencyType.WEEK_DAYS) current.frequencyDays
                    else current.frequencyDays.filter { it in 1..7 }.toSet().ifEmpty { (1..7).toSet() }
                HabitFrequencyType.MONTH_DAYS ->
                    if (current.frequencyType == HabitFrequencyType.MONTH_DAYS) current.frequencyDays
                    else emptySet()
                HabitFrequencyType.PERIOD -> emptySet()
            }
            current.copy(frequencyType = type, frequencyDays = days)
        }
    }

    fun toggleMonthDay(day: Int) {
        _state.update { current ->
            val updated = if (current.frequencyDays.contains(day)) {
                if (current.frequencyDays.size > 1) current.frequencyDays - day else current.frequencyDays
            } else {
                current.frequencyDays + day
            }
            current.copy(frequencyDays = updated)
        }
    }

    fun onPeriodTargetChanged(target: Int) {
        _state.update { it.copy(periodTarget = target.coerceIn(1, 30)) }
    }

    fun onPeriodLengthChanged(length: Int) {
        _state.update { it.copy(periodLengthDays = length.coerceIn(1, 90)) }
    }

    fun toggleDay(day: Int) {
        _state.update { current ->
            val updated = if (current.frequencyDays.contains(day)) {
                // Keep at least one day selected
                if (current.frequencyDays.size > 1) current.frequencyDays - day else current.frequencyDays
            } else {
                current.frequencyDays + day
            }
            current.copy(frequencyDays = updated)
        }
    }

    fun setAllDays() {
        _state.update { it.copy(frequencyDays = (1..7).toSet()) }
    }

    fun setWeekdays() {
        _state.update { it.copy(frequencyDays = (1..5).toSet()) }
    }

    fun setWeekends() {
        _state.update { it.copy(frequencyDays = setOf(6, 7)) }
    }

    fun onReminderEnabledChanged(enabled: Boolean) {
        _state.update { it.copy(isReminderEnabled = enabled) }
    }

    fun onReminderTimeChanged(hour: Int, minute: Int) {
        _state.update { it.copy(reminderHour = hour, reminderMinute = minute) }
    }

    fun onActiveToggled(active: Boolean) {
        _state.update { it.copy(isActive = active) }
    }

    fun applyPreset(preset: HabitPresetItem) {
        _state.update {
            it.copy(
                name = preset.title,
                category = preset.category,
                selectedIcon = preset.iconName,
                selectedColor = preset.colorHex,
                timeOfDay = preset.timeOfDay,
                reminderHour = preset.defaultHour,
                reminderMinute = 0,
                nameError = null
            )
        }
    }

    fun saveHabit() {
        val currentState = _state.value
        val trimmedName = currentState.name.trim()
        if (trimmedName.isBlank()) {
            _state.update { it.copy(nameError = "Habit name cannot be blank") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, generalError = null) }

            val habit = Habit(
                id = if (currentState.isEditMode) currentState.habitId else 0,
                name = trimmedName,
                description = currentState.description.trim(),
                iconName = currentState.selectedIcon,
                colorHex = currentState.selectedColor,
                category = currentState.category,
                timeOfDay = currentState.timeOfDay,
                frequencyType = currentState.frequencyType,
                frequencyDays = if (currentState.frequencyDays.isEmpty() && currentState.frequencyType != HabitFrequencyType.PERIOD) {
                    (1..7).toSet()
                } else {
                    currentState.frequencyDays
                },
                periodTarget = currentState.periodTarget,
                periodLengthDays = currentState.periodLengthDays,
                reminderHour = if (currentState.isReminderEnabled) currentState.reminderHour else null,
                reminderMinute = if (currentState.isReminderEnabled) currentState.reminderMinute else null,
                isReminderEnabled = currentState.isReminderEnabled,
                isActive = currentState.isActive
            )

            val result = if (currentState.isEditMode) {
                updateHabitUseCase(habit)
            } else {
                addHabitUseCase(habit).map { }
            }

            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccessfully = true,
                        generalError = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isSaving = false,
                        generalError = result.exceptionOrNull()?.localizedMessage
                            ?: "Failed to save habit"
                    )
                }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(generalError = null) }
    }
}

private fun createDefaultHabitRepository(application: Application): HabitRepository {
    val db = HabitDatabase.getInstance(application)
    return HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
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
