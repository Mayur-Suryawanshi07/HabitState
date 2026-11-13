package com.mayur.habitstate.presentation.screens.main.home.component

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mayur.habitstate.domain.model.Habit
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val availableColors = listOf(
    0xFF6A1B9A, // HabitState Purple Focus
    0xFF2196F3, // Blue
    0xFFFF9800, // Amber
    0xFF9C27B0, // Purple
    0xFFFF5722, // Sunset Coral
    0xFF009688  // Teal
)

val availableIcons = listOf(
    "fitness" to "Fitness",
    "water" to "Water",
    "book" to "Reading",
    "mindset" to "Mindset",
    "code" to "Coding",
    "sleep" to "Sleep",
    "run" to "Running"
)

val availableCategories = listOf(
    "General",
    "Health",
    "Fitness",
    "Mindset",
    "Productivity",
    "Learning"
)

val dayLabels = listOf(
    1 to "Mon",
    2 to "Tue",
    3 to "Wed",
    4 to "Thu",
    5 to "Fri",
    6 to "Sat",
    7 to "Sun"
)

data class HabitPreset(
    val title: String,
    val category: String,
    val iconName: String,
    val colorHex: Long,
    val timeOfDay: String,
    val defaultHour: Int
)

val habitPresets = listOf(
    HabitPreset("Drink 2L Water", "Health", "water", 0xFF2196F3, "MORNING", 8),
    HabitPreset("Morning Workout", "Fitness", "fitness", 0xFF6A1B9A, "MORNING", 7),
    HabitPreset("30-Min Reading", "Learning", "book", 0xFFFF9800, "EVENING", 20),
    HabitPreset("10-Min Meditation", "Mindset", "mindset", 0xFF9C27B0, "MORNING", 7),
    HabitPreset("Coding Practice", "Productivity", "code", 0xFF009688, "AFTERNOON", 15)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitDialog(
    initialHabit: Habit?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        description: String,
        iconName: String,
        colorHex: Long,
        category: String,
        timeOfDay: String,
        frequencyDays: Set<Int>,
        reminderHour: Int?,
        reminderMinute: Int?,
        isReminderEnabled: Boolean,
        isActive: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(initialHabit?.category ?: "General") }
    var selectedTimeOfDay by remember { mutableStateOf(initialHabit?.timeOfDay ?: "ANYTIME") }
    var selectedColor by remember { mutableStateOf(initialHabit?.colorHex ?: availableColors.first()) }
    var selectedIcon by remember { mutableStateOf(initialHabit?.iconName ?: availableIcons.first().first) }

    var selectedDays by remember {
        mutableStateOf(initialHabit?.frequencyDays ?: (1..7).toSet())
    }

    var isReminderEnabled by remember {
        mutableStateOf(initialHabit?.isReminderEnabled ?: false)
    }

    var reminderHour by remember {
        mutableIntStateOf(initialHabit?.reminderHour ?: 8)
    }
    var reminderMinute by remember {
        mutableIntStateOf(initialHabit?.reminderMinute ?: 0)
    }

    var isActive by remember {
        mutableStateOf(initialHabit?.isActive ?: true)
    }

    var nameError by remember { mutableStateOf<String?>(null) }

    val formattedTime = remember(reminderHour, reminderMinute) {
        val time = LocalTime.of(reminderHour, reminderMinute)
        time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                reminderHour = hourOfDay
                reminderMinute = minute
            },
            reminderHour,
            reminderMinute,
            false
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialHabit == null) "Create New Habit" else "Edit Habit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Quick Presets Carousel if creating a new habit (inspired by Screenshot 2)
                if (initialHabit == null) {
                    Text(
                        text = "CHOOSE FROM PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        habitPresets.forEach { preset ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        name = preset.title
                                        selectedCategory = preset.category
                                        selectedIcon = preset.iconName
                                        selectedColor = preset.colorHex
                                        selectedTimeOfDay = preset.timeOfDay
                                        reminderHour = preset.defaultHour
                                        reminderMinute = 0
                                        nameError = null
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getHabitIcon(preset.iconName),
                                        contentDescription = null,
                                        tint = Color(preset.colorHex),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Habit Name *") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "DO IT AT" Time of Day (inspired by Screenshot 3)
                Text(
                    text = "Do it at",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timeOptions = listOf(
                        Triple("ANYTIME", "Anytime", Icons.Default.Schedule),
                        Triple("MORNING", "Morning", Icons.Default.WbSunny),
                        Triple("AFTERNOON", "Afternoon", Icons.Default.LightMode),
                        Triple("EVENING", "Evening", Icons.Default.NightsStay)
                    )

                    timeOptions.forEach { (key, label, icon) ->
                        FilterChip(
                            selected = selectedTimeOfDay == key,
                            onClick = {
                                selectedTimeOfDay = key
                                when (key) {
                                    "MORNING" -> reminderHour = 8
                                    "AFTERNOON" -> reminderHour = 14
                                    "EVENING" -> reminderHour = 20
                                }
                            },
                            label = { Text(label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Picker
                Text(
                    text = "Habit Color",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableColors.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal }
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected Color",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Picker
                Text(
                    text = "Habit Icon",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableIcons.forEach { (iconKey, label) ->
                        val isSelected = selectedIcon == iconKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIcon = iconKey },
                            label = { Text(label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getHabitIcon(iconKey),
                                    contentDescription = label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = {
                        selectedDays = if (selectedDays.size == 7) emptySet() else (1..7).toSet()
                    }) {
                        Text(if (selectedDays.size == 7) "Clear All" else "Every Day")
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dayLabels.forEach { (dayNum, dayName) ->
                        val isDaySelected = selectedDays.contains(dayNum)
                        FilterChip(
                            selected = isDaySelected,
                            onClick = {
                                selectedDays = if (isDaySelected) {
                                    selectedDays - dayNum
                                } else {
                                    selectedDays + dayNum
                                }
                            },
                            label = { Text(dayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Switch & Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Reminder",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Send notification on scheduled days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it }
                    )
                }

                if (isReminderEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { timePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Select Time",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reminder Time: $formattedTime")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active / Inactive Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Habit Status",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isActive) "Active (visible in tracker)" else "Paused (tracking paused)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Habit name is required"
                        return@Button
                    }
                    val days = if (selectedDays.isEmpty()) (1..7).toSet() else selectedDays
                    onSave(
                        name.trim(),
                        description.trim(),
                        selectedIcon,
                        selectedColor,
                        selectedCategory,
                        selectedTimeOfDay,
                        days,
                        if (isReminderEnabled) reminderHour else null,
                        if (isReminderEnabled) reminderMinute else null,
                        isReminderEnabled,
                        isActive
                    )
                }
            ) {
                Text(if (initialHabit == null) "Add Habit" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
