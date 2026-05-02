package com.example.nonono.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonono.domain.CellState
import com.example.nonono.domain.samplePuzzles

@Composable
fun HomeScreen(
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {

        TopBar(onSettings = { /* TODO */ })

        DailyCard(onPlay = onPlay)

        ModesSection(onPlay = onPlay)

        ActivitySection()

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
private fun DailyCard(onPlay: () -> Unit) {
    val daily = samplePuzzles.first()

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
                    text = daily.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${daily.solution.width} × ${daily.solution.height} · 3 lives",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PuzzlePreview(
                cells = daily.solution.cells,
                width = daily.solution.width,
                height = daily.solution.height,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Resets in 7h 32m",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledIconButton(
                    onClick = onPlay,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
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
private fun ModesSection(onPlay: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("MODES")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeTile(
                title = "Levels",
                subtitle = "3 of 10 solved",
                onClick = onPlay,
                modifier = Modifier.weight(1f),
            )
            ModeTile(
                title = "Endless",
                subtitle = "Random puzzles",
                onClick = onPlay,
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
private fun ActivitySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("ACTIVITY")
        Column {
            ActivityRow("Current streak", "5 days")
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
