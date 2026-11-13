package com.mayur.habitstate.data.repository

import com.mayur.habitstate.data.local.dao.HabitDao
import com.mayur.habitstate.data.local.dao.HabitLogDao
import com.mayur.habitstate.data.local.mapper.toDomain
import com.mayur.habitstate.data.local.mapper.toEntity
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog
import com.mayur.habitstate.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveHabits(): Flow<List<Habit>> {
        return habitDao.getActiveHabits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHabitById(id: Long): Flow<Habit?> {
        return habitDao.getHabitById(id).map { it?.toDomain() }
    }

    override suspend fun getHabitByIdOnce(id: Long): Habit? {
        return habitDao.getHabitByIdOnce(id)?.toDomain()
    }

    override suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit.toEntity())
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.toEntity())
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit.toEntity())
    }

    override fun getHabitLogs(habitId: Long): Flow<List<HabitLog>> {
        return habitLogDao.getLogsForHabit(habitId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHabitLogsOnce(habitId: Long): List<HabitLog> {
        return habitLogDao.getLogsForHabitOnce(habitId).map { it.toDomain() }
    }

    override fun getHabitLogsForDate(date: String): Flow<List<HabitLog>> {
        return habitLogDao.getLogsForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHabitLogsForDates(dates: List<String>): Flow<List<HabitLog>> {
        return habitLogDao.getLogsForDates(dates).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHabitLogsForDatesOnce(dates: List<String>): List<HabitLog> {
        return habitLogDao.getLogsForDatesOnce(dates).map { it.toDomain() }
    }

    override fun getAllLogs(): Flow<List<HabitLog>> {
        return habitLogDao.getAllLogs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertHabitLog(log: HabitLog): Long {
        return habitLogDao.insertLog(log.toEntity())
    }

    override suspend fun deleteHabitLogForDate(habitId: Long, date: String) {
        habitLogDao.deleteLogForDate(habitId, date)
    }

    override suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean {
        return habitLogDao.isHabitCompletedOnDate(habitId, date)
    }
}
