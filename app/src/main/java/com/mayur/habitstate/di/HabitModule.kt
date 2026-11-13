package com.mayur.habitstate.di

import android.content.Context
import com.mayur.habitstate.data.local.dao.HabitDao
import com.mayur.habitstate.data.local.dao.HabitLogDao
import com.mayur.habitstate.data.local.database.HabitDatabase
import com.mayur.habitstate.data.reminder.HabitNotificationManager
import com.mayur.habitstate.data.reminder.HabitReminderSchedulerImpl
import com.mayur.habitstate.data.repository.HabitRepositoryImpl
import com.mayur.habitstate.domain.repository.HabitRepository
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler
import com.mayur.habitstate.domain.usecase.AddHabitUseCase
import com.mayur.habitstate.domain.usecase.CalculateHabitScoreUseCase
import com.mayur.habitstate.domain.usecase.CalculateStreakUseCase
import com.mayur.habitstate.domain.usecase.DeleteHabitUseCase
import com.mayur.habitstate.domain.usecase.GetTodayHabitsUseCase
import com.mayur.habitstate.domain.usecase.ScheduleReminderUseCase
import com.mayur.habitstate.domain.usecase.ToggleHabitCompletionUseCase
import com.mayur.habitstate.domain.usecase.UpdateHabitUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HabitModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
        return HabitDatabase.getInstance(context)
    }

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideHabitLogDao(database: HabitDatabase): HabitLogDao {
        return database.habitLogDao()
    }

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        habitLogDao: HabitLogDao
    ): HabitRepository {
        return HabitRepositoryImpl(habitDao, habitLogDao)
    }

    @Provides
    @Singleton
    fun provideHabitReminderScheduler(@ApplicationContext context: Context): HabitReminderScheduler {
        return HabitReminderSchedulerImpl(context)
    }

    @Provides
    @Singleton
    fun provideHabitNotificationManager(@ApplicationContext context: Context): HabitNotificationManager {
        return HabitNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideCalculateStreakUseCase(): CalculateStreakUseCase {
        return CalculateStreakUseCase()
    }

    @Provides
    @Singleton
    fun provideGetTodayHabitsUseCase(
        repository: HabitRepository,
        calculateStreakUseCase: CalculateStreakUseCase
    ): GetTodayHabitsUseCase {
        return GetTodayHabitsUseCase(repository, calculateStreakUseCase)
    }

    @Provides
    @Singleton
    fun provideCalculateHabitScoreUseCase(
        repository: HabitRepository
    ): CalculateHabitScoreUseCase {
        return CalculateHabitScoreUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetWeekCompletionUseCase(
        repository: HabitRepository
    ): com.mayur.habitstate.domain.usecase.GetWeekCompletionUseCase {
        return com.mayur.habitstate.domain.usecase.GetWeekCompletionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideToggleHabitCompletionUseCase(
        repository: HabitRepository,
        reminderScheduler: HabitReminderScheduler
    ): ToggleHabitCompletionUseCase {
        return ToggleHabitCompletionUseCase(repository, reminderScheduler)
    }

    @Provides
    @Singleton
    fun provideAddHabitUseCase(
        repository: HabitRepository,
        reminderScheduler: HabitReminderScheduler
    ): AddHabitUseCase {
        return AddHabitUseCase(repository, reminderScheduler)
    }

    @Provides
    @Singleton
    fun provideUpdateHabitUseCase(
        repository: HabitRepository,
        reminderScheduler: HabitReminderScheduler
    ): UpdateHabitUseCase {
        return UpdateHabitUseCase(repository, reminderScheduler)
    }

    @Provides
    @Singleton
    fun provideDeleteHabitUseCase(
        repository: HabitRepository,
        reminderScheduler: HabitReminderScheduler
    ): DeleteHabitUseCase {
        return DeleteHabitUseCase(repository, reminderScheduler)
    }

    @Provides
    @Singleton
    fun provideScheduleReminderUseCase(
        reminderScheduler: HabitReminderScheduler
    ): ScheduleReminderUseCase {
        return ScheduleReminderUseCase(reminderScheduler)
    }
}
