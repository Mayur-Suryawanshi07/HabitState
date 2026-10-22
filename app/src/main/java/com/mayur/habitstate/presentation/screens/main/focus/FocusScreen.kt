package com.mayur.habitstate.presentation.screens.main.focus

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mayur.habitstate.presentation.screens.main.component.CustomBottomBar
import com.mayur.habitstate.presentation.screens.main.component.CustomTopBar
import com.mayur.habitstate.presentation.screens.main.component.Screen
import com.mayur.habitstate.presentation.screens.main.component.bottomItems

@Composable
fun FocusScreen(
    viewModel: FocusViewModel = viewModel(),
    onNavigateToScreen: (Screen) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check permissions and settings when screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAccessibilityPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val distractingList = remember(state.installedApps, state.distractingAppPackages) {
        state.installedApps.filter { state.distractingAppPackages.contains(it.packageName) }
    }

    val remainingList = remember(state.installedApps, state.distractingAppPackages, state.appSearchQuery) {
        val nonDistracting = state.installedApps.filter { !state.distractingAppPackages.contains(it.packageName) }
        if (state.appSearchQuery.isBlank()) {
            nonDistracting
        } else {
            nonDistracting.filter {
                it.appName.contains(state.appSearchQuery, ignoreCase = true) ||
                        it.packageName.contains(state.appSearchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(title = "Focus")
        },
        bottomBar = {
            CustomBottomBar(
                items = bottomItems,
                currentRoute = Screen.Search.route,
                onItemClick = onNavigateToScreen
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Accessibility Permission Banner
            if (!state.isAccessibilityEnabled) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Permission Warning",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Enable App Blocking",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "To automatically pause distracting apps when Focus Mode is active, turn on HabitState in Accessibility Settings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Enable in Settings")
                            }
                        }
                    }
                }
            }

            // Title & Description
            item {
                Column {
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When you need time to focus, you can pause distracting apps and hide their notifications.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Schedule Setting Row
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.openScheduleDialog(true) },
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.schedule.isEnabled) Icons.Default.Schedule else Icons.Default.Add,
                            contentDescription = "Schedule",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.schedule.isEnabled) "Schedule Active" else "Set a schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (state.schedule.isEnabled) {
                                Text(
                                    text = "${formatTime12H(state.schedule.startHour, state.schedule.startMinute)} - ${formatTime12H(state.schedule.endHour, state.schedule.endMinute)} • ${formatDays(state.schedule.activeDays)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Control Buttons & Session Status
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Active Status Indicator
                    if (state.isFocusActive || state.isOnBreak) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.isOnBreak) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (state.isOnBreak) "On Break" else "Focus Session Active",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = if (state.isOnBreak) formatSeconds(state.breakRemainingSeconds) else formatSeconds(state.focusSessionSeconds),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                if (state.isOnBreak) {
                                    TextButton(onClick = viewModel::endBreak) {
                                        Text("End Break", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = viewModel::toggleFocusMode,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isFocusActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                                contentColor = if (state.isFocusActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = if (state.isFocusActive) "TURN OFF NOW" else "TURN ON NOW",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.openBreakDialog(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FreeBreakfast,
                                contentDescription = "Break",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TAKE A BREAK",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Distracting Apps Section
            item {
                Text(
                    text = "Your distracting apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (distractingList.isEmpty()) {
                item {
                    Text(
                        text = "No distracting apps selected yet. Select apps below to pause them during Focus Mode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(distractingList, key = { "distracting_${it.packageName}" }) { app ->
                    AppRowItem(
                        app = app,
                        onCheckedChange = { viewModel.toggleAppDistracting(app.packageName) }
                    )
                }
            }

            // Select More Apps Section
            item {
                Column {
                    Text(
                        text = "Select more apps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = state.appSearchQuery,
                        onValueChange = viewModel::setAppSearchQuery,
                        placeholder = { Text("Search installed apps...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            if (state.isLoadingApps) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(remainingList, key = { "remaining_${it.packageName}" }) { app ->
                    AppRowItem(
                        app = app,
                        onCheckedChange = { viewModel.toggleAppDistracting(app.packageName) }
                    )
                }
            }
        }
    }

    // Schedule Settings Dialog
    if (state.isScheduleDialogOpen) {
        ScheduleDialog(
            currentSchedule = state.schedule,
            onDismiss = { viewModel.openScheduleDialog(false) },
            onSave = viewModel::saveSchedule
        )
    }

    // Break Dialog
    if (state.isBreakDialogOpen) {
        BreakDialog(
            onDismiss = { viewModel.openBreakDialog(false) },
            onSelectBreak = viewModel::startBreak
        )
    }
}

@Composable
fun AppRowItem(
    app: AppInfo,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!app.isDistracting) },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.appName,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = app.appName,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Checkbox(
                checked = app.isDistracting,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun ScheduleDialog(
    currentSchedule: FocusSchedule,
    onDismiss: () -> Unit,
    onSave: (FocusSchedule) -> Unit
) {
    var startHour by remember { mutableIntStateOf(currentSchedule.startHour) }
    var startMinute by remember { mutableIntStateOf(currentSchedule.startMinute) }
    var endHour by remember { mutableIntStateOf(currentSchedule.endHour) }
    var endMinute by remember { mutableIntStateOf(currentSchedule.endMinute) }
    var activeDays by remember { mutableStateOf(currentSchedule.activeDays) }

    val daysMap = listOf(
        1 to "S", // Sun
        2 to "M", // Mon
        3 to "T", // Tue
        4 to "W", // Wed
        5 to "T", // Thu
        6 to "F", // Fri
        7 to "S"  // Sat
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set a schedule",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Time Pickers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatTime12H(startHour, startMinute),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                // Toggle standard morning/afternoon times for simplicity
                                if (startHour == 9) startHour = 8 else if (startHour == 8) startHour = 10 else startHour = 9
                            }
                        )
                    }

                    Column {
                        Text(
                            text = "End",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatTime12H(endHour, endMinute),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                if (endHour == 17) endHour = 18 else if (endHour == 18) endHour = 16 else endHour = 17
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Weekdays Circles Row
                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysMap.forEach { (dayInt, label) ->
                        val selected = activeDays.contains(dayInt)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable {
                                    activeDays = if (selected) {
                                        activeDays - dayInt
                                    } else {
                                        activeDays + dayInt
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        FocusSchedule(
                            isEnabled = true,
                            startHour = startHour,
                            startMinute = startMinute,
                            endHour = endHour,
                            endMinute = endMinute,
                            activeDays = activeDays
                        )
                    )
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("SET", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun BreakDialog(
    onDismiss: () -> Unit,
    onSelectBreak: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Take a Break", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select break duration:")
                Spacer(modifier = Modifier.height(8.dp))
                listOf(5, 15, 30).forEach { mins ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectBreak(mins) },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$mins minutes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

private fun formatSeconds(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

private fun formatTime12H(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return String.format("%d:%02d %s", h12, minute, amPm)
}

private fun formatDays(days: Set<Int>): String {
    val map = mapOf(1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat")
    return days.sorted().mapNotNull { map[it] }.joinToString(", ")
}
