package com.mayur.habitstate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mayur.habitstate.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getLogsForHabitOnce(habitId: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date IN (:dates)")
    fun getLogsForDates(dates: List<String>): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date IN (:dates)")
    suspend fun getLogsForDatesOnce(dates: List<String>): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM habit_logs WHERE habitId = :habitId AND date = :date)")
    suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLogForDate(habitId: Long, date: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}
