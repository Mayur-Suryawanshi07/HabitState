package com.mayur.habitstate.presentation.screens.main.score

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mayur.habitstate.data.local.database.HabitDatabase
import com.mayur.habitstate.data.repository.HabitRepositoryImpl
import com.mayur.habitstate.domain.usecase.CalculateHabitScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class HabitScoreViewModel @Inject constructor(
    application: Application,
    private val calculateHabitScoreUseCase: CalculateHabitScoreUseCase
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        calculateHabitScoreUseCase = createDefaultCalculateHabitScoreUseCase(application)
    )

    private val _state = MutableStateFlow(HabitScoreState())
    val state: StateFlow<HabitScoreState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            calculateHabitScoreUseCase()
                .catch { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage ?: "Failed to load scores"
                        )
                    }
                }
                .collect { summary ->
                    _state.update {
                        it.copy(
                            summary = summary,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }
}

private fun createDefaultCalculateHabitScoreUseCase(application: Application): CalculateHabitScoreUseCase {
    val db = HabitDatabase.getInstance(application)
    val repo = HabitRepositoryImpl(db.habitDao(), db.habitLogDao())
    return CalculateHabitScoreUseCase(repo)
}
