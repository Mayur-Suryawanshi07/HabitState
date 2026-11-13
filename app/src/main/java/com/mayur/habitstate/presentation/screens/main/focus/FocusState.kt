package com.mayur.habitstate.presentation.screens.main.focus

import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap? = null,
    val isDistracting: Boolean = false
)

data class FocusSchedule(
    val isEnabled: Boolean = false,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val activeDays: Set<Int> = setOf(2, 3, 4, 5, 6) // 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
)

data class FocusState(
    val isFocusActive: Boolean = false,
    val isOnBreak: Boolean = false,
    val breakRemainingSeconds: Long = 0L,
    val focusSessionSeconds: Long = 0L,
    val installedApps: List<AppInfo> = emptyList(),
    val distractingAppPackages: Set<String> = emptySet(),
    val schedule: FocusSchedule = FocusSchedule(),
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayPermissionEnabled: Boolean = false,
    val isScheduleDialogOpen: Boolean = false,
    val isBreakDialogOpen: Boolean = false,
    val isLoadingApps: Boolean = true,
    val appSearchQuery: String = ""
)
