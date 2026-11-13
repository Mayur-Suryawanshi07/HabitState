package com.mayur.habitstate.presentation.screens.main.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class HabitTimeFilter(val label: String) {
    ALL("All"),
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    ANYTIME("Anytime")
}

fun getFilterIcon(filter: HabitTimeFilter): ImageVector? {
    return when (filter) {
        HabitTimeFilter.ALL -> null
        HabitTimeFilter.MORNING -> Icons.Default.WbSunny
        HabitTimeFilter.AFTERNOON -> Icons.Default.LightMode
        HabitTimeFilter.EVENING -> Icons.Default.NightsStay
        HabitTimeFilter.ANYTIME -> Icons.Default.Schedule
    }
}

@Composable
fun TimeFilterRow(
    selectedFilter: HabitTimeFilter,
    onFilterSelected: (HabitTimeFilter) -> Unit,
    countsProvider: (HabitTimeFilter) -> Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HabitTimeFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val count = countsProvider(filter)
            val icon = getFilterIcon(filter)

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                label = "filterBg"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "filterContent"
            )

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(20.dp),
                color = backgroundColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = filter.label,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = filter.label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = contentColor
                    )

                    if (count > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
