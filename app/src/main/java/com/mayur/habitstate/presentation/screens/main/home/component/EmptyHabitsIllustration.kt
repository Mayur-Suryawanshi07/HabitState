package com.mayur.habitstate.presentation.screens.main.home.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyHabitsIllustration(
    isFilterActive: Boolean,
    onCreateHabit: () -> Unit,
    onOpenFocusMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stylized Clipboard Illustration matching reference screenshot
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft background glowing circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            color = primaryContainer.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                )

                // Canvas drawing the Clipboard with Checklist rows
                Canvas(modifier = Modifier.size(80.dp, 100.dp)) {
                    val w = size.width
                    val h = size.height

                    // Clipboard main board
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.85f),
                        topLeft = Offset(0f, 10f),
                        size = Size(w, h - 10f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    // Inner paper sheet
                    drawRoundRect(
                        color = surfaceColor.copy(alpha = 0.95f),
                        topLeft = Offset(8f, 18f),
                        size = Size(w - 16f, h - 26f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Top clip
                    drawRoundRect(
                        color = secondaryColor,
                        topLeft = Offset(w * 0.28f, 2f),
                        size = Size(w * 0.44f, 18f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Checklist lines on paper
                    val lineX = 18f
                    val lineEndX = w - 18f
                    val lineY1 = 36f
                    val lineY2 = 50f
                    val lineY3 = 64f

                    drawLine(
                        color = primaryColor.copy(alpha = 0.35f),
                        start = Offset(lineX, lineY1),
                        end = Offset(lineEndX, lineY1),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.35f),
                        start = Offset(lineX, lineY2),
                        end = Offset(lineEndX * 0.75f, lineY2),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.35f),
                        start = Offset(lineX, lineY3),
                        end = Offset(lineEndX * 0.6f, lineY3),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                // Plus badge floating at the bottom right corner of clipboard
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-4).dp)
                        .size(38.dp)
                        .background(
                            color = primaryColor,
                            shape = CircleShape
                        )
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Text Headline
            Text(
                text = if (isFilterActive) "No Habits for this Filter" else "You've not created any habits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Description
            Text(
                text = if (isFilterActive) {
                    "No habits match this time of day. Try selecting 'All' or create a new habit for this period."
                } else {
                    "Related to your daily routine. Check it in a regular way and build powerful, consistent streaks."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Prominent "CREATE YOUR FIRST HABIT" Button (matching screenshot 1)
            Button(
                onClick = onCreateHabit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CREATE YOUR FIRST HABIT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Focus Mode Shortcut in empty state
            OutlinedButton(
                onClick = onOpenFocusMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enter Focus Mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
