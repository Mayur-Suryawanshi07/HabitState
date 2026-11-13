package com.mayur.habitstate.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.mayur.habitstate.data.focus.FocusNotificationManager
import com.mayur.habitstate.data.focus.FocusPreferencesRepository

class FocusAccessibilityService : AccessibilityService() {

    private lateinit var prefsRepository: FocusPreferencesRepository
    private lateinit var notificationManager: FocusNotificationManager
    private lateinit var windowManager: WindowManager

    private val handler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private var overlayPackageName: String = ""

    private val monitorRunnable = object : Runnable {
        override fun run() {
            enforceFocusBlock()
            val interval = if (prefsRepository.isFocusActive() && !prefsRepository.isOnBreak()) {
                MONITOR_INTERVAL_ACTIVE_MS
            } else {
                MONITOR_INTERVAL_IDLE_MS
            }
            handler.postDelayed(this, interval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefsRepository = FocusPreferencesRepository(applicationContext)
        notificationManager = FocusNotificationManager(applicationContext)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
            flags = flags or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        handler.removeCallbacks(monitorRunnable)
        handler.post(monitorRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> enforceFocusBlock(event)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacks(monitorRunnable)
        hideOverlay()
        super.onDestroy()
    }

    private fun enforceFocusBlock(event: AccessibilityEvent? = null) {
        if (!prefsRepository.isFocusActive() || prefsRepository.isOnBreak()) {
            hideOverlay()
            return
        }

        val distractingApps = prefsRepository.getDistractingApps()
        if (distractingApps.isEmpty()) return

        val packageName = resolveForegroundPackage(event) ?: return
        if (shouldIgnorePackage(packageName)) return

        if (distractingApps.contains(packageName)) {
            if (overlayView == null) {
                blockPackage(packageName)
            }
        }
    }

    private fun blockPackage(packageName: String) {
        performGlobalAction(GLOBAL_ACTION_HOME)
        showOverlay(packageName)
    }

    private fun resolveForegroundPackage(event: AccessibilityEvent?): String? {
        event?.packageName?.toString()?.takeIf { it.isNotBlank() }?.let { pkg ->
            if (!shouldIgnorePackage(pkg)) return pkg
        }

        rootInActiveWindow?.packageName?.toString()?.takeIf { it.isNotBlank() }?.let { pkg ->
            if (!shouldIgnorePackage(pkg)) return pkg
        }

        windows?.asSequence()
            ?.filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            ?.mapNotNull { it.root?.packageName?.toString() }
            ?.firstOrNull { it.isNotBlank() && !shouldIgnorePackage(it) }
            ?.let { return it }

        event?.packageName?.toString()?.takeIf { it.isNotBlank() }?.let { return it }
        return rootInActiveWindow?.packageName?.toString()
    }

    private fun shouldIgnorePackage(packageName: String): Boolean {
        val ownPackage = applicationContext.packageName
        if (packageName == ownPackage) return true
        if (packageName == "android") return true
        val ignoredFragments = listOf(
            "systemui",
            "launcher",
            "inputmethod",
            ".ime",
            "permissioncontroller",
            "packageinstaller",
            "intentresolver",
            "android.settings",
            "com.android.settings",
            "com.android.systemui",
            "com.android.phone",
            "com.android.intentresolver"
        )
        return ignoredFragments.any { packageName.contains(it, ignoreCase = true) }
    }

    private fun showOverlay(packageName: String) {
        if (overlayView != null && overlayPackageName == packageName) return

        if (overlayView != null) {
            hideOverlay()
        }

        overlayPackageName = packageName
        val appName = getAppName(packageName)
        val view = createOverlayView(appName, packageName)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            title = "HabitState Focus"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        overlayView = view
        try {
            windowManager.addView(view, params)
        } catch (_: Exception) {
            overlayView = null
            overlayPackageName = ""
            fallbackLaunchBlockedActivity(packageName)
        }
    }

    private fun hideOverlay() {
        overlayView?.let { view ->
            try {
                windowManager.removeViewImmediate(view)
            } catch (_: Exception) {
                try {
                    windowManager.removeView(view)
                } catch (_: Exception) {
                }
            }
        }
        overlayView = null
        overlayPackageName = ""
    }

    private fun fallbackLaunchBlockedActivity(packageName: String) {
        try {
            val intent = Intent(this, com.mayur.habitstate.data.focus.FocusBlockedActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(com.mayur.habitstate.data.focus.FocusBlockedActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
        } catch (_: Exception) {
        }
    }

    private fun createOverlayView(appName: String, packageName: String): View {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val scrim = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            isClickable = true
            isFocusable = true
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(24), dp(24), dp(24), dp(20))
            elevation = dp(8).toFloat()
        }

        val title = TextView(this).apply {
            text = "Focus is on"
            setTextColor(Color.parseColor("#1C1B1F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.DEFAULT_BOLD
        }

        val message = TextView(this).apply {
            text = "$appName and other distracting apps are paused right now"
            setTextColor(Color.parseColor("#49454F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(0, dp(16), 0, dp(24))
        }

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        val useFor5Min = Button(this).apply {
            text = "USE APP FOR 5 MINUTES"
            setTextColor(Color.parseColor("#6750A4"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { allowAppForFiveMinutes(packageName) }
        }

        val okButton = Button(this).apply {
            text = "OK"
            setTextColor(Color.parseColor("#6750A4"))
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                performGlobalAction(GLOBAL_ACTION_HOME)
                hideOverlay()
            }
        }

        actions.addView(useFor5Min)
        actions.addView(okButton)
        card.addView(title)
        card.addView(message)
        card.addView(actions)

        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            marginStart = dp(24)
            marginEnd = dp(24)
        }
        scrim.addView(card, cardParams)
        return scrim
    }

    private fun allowAppForFiveMinutes(packageName: String) {
        val breakEnd = System.currentTimeMillis() + (5 * 60 * 1000L)
        prefsRepository.setBreakEndTimestamp(breakEnd)
        notificationManager.showFocusActiveNotification(
            isOnBreak = true,
            breakRemainingMinutes = 5,
            distractingAppsCount = prefsRepository.getDistractingApps().size
        )
        sendBroadcast(
            Intent(FocusNotificationManager.ACTION_FOCUS_STATE_CHANGED).apply {
                setPackage(applicationContext.packageName)
            }
        )
        hideOverlay()
        if (packageName.isNotBlank()) {
            packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    startActivity(launchIntent)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun getAppName(packageName: String): String {
        if (packageName.isBlank()) return "This app"
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            "This app"
        }
    }

    companion object {
        private const val MONITOR_INTERVAL_ACTIVE_MS = 400L
        private const val MONITOR_INTERVAL_IDLE_MS = 1000L

        fun isServiceEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val component = ComponentName(context, FocusAccessibilityService::class.java)
            val expected = component.flattenToString()
            val expectedShort = component.flattenToShortString()
            return enabledServices.contains(expected, ignoreCase = true) ||
                enabledServices.contains(expectedShort, ignoreCase = true)
        }
    }
}
