package com.mayur.habitstate.presentation.screens.main.focus

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    init {
        loadData()
        startTimerTicker()
    }

    fun loadData() {
        checkAccessibilityPermission()
        loadPreferences()
        loadInstalledApps()
    }

    fun checkAccessibilityPermission() {
        val enabled = FocusAccessibilityService.isServiceEnabled(getApplication())
        _state.update { it.copy(isAccessibilityEnabled = enabled) }
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
                    val newSessionSec = if (active && !isOnBreak) currentState.focusSessionSeconds + 1 else currentState.focusSessionSeconds
                    val breakRemSec = if (isOnBreak) (breakEnd - now) / 1000L else 0L

                    currentState.copy(
                        isFocusActive = active,
                        isOnBreak = isOnBreak,
                        breakRemainingSeconds = breakRemSec,
                        focusSessionSeconds = newSessionSec
                    )
                }
            }
        }
    }

    fun toggleFocusMode() {
        val newStatus = !_state.value.isFocusActive
        repository.setFocusActive(newStatus)
        _state.update { it.copy(isFocusActive = newStatus, isOnBreak = false) }
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
    }

    fun endBreak() {
        repository.setBreakEndTimestamp(0L)
        _state.update {
            it.copy(
                isOnBreak = false,
                breakRemainingSeconds = 0L
            )
        }
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
