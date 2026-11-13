package com.mayur.habitstate.presentation.screens.main.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mayur.habitstate.domain.model.Habit
import com.mayur.habitstate.domain.model.HabitWithStatus

fun getHabitIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "water" -> Icons.Default.WaterDrop
        "book", "reading" -> Icons.Default.Book
        "mindset", "meditation" -> Icons.Default.SelfImprovement
        "code", "programming" -> Icons.Default.Code
        "sleep" -> Icons.Default.Nightlight
        "run", "running" -> Icons.AutoMirrored.Filled.DirectionsRun
        else -> Icons.Default.FitnessCenter
    }
}

fun getTimeOfDayIcon(timeOfDay: String): ImageVector {
    return when (timeOfDay.uppercase()) {
        "MORNING" -> Icons.Default.WbSunny
        "AFTERNOON" -> Icons.Default.LightMode
        "EVENING" -> Icons.Default.NightsStay
        else -> Icons.Default.Schedule
    }
}

@Composable
fun HabitCard(
    habitWithStatus: HabitWithStatus,
    onToggleCompletion: (Long) -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onTogglePause: (Habit) -> Unit,
    onAddNote: (Habit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val habit = habitWithStatus.habit
    val isCompleted = habitWithStatus.isCompletedToday
    val streak = habitWithStatus.currentStreak
    var isMenuExpanded by remember { mutableStateOf(false) }
    val habitColor = Color(habit.colorHex)

    val checkBgColor by animateColorAsState(
        targetValue = if (isCompleted) habitColor else Color.Transparent,
        label = "checkBg"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getHabitIcon(habit.iconName),
                        contentDescription = habit.name,
                        tint = habitColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isMenuExpanded = true }
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitle = when {
                        streak > 0 -> "Streak: $streak day${if (streak == 1) "" else "s"}"
                        habit.description.isNotBlank() -> habit.description
                        else -> habit.formattedFrequency()
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                            onClick = {
                                isMenuExpanded = false
                                onEdit(habit)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (habit.isActive) "Pause" else "Resume") },
                            leadingIcon = {
                                Icon(
                                    if (habit.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onTogglePause(habit)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onDelete(habit)
                            }
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAddNote(habit) },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Add note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(checkBgColor)
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) habitColor else habitColor.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                        .clickable { onToggleCompletion(habit.id) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (habitWithStatus.heatmap.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                val numRows = 7
                val totalCells = habitWithStatus.heatmap.size
                val numCols = (totalCells + numRows - 1) / numRows

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until numCols) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (row in 0 until numRows) {
                                val index = col * numRows + row
                                val cell = habitWithStatus.heatmap.getOrNull(index)
                                val cellColor = when {
                                    cell?.isCompleted == true -> habitColor
                                    cell?.isScheduled == true -> habitColor.copy(alpha = 0.14f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(2.5.dp))
                                        .background(cellColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
