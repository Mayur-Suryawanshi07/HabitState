package com.mayur.habitstate.presentation.screens.main.score

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mayur.habitstate.domain.model.HabitScoreItem
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScoreScreen(
    viewModel: HabitScoreViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val summary = state.summary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Habit Score",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Score info"
                        )
                    }
                    IconButton(onClick = {
                        val text = "My HabitState score is ${
                            String.format(Locale.getDefault(), "%.2f", summary.overallScore)
                        } out of 100"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share score"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                },
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Overall Score",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ScoreGauge(
                                score = summary.overallScore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%.2f out of 100",
                                        summary.overallScore
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (summary.items.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Create habits and complete them to see your score.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(summary.items, key = { it.habit.id }) { item ->
                        HabitScoreMiniCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitScoreMiniCard(item: HabitScoreItem) {
    val habitColor = Color(item.habit.colorHex)
    val cardBg = MaterialTheme.colorScheme.surface
    val pillBg = habitColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MiniScoreGauge(
                score = item.scorePercent,
                accentColor = habitColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = pillBg
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", item.scorePercent),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoreGauge(
    score: Float,
    modifier: Modifier = Modifier
) {
    val coralRed = Color(0xFFE55B49)
    val warmYellow = Color(0xFFF3CB6C)
    val mintGreen = Color(0xFFA1D4B0)
    val needleColor = Color(0xFF383C3E)
    val clamped = score.coerceIn(0f, 100f)

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.14f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val diameter = size.minDimension * 1.1f
        val top = size.height * 0.15f
        val left = (size.width - diameter) / 2f
        val arcSize = Size(diameter, diameter)

        // 3 Segmented Arcs matching Image 1
        // Arc 1: Red (Coral)
        drawArc(
            color = coralRed,
            startAngle = 185f,
            sweepAngle = 48f,
            useCenter = false,
            topLeft = Offset(left, top),
            size = arcSize,
            style = stroke
        )
        // Arc 2: Yellow
        drawArc(
            color = warmYellow,
            startAngle = 240f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = Offset(left, top),
            size = arcSize,
            style = stroke
        )
        // Arc 3: Mint Green
        drawArc(
            color = mintGreen,
            startAngle = 307f,
            sweepAngle = 48f,
            useCenter = false,
            topLeft = Offset(left, top),
            size = arcSize,
            style = stroke
        )

        // Needle pointing to current score angle
        val angleRad = Math.toRadians((180.0 - (clamped / 100.0) * 180.0))
        val center = Offset(size.width / 2f, top + diameter / 2f)
        val needleLength = diameter * 0.42f
        val tip = Offset(
            x = center.x + (cos(angleRad) * needleLength).toFloat(),
            y = center.y - (sin(angleRad) * needleLength).toFloat()
        )
        drawLine(
            color = needleColor,
            start = center,
            end = tip,
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )
        drawCircle(color = needleColor, radius = 12f, center = center)
    }
}

@Composable
private fun MiniScoreGauge(
    score: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val clamped = score.coerceIn(0f, 100f)
    val needleColor = Color(0xFF383C3E)
    val trackColor = accentColor.copy(alpha = 0.12f)
    val indicatorColor = Color(0xFFE55B49)

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.14f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val diameter = size.minDimension * 1.1f
        val top = size.height * 0.15f
        val left = (size.width - diameter) / 2f
        val arcSize = Size(diameter, diameter)

        // Background arc track
        drawArc(
            color = trackColor,
            startAngle = 185f,
            sweepAngle = 170f,
            useCenter = false,
            topLeft = Offset(left, top),
            size = arcSize,
            style = stroke
        )

        // Colored mark at start of arc
        drawArc(
            color = indicatorColor,
            startAngle = 185f,
            sweepAngle = 12f,
            useCenter = false,
            topLeft = Offset(left, top),
            size = arcSize,
            style = stroke
        )

        // Needle pointing to score
        val angleRad = Math.toRadians((180.0 - (clamped / 100.0) * 180.0))
        val center = Offset(size.width / 2f, top + diameter / 2f)
        val needleLength = diameter * 0.42f
        val tip = Offset(
            x = center.x + (cos(angleRad) * needleLength).toFloat(),
            y = center.y - (sin(angleRad) * needleLength).toFloat()
        )
        drawLine(
            color = needleColor,
            start = center,
            end = tip,
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
        drawCircle(color = needleColor, radius = 8f, center = center)
    }
}
