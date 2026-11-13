package com.mayur.habitstate.domain.usecase

import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.scheduler.HabitReminderScheduler

class FakeHabitReminderScheduler : HabitReminderScheduler {
    val scheduledHabitIds = mutableListOf<Long>()
    val canceledHabitIds = mutableListOf<Long>()
    var rescheduleAllCount = 0

    override fun scheduleReminder(habit: Habit) {
        scheduledHabitIds.add(habit.id)
    }

    override fun cancelReminder(habitId: Long) {
        canceledHabitIds.add(habitId)
    }

    override suspend fun rescheduleAllReminders() {
        rescheduleAllCount++
    }
}
