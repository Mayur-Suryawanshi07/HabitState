package com.mayur.habitstate.presentation.screens.main.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitItem(
    val title: String,
    val category: String,
    val streak: Int,
    val isCompleted: Boolean,
    val icon: ImageVector
)

val sampleHabits = listOf(
    HabitItem("Morning Hydration", "Health", 7, true, Icons.Default.WaterDrop),
    HabitItem("30-Min Daily Reading", "Mindset", 12, false, Icons.Default.SelfImprovement),
    HabitItem("Core Workout", "Fitness", 5, true, Icons.Default.LocalFireDepartment)
)

data class HomeState(
    val userName: String = "Habit Tracker",
    val userEmail: String = "",
    val habits: List<HabitItem> = emptyList(),
    val isLoading: Boolean = false
)
