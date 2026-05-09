package com.example.nonono.ui.home

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nonono.data.DailyOutcome
import com.example.nonono.data.currentEpochDay
import com.example.nonono.domain.CellState

private const val LEVEL_COUNT = 10

@Composable
fun HomeScreen(
    onPlayDaily: () -> Unit,
    onLevels: () -> Unit,
    onEndless: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(app) } },
    )
    val dailyOutcome by viewModel.dailyOutcome.collectAsStateWithLifecycle()
    val levelsSolved by viewModel.levelsSolvedCount.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val dailySolution = viewModel.dailySolution

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        TopBar(onSettings = onSettings)

        DailyCard(
            outcome = dailyOutcome,
            solution = dailySolution,
            onPlay = onPlayDaily,
        )

        ModesSection(
            levelsSolved = levelsSolved,
            onLevels = onLevels,
            onEndless = onEndless,
        )

        ActivitySection(streak = streak)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TopBar(onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Nonono",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DailyCard(
    outcome: DailyOutcome,
    solution: com.example.nonono.domain.Board?,
    onPlay: () -> Unit,
) {
    val locked = outcome != DailyOutcome.Pending
    val revealed = outcome != DailyOutcome.Pending

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                )
                Text(
                    text = when (outcome) {
                        DailyOutcome.Pending -> "Daily puzzle"
                        DailyOutcome.Won -> "Solved"
                        DailyOutcome.Lost -> "Come back tomorrow"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "5 × 5 · 3 lives · one attempt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            DailyPreview(solution = solution, revealed = revealed)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Resets in ${resetCountdown()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledIconButton(
                    onClick = onPlay,
                    enabled = !locked,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = if (outcome == DailyOutcome.Won) Icons.Filled.Check else Icons.Filled.PlayArrow,
                        contentDescription = if (locked) "Done" else "Play",
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

private fun resetCountdown(): String {
    val now = System.currentTimeMillis()
    val nextDayMs = (currentEpochDay() + 1L) * 86_400_000L
    val remainingMs = (nextDayMs - now).coerceAtLeast(0L)
    val hours = remainingMs / (60L * 60L * 1000L)
    val minutes = (remainingMs / (60L * 1000L)) % 60L
    return "${hours}h ${minutes}m"
}

@Composable
private fun DailyPreview(
    solution: com.example.nonono.domain.Board?,
    revealed: Boolean,
) {
    val cell = 18.dp
    val gap = 2.dp
    val width = solution?.width ?: 5
    val height = solution?.height ?: 5
    val totalWidth = cell * width + gap * (width - 1)
    val totalHeight = cell * height + gap * (height - 1)

    Box(
        modifier = Modifier.size(width = totalWidth, height = totalHeight),
        contentAlignment = Alignment.Center,
    ) {
        if (revealed && solution != null) {
            PuzzlePreview(cells = solution.cells, width = width, height = height)
        } else {
            Text(
                text = "?",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PuzzlePreview(
    cells: List<CellState>,
    width: Int,
    height: Int,
) {
    val filled = MaterialTheme.colorScheme.onSurface
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val cell = 18.dp
    val gap = 2.dp

    Canvas(
        modifier = Modifier.size(
            width = cell * width + gap * (width - 1),
            height = cell * height + gap * (height - 1),
        ),
    ) {
        val cellPx = cell.toPx()
        val gapPx = gap.toPx()
        for (i in cells.indices) {
            val cx = i % width
            val cy = i / width
            val x = cx * (cellPx + gapPx)
            val y = cy * (cellPx + gapPx)
            drawRect(
                color = if (cells[i] == CellState.Filled) filled else empty,
                topLeft = Offset(x, y),
                size = Size(cellPx, cellPx),
            )
        }
    }
}

@Composable
private fun ModesSection(
    levelsSolved: Int,
    onLevels: () -> Unit,
    onEndless: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("MODES")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeTile(
                title = "Levels",
                subtitle = "$levelsSolved of $LEVEL_COUNT solved",
                onClick = onLevels,
                modifier = Modifier.weight(1f),
            )
            ModeTile(
                title = "Endless",
                subtitle = "Random puzzles",
                onClick = onEndless,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ModeTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActivitySection(streak: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("ACTIVITY")
        Column {
            ActivityRow(
                label = "Current streak",
                value = if (streak == 1) "1 day" else "$streak days",
            )
            ThinDivider()
            ActivityRow("Best time", "0:42")
            ThinDivider()
            ActivityRow("Puzzles solved", "24")
        }
    }
}

@Composable
private fun ActivityRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.5.sp,
    )
}
