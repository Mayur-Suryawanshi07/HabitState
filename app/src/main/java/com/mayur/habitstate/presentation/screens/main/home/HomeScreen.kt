package com.mayur.habitstate.presentation.screens.main.home

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.Habit.Companion.ordinalSuffix
import com.mayur.habitstate.presentation.screens.main.component.CustomBottomBar
import com.mayur.habitstate.presentation.screens.main.component.Screen
import com.mayur.habitstate.presentation.screens.main.home.component.EmptyHabitsIllustration
import com.mayur.habitstate.presentation.screens.main.home.component.FilterAndScoreRow
import com.mayur.habitstate.presentation.screens.main.home.component.HabitCard
import com.mayur.habitstate.presentation.screens.main.home.component.WeeklyCalendarStrip
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToScreen: (Screen) -> Unit,
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToEditHabit: (Long) -> Unit = {},
    onNavigateToHabitScore: () -> Unit = {},
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var habitForNote by remember { mutableStateOf<Habit?>(null) }
    var noteDraft by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val dateHeader = remember(state.selectedDate, state.isTodaySelected) {
        formatHomeDateHeader(state.selectedDate, state.isTodaySelected)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateHeader,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                // Crown / Premium Action
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToScreen(Screen.Search) },
                    shape = CircleShape,
                    color = Color(0xFFFFF59D) // Soft Gold/Yellow Pill
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Premium",
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Share Action
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            val shareText = buildString {
                                append(dateHeader)
                                append("\nHabit Score: ")
                                append(String.format(Locale.getDefault(), "%.1f%%", state.habitScore))
                                append("\nCompleted ")
                                append(state.completedCount)
                                append("/")
                                append(state.totalCount)
                                append(" habits")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share progress"))
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Settings Action
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToScreen(Screen.Profile) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            CustomBottomBar(
                currentRoute = Screen.Home.route,
                onItemClick = onNavigateToScreen,
                onAddClick = onNavigateToAddHabit
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToScreen(Screen.Search) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Focus Mode"
                    )
                },
                text = {
                    Text(
                        text = "Focus Mode",
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    state.errorMessage?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.dismissError() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                WeeklyCalendarStrip(
                    selectedDate = state.selectedDate,
                    weekDates = state.weekDates,
                    completionStatus = state.weekCompletionStatus,
                    onSelectDate = { viewModel.selectDate(it) },
                    onPreviousWeek = { viewModel.previousWeek() },
                    onNextWeek = { viewModel.nextWeek() },
                    onResetToToday = { viewModel.resetToToday() }
                )
            }

            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Filter",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Habit Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterAndScoreRow(
                        selectedFilter = state.selectedFilter,
                        allCount = state.totalCount,
                        dueCount = state.dueCount,
                        habitScore = state.habitScore,
                        onFilterSelected = { viewModel.setFilter(it) },
                        onHabitScoreClick = onNavigateToHabitScore
                    )
                }
            }

            if (state.isLoading && state.habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (!state.isLoading && state.filteredHabits.isEmpty()) {
                item {
                    EmptyHabitsIllustration(
                        isFilterActive = state.selectedFilter != HabitListFilter.ALL,
                        onCreateHabit = onNavigateToAddHabit,
                        onOpenFocusMode = { onNavigateToScreen(Screen.Search) }
                    )
                }
            }

            items(
                items = state.filteredHabits,
                key = { it.habit.id }
            ) { habitWithStatus ->
                HabitCard(
                    habitWithStatus = habitWithStatus,
                    onToggleCompletion = { habitId -> viewModel.toggleHabitCompletion(habitId) },
                    onEdit = { habit -> onNavigateToEditHabit(habit.id) },
                    onDelete = { habit -> habitToDelete = habit },
                    onTogglePause = { habit -> viewModel.togglePauseHabit(habit) },
                    onAddNote = { habit ->
                        habitForNote = habit
                        noteDraft = habit.description
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete Habit") },
            text = {
                Text("Are you sure you want to delete \"${habit.name}\"? All history and streak logs will be permanently deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(habit)
                        habitToDelete = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) { Text("Cancel") }
            }
        )
    }

    habitForNote?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitForNote = null },
            title = { Text("Add a note") },
            text = {
                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Log progress or a short note") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateHabit(habit.copy(description = noteDraft.trim()))
                        habitForNote = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { habitForNote = null }) { Text("Cancel") }
            }
        )
    }
}

private fun formatHomeDateHeader(date: LocalDate, isToday: Boolean): String {
    val day = date.dayOfMonth
    val month = date.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
    val body = "$day${ordinalSuffix(day)} $month"
    return if (isToday) "Today, $body" else body
}
