package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog
import com.mayur.habitstate.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeHabitRepository : HabitRepository {

    private val habitsFlow = MutableStateFlow<List<Habit>>(emptyList())
    private val logsFlow = MutableStateFlow<List<HabitLog>>(emptyList())
    private var nextId = 1L

    override fun getAllHabits(): Flow<List<Habit>> = habitsFlow

    override fun getActiveHabits(): Flow<List<Habit>> =
        habitsFlow.map { habits -> habits.filter { it.isActive } }

    override fun getHabitById(id: Long): Flow<Habit?> =
        habitsFlow.map { habits -> habits.firstOrNull { it.id == id } }

    override suspend fun getHabitByIdOnce(id: Long): Habit? =
        habitsFlow.value.firstOrNull { it.id == id }

    override suspend fun insertHabit(habit: Habit): Long {
        val id = if (habit.id > 0) habit.id else nextId++
        val saved = habit.copy(id = id)
        habitsFlow.value = habitsFlow.value.filter { it.id != id } + saved
        return id
    }

    override suspend fun updateHabit(habit: Habit) {
        habitsFlow.value = habitsFlow.value.map { if (it.id == habit.id) habit else it }
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitsFlow.value = habitsFlow.value.filter { it.id != habit.id }
        logsFlow.value = logsFlow.value.filter { it.habitId != habit.id }
    }

    override fun getHabitLogs(habitId: Long): Flow<List<HabitLog>> =
        logsFlow.map { logs -> logs.filter { it.habitId == habitId } }

    override suspend fun getHabitLogsOnce(habitId: Long): List<HabitLog> =
        logsFlow.value.filter { it.habitId == habitId }

    override fun getHabitLogsForDate(date: String): Flow<List<HabitLog>> =
        logsFlow.map { logs -> logs.filter { it.date == date } }

    override fun getHabitLogsForDates(dates: List<String>): Flow<List<HabitLog>> =
        logsFlow.map { logs -> logs.filter { it.date in dates } }

    override suspend fun getHabitLogsForDatesOnce(dates: List<String>): List<HabitLog> =
        logsFlow.value.filter { it.date in dates }

    override fun getAllLogs(): Flow<List<HabitLog>> = logsFlow

    override suspend fun insertHabitLog(log: HabitLog): Long {
        val id = if (log.id > 0) log.id else nextId++
        val saved = log.copy(id = id)
        logsFlow.value = logsFlow.value + saved
        return id
    }

    override suspend fun deleteHabitLogForDate(habitId: Long, date: String) {
        logsFlow.value = logsFlow.value.filterNot { it.habitId == habitId && it.date == date }
    }

    override suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean =
        logsFlow.value.any { it.habitId == habitId && it.date == date }
}
