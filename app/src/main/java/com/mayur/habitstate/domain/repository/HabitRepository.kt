package com.mayur.habitstate.domain.repository

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for managing habits and habit logs.
 */
interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getActiveHabits(): Flow<List<Habit>>
    fun getHabitById(id: Long): Flow<Habit?>
    suspend fun getHabitByIdOnce(id: Long): Habit?
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)

    fun getHabitLogs(habitId: Long): Flow<List<HabitLog>>
    suspend fun getHabitLogsOnce(habitId: Long): List<HabitLog>
    fun getHabitLogsForDate(date: String): Flow<List<HabitLog>>
    fun getHabitLogsForDates(dates: List<String>): Flow<List<HabitLog>>
    suspend fun getHabitLogsForDatesOnce(dates: List<String>): List<HabitLog>
    fun getAllLogs(): Flow<List<HabitLog>>
    suspend fun insertHabitLog(log: HabitLog): Long
    suspend fun deleteHabitLogForDate(habitId: Long, date: String)
    suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean
}
