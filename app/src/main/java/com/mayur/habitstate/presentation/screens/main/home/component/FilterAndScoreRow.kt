package com.mayur.habitstate.presentation.screens.main.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mayur.habitstate.presentation.screens.main.home.HabitListFilter
import java.util.Locale

@Composable
fun FilterAndScoreRow(
    selectedFilter: HabitListFilter,
    allCount: Int,
    dueCount: Int,
    habitScore: Float,
    onFilterSelected: (HabitListFilter) -> Unit,
    onHabitScoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipButton(
                label = "${HabitListFilter.ALL.label} ($allCount)",
                selected = selectedFilter == HabitListFilter.ALL,
                onClick = { onFilterSelected(HabitListFilter.ALL) }
            )
            FilterChipButton(
                label = "${HabitListFilter.DUE.label} ($dueCount)",
                selected = selectedFilter == HabitListFilter.DUE,
                onClick = { onFilterSelected(HabitListFilter.DUE) }
            )
        }

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onHabitScoreClick)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Speed,
                    contentDescription = "Habit Score",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", habitScore),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val yellowAccent = Color(0xFFFDD835)

    val background by animateColorAsState(
        targetValue = if (selected) yellowAccent else MaterialTheme.colorScheme.surface,
        label = "filterBg"
    )
    val content by animateColorAsState(
        targetValue = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
        label = "filterFg"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier else Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
            ),
        shape = RoundedCornerShape(20.dp),
        color = background
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = content,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
