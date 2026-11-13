package com.mayur.habitstate.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconName: String,
    val colorHex: Long,
    val frequencyDays: String, // Comma separated, e.g. "1,2,3,4,5,6,7"
    val reminderHour: Int?,
    val reminderMinute: Int?,
    val isReminderEnabled: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "General")
    val category: String = "General",
    @ColumnInfo(defaultValue = "ANYTIME")
    val timeOfDay: String = "ANYTIME"
)
