package com.mayur.habitstate.presentation.screens.main.focus

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mayur.habitstate.data.focus.FocusNotificationManager
import com.mayur.habitstate.data.focus.FocusPreferencesRepository
import com.mayur.habitstate.service.FocusAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FocusPreferencesRepository(application)
    private val notificationManager = FocusNotificationManager(application)

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private val focusStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadPreferences()
        }
    }

    init {
        registerBroadcastReceiver()
        loadData()
        startTimerTicker()
    }

    private fun registerBroadcastReceiver() {
        val app = getApplication<Application>()
        val filter = IntentFilter(FocusNotificationManager.ACTION_FOCUS_STATE_CHANGED)
        androidx.core.content.ContextCompat.registerReceiver(
            app,
            focusStateReceiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(focusStateReceiver)
        } catch (_: Exception) {}
    }

    fun loadData() {
        checkAccessibilityPermission()
        loadPreferences()
        loadInstalledApps()
    }

    fun checkAccessibilityPermission() {
        val app = getApplication<Application>()
        val accessibilityEnabled = FocusAccessibilityService.isServiceEnabled(app)
        val overlayEnabled = android.provider.Settings.canDrawOverlays(app)
        _state.update {
            it.copy(
                isAccessibilityEnabled = accessibilityEnabled,
                isOverlayPermissionEnabled = overlayEnabled
            )
        }
    }

    private fun loadPreferences() {
        val active = repository.isFocusActive()
        val distracting = repository.getDistractingApps()
        val schedule = repository.getSchedule()
        val onBreak = repository.isOnBreak()

        _state.update {
            it.copy(
                isFocusActive = active,
                distractingAppPackages = distracting,
                schedule = schedule,
                isOnBreak = onBreak
            )
        }

        if (active) {
            val breakRemMin = if (onBreak) {
                val breakEnd = repository.getBreakEndTimestamp()
                val remMs = breakEnd - System.currentTimeMillis()
                if (remMs > 0) (remMs / 60000).toInt() + 1 else 0
            } else 0
            updateNotificationState(isFocusActive = true, isOnBreak = onBreak, breakRemainingMinutes = breakRemMin)
        } else {
            notificationManager.dismissFocusNotification()
        }
    }

    private fun updateNotificationState(
        isFocusActive: Boolean,
        isOnBreak: Boolean,
        breakRemainingMinutes: Int = 0
    ) {
        if (isFocusActive) {
            val distractingCount = repository.getDistractingApps().size
            notificationManager.showFocusActiveNotification(
                isOnBreak = isOnBreak,
                breakRemainingMinutes = breakRemainingMinutes,
                distractingAppsCount = distractingCount
            )
        } else {
            notificationManager.dismissFocusNotification()
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoadingApps = true) }

            val context = getApplication<Application>()
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = try {
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            } catch (_: Exception) {
                emptyList()
            }

            val distractingPackages = repository.getDistractingApps()

            val apps = resolveInfos.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == context.packageName) return@mapNotNull null

                val appName = try {
                    resolveInfo.loadLabel(pm).toString()
                } catch (_: Exception) {
                    pkgName
                }

                val iconBitmap = try {
                    drawableToBitmap(resolveInfo.loadIcon(pm))?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }

                AppInfo(
                    packageName = pkgName,
                    appName = appName,
                    icon = iconBitmap,
                    isDistracting = distractingPackages.contains(pkgName)
                )
            }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }

            withContext(Dispatchers.Main) {
                _state.update {
                    it.copy(
                        installedApps = apps,
                        isLoadingApps = false
                    )
                }
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        return try {
            val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun startTimerTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)

                val breakEnd = repository.getBreakEndTimestamp()
                val now = System.currentTimeMillis()
                val isOnBreak = now < breakEnd
                val active = repository.isFocusActive()

                _state.update { currentState ->
                    val breakRemSec = if (isOnBreak) (breakEnd - now) / 1000L else 0L

                    currentState.copy(
                        isFocusActive = active,
                        isOnBreak = isOnBreak,
                        breakRemainingSeconds = breakRemSec
                    )
                }
            }
        }
    }

    fun toggleFocusMode() {
        val newStatus = !_state.value.isFocusActive
        repository.setFocusActive(newStatus)
        if (!newStatus) {
            repository.setBreakEndTimestamp(0L)
        }
        _state.update { it.copy(isFocusActive = newStatus, isOnBreak = if (!newStatus) false else it.isOnBreak) }
        updateNotificationState(isFocusActive = newStatus, isOnBreak = false)
    }

    fun toggleAppDistracting(packageName: String) {
        repository.toggleDistractingApp(packageName)
        val newDistracting = repository.getDistractingApps()

        _state.update { currentState ->
            val updatedApps = currentState.installedApps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isDistracting = newDistracting.contains(packageName))
                } else app
            }
            currentState.copy(
                distractingAppPackages = newDistracting,
                installedApps = updatedApps
            )
        }

        if (_state.value.isFocusActive) {
            updateNotificationState(
                isFocusActive = true,
                isOnBreak = _state.value.isOnBreak
            )
        }
    }

    fun startBreak(minutes: Int) {
        val breakEnd = System.currentTimeMillis() + minutes * 60 * 1000L
        repository.setBreakEndTimestamp(breakEnd)
        _state.update {
            it.copy(
                isOnBreak = true,
                breakRemainingSeconds = (minutes * 60).toLong(),
                isBreakDialogOpen = false
            )
        }
        updateNotificationState(isFocusActive = true, isOnBreak = true, breakRemainingMinutes = minutes)
    }

    fun endBreak() {
        repository.setBreakEndTimestamp(0L)
        val active = repository.isFocusActive()
        _state.update {
            it.copy(
                isOnBreak = false,
                breakRemainingSeconds = 0L
            )
        }
        updateNotificationState(isFocusActive = active, isOnBreak = false)
    }

    fun saveSchedule(schedule: FocusSchedule) {
        repository.saveSchedule(schedule)
        val active = repository.isFocusActive()
        _state.update {
            it.copy(
                schedule = schedule,
                isScheduleDialogOpen = false,
                isFocusActive = active
            )
        }
        updateNotificationState(isFocusActive = active, isOnBreak = false)
    }

    fun openScheduleDialog(open: Boolean) {
        _state.update { it.copy(isScheduleDialogOpen = open) }
    }

    fun openBreakDialog(open: Boolean) {
        _state.update { it.copy(isBreakDialogOpen = open) }
    }

    fun setAppSearchQuery(query: String) {
        _state.update { it.copy(appSearchQuery = query) }
    }
}
