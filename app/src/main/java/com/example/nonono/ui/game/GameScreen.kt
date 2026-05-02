package com.example.nonono.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonono.domain.CellState
import com.example.nonono.domain.GameStatus
import com.example.nonono.domain.Puzzle
import com.example.nonono.domain.TapMode
import kotlin.math.abs

private val CELL = 48.dp
private val GUTTER = 56.dp

@Composable
fun GameScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
) {
    val level by viewModel.level.collectAsStateWithLifecycle()
    val puzzle = level.puzzle
    val board by viewModel.board.collectAsStateWithLifecycle()
    val gameStatus by viewModel.gameStatus.collectAsStateWithLifecycle()
    val lives by viewModel.lives.collectAsStateWithLifecycle()
    val tapMode by viewModel.tapMode.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = level.name,
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "❤️".repeat(lives),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text =
                when (gameStatus) {
                    GameStatus.Won -> "You WON!"
                    GameStatus.Lost -> "Sorry, You lost!"
                    else -> "Tap cells to fill the puzzle"
                },
            style = MaterialTheme.typography.bodyLarge,
            color = if (gameStatus == GameStatus.Won) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        ColumnClues(puzzle)

        Row(verticalAlignment = Alignment.Top) {
            Column {
                for (y in 0 until board.height) {
                    RowClue(puzzle.rows[y])
                }
            }

            Box(
                modifier = Modifier.pointerInput(board.width, board.height) {
                    val cellPx = with(density) { CELL.toPx() }
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        fun cellAt(offset: Offset): Pair<Int, Int>? {
                            val cx = (offset.x / cellPx).toInt()
                            val cy = (offset.y / cellPx).toInt()
                            return if (cx in 0 until board.width && cy in 0 until board.height) {
                                cx to cy
                            } else {
                                null
                            }
                        }

                        val downCell = cellAt(down.position)
                        if (downCell != null) {
                            viewModel.onCellTap(downCell.first, downCell.second)
                        }

                        var lastCell = downCell
                        var lockedAxis: Char? = null

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break

                            val cell = cellAt(change.position) ?: continue
                            val (cx, cy) = cell

                            if (lockedAxis == null && downCell != null) {
                                val dx = abs(cx - downCell.first)
                                val dy = abs(cy - downCell.second)
                                if (dx + dy > 0) {
                                    lockedAxis = if (dx >= dy) 'h' else 'v'
                                }
                            }

                            val targetX = if (lockedAxis == 'h') cx else (downCell?.first ?: cx)
                            val targetY = if (lockedAxis == 'v') cy else (downCell?.second ?: cy)
                            val target = targetX to targetY

                            if (target != lastCell) {
                                viewModel.onCellTap(targetX, targetY)
                                lastCell = target
                            }
                        }
                    }
                },
            ) {
                Column {
                    for (y in 0 until board.height) {
                        Row {
                            for (x in 0 until board.width) {
                                Cell(state = board.get(x, y), size = CELL)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(GUTTER))
        }

        Spacer(Modifier.height(16.dp))

        TapModeToggle(
            current = tapMode,
            gameStatus = gameStatus,
            onToggle = viewModel::toggleTapMode,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(onClick = viewModel::reset) {
            Text("New game")
        }

        OutlinedButton(onClick = onBack) {
            Text("Menu")
        }
    }
}

@Composable
private fun TapModeToggle(
    current: TapMode,
    gameStatus: GameStatus,
    onToggle: () -> Unit,
) {
    val modes = listOf(TapMode.Fill, TapMode.Mark)
    SingleChoiceSegmentedButtonRow {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = current == mode,
                enabled = gameStatus == GameStatus.Playing,
                onClick = { if (current != mode) onToggle() },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                icon = {
                    when (mode) {
                        TapMode.Fill -> {
                            Box(
                                modifier =
                                    Modifier
                                        .size(SegmentedButtonDefaults.IconSize)
                                        .background(
                                            color = LocalContentColor.current,
                                            shape = RoundedCornerShape(2.dp),
                                        ),
                            )
                        }

                        TapMode.Mark -> {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                            )
                        }
                    }
                },
                label = {
                    Text(
                        color = MaterialTheme.colorScheme.primary,
                        text =
                            when (mode) {
                                TapMode.Fill -> "Fill"
                                TapMode.Mark -> "Mark"
                            },
                    )
                },
            )
        }
    }
}

@Composable
private fun ColumnClues(puzzle: Puzzle) {
    Row {
        Spacer(Modifier.width(GUTTER))
        for (clue in puzzle.cols) {
            Column(
                modifier = Modifier.size(width = CELL, height = GUTTER),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (n in clue) {
                    Text(
                        text = n.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        // Mirror the gutter on the right so the grid sits centred.
        Spacer(Modifier.width(GUTTER))
    }
}

@Composable
private fun RowClue(clue: List<Int>) {
    Row(
        modifier = Modifier.size(width = GUTTER, height = CELL),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (n in clue) {
            Text(
                text = n.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun Cell(
    state: CellState,
    size: Dp,
) {
    val color =
        if (state == CellState.Filled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    Box(
        modifier =
            Modifier
                .size(size)
                .padding(2.dp)
                .background(color = color, shape = RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (state == CellState.Marked) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Marked",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize().padding(8.dp),
            )
        }
    }
}
