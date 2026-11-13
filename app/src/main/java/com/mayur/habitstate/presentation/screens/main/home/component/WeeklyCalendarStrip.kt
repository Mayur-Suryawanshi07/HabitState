package com.mayur.habitstate.presentation.screens.main.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyCalendarStrip(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    completionStatus: Map<LocalDate, Float>,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {},
    onResetToToday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val isFuture = date.isAfter(today)
            val ratio = completionStatus[date] ?: 0f
            val isCompleted = !isFuture && ratio >= 1f
            val isMissed = date.isBefore(today) && ratio < 1f

            val yellowAccent = Color(0xFFFBC02D)
            val yellowBg = Color(0xFFFFF9C4)

            val ringColor by animateColorAsState(
                targetValue = when {
                    isSelected -> yellowAccent
                    isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    isMissed -> MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                },
                label = "calRing"
            )
            val fillColor by animateColorAsState(
                targetValue = when {
                    isSelected -> yellowBg
                    isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else -> Color.Transparent
                },
                label = "calFill"
            )
            val textColor by animateColorAsState(
                targetValue = when {
                    isSelected -> Color.Black
                    isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                label = "calText"
            )

            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSelectDate(date) }
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(fillColor, CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 1.5.dp,
                            color = ringColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || date == today) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}
