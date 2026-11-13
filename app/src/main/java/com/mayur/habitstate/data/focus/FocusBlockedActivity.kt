package com.mayur.habitstate.data.focus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mayur.habitstate.ui.theme.HabitStateTheme

class FocusBlockedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appName = getAppName(packageName)

        val prefsRepository = FocusPreferencesRepository(applicationContext)
        val notificationManager = FocusNotificationManager(applicationContext)

        setContent {
            HabitStateTheme {
                FocusBlockedScreen(
                    appName = appName,
                    onUseFor5Minutes = {
                        // Start 5-minute break
                        val breakEnd = System.currentTimeMillis() + (5 * 60 * 1000L)
                        prefsRepository.setBreakEndTimestamp(breakEnd)
                        notificationManager.showFocusActiveNotification(
                            isOnBreak = true,
                            breakRemainingMinutes = 5,
                            distractingAppsCount = prefsRepository.getDistractingApps().size
                        )

                        // Broadcast update to viewmodel/listeners
                        val updateIntent = Intent(FocusNotificationManager.ACTION_FOCUS_STATE_CHANGED).apply {
                            setPackage(applicationContext.packageName)
                        }
                        sendBroadcast(updateIntent)

                        // Launch the target app
                        if (packageName.isNotBlank()) {
                            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                            if (launchIntent != null) {
                                startActivity(launchIntent)
                            }
                        }
                        finish()
                    },
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    private fun getAppName(packageName: String): String {
        if (packageName.isBlank()) return "This app"
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            "This app"
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}

@Composable
fun FocusBlockedScreen(
    appName: String,
    onUseFor5Minutes: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with icon & title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Adjust,
                            contentDescription = "Focus Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Focus is on",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Message text matching screenshot
                Text(
                    text = "$appName and other distracting apps are paused right now",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions row matching screenshot buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onUseFor5Minutes,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "USE APP FOR 5 MINUTES",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "OK",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
