package com.mayur.habitstate.data.local.mapper

import com.mayur.habitstate.data.local.entity.HabitEntity
import com.mayur.habitstate.data.local.entity.HabitLogEntity
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitLog

fun HabitEntity.toDomain(): Habit {
    val decoded = Habit.decodeFrequency(frequencyDays)

    return Habit(
        id = id,
        name = name,
        description = description,
        iconName = iconName,
        colorHex = colorHex,
        category = category,
        timeOfDay = timeOfDay,
        frequencyType = decoded.type,
        frequencyDays = decoded.days,
        periodTarget = decoded.periodTarget,
        periodLengthDays = decoded.periodLengthDays,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        isReminderEnabled = isReminderEnabled,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun Habit.toEntity(): HabitEntity {
    val frequencyString = encodeFrequency()

    return HabitEntity(
        id = id,
        name = name,
        description = description,
        iconName = iconName,
        colorHex = colorHex,
        category = category,
        timeOfDay = timeOfDay,
        frequencyDays = frequencyString,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        isReminderEnabled = isReminderEnabled,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun HabitLogEntity.toDomain(): HabitLog {
    return HabitLog(
        id = id,
        habitId = habitId,
        date = date,
        completedAt = completedAt
    )
}

fun HabitLog.toEntity(): HabitLogEntity {
    return HabitLogEntity(
        id = id,
        habitId = habitId,
        date = date,
        completedAt = completedAt
    )
}
