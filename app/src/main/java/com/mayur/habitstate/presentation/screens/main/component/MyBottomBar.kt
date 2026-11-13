package com.mayur.habitstate.presentation.screens.main.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomBottomBar(
    items: List<Screen> = bottomItems,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    onAddClick: () -> Unit = {}
) {
    val homeSelected = currentRoute == Screen.Home.route
    val statsSelected = currentRoute == Screen.Profile.route

    val yellowAccent = Color(0xFFFDD835)

    Surface(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val homeTint by animateColorAsState(
                if (homeSelected) yellowAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                label = "homeTint"
            )
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = Screen.Home.label,
                tint = homeTint,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onItemClick(Screen.Home) }
                    .padding(2.dp)
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            val statsTint by animateColorAsState(
                if (statsSelected) yellowAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                label = "statsTint"
            )
            Icon(
                imageVector = Icons.Outlined.FormatListBulleted,
                contentDescription = Screen.Profile.label,
                tint = statsTint,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onItemClick(Screen.Profile) }
                    .padding(2.dp)
            )
        }
    }
}
