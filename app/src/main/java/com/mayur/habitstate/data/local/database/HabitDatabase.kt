package com.mayur.habitstate.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mayur.habitstate.data.local.dao.HabitDao
import com.mayur.habitstate.data.local.dao.HabitLogDao
import com.mayur.habitstate.data.local.entity.HabitEntity
import com.mayur.habitstate.data.local.entity.HabitLogEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        const val DATABASE_NAME = "habit_state_database"

        @Volatile
        private var instance: HabitDatabase? = null

        fun getInstance(context: Context): HabitDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
