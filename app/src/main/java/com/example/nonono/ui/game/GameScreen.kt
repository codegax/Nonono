package com.example.nonono.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonono.domain.CellState
import com.example.nonono.domain.Puzzle
import com.example.nonono.domain.TapMode
import com.example.nonono.domain.GameStatus

private val CELL = 48.dp
private val GUTTER = 56.dp

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
) {
    val puzzle = viewModel.puzzle
    val board by viewModel.board.collectAsStateWithLifecycle()
    val gameStatus by viewModel.gameStatus.collectAsStateWithLifecycle()
    val lives by viewModel.lives.collectAsStateWithLifecycle()
    val tapMode by viewModel.tapMode.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nonono",
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
            // text = if (isWon) "You win!" else if(isLost) "You Lost!" else "Tap cells to fill the puzzle",
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

        for (y in 0 until board.height) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RowClue(puzzle.rows[y])
                for (x in 0 until board.width) {
                    Cell(
                        state = board.get(x, y),
                        size = CELL,
                        onTap = { viewModel.onCellTap(x, y) },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        TapModeToggle(
            current = tapMode,
            onToggle = viewModel::toggleTapMode,
        )
    }
}

@Composable
private fun TapModeToggle(
    current: TapMode,
    onToggle: () -> Unit,
) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Fill side: a filled square glyph
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .background(
                        color = if (current == TapMode.Fill) active else inactive,
                        shape = RoundedCornerShape(3.dp),
                    ),
        )

        Spacer(Modifier.width(12.dp))

        Switch(
            checked = current == TapMode.Mark,
            onCheckedChange = { onToggle() },
        )

        Spacer(Modifier.width(12.dp))

        // Mark side: an X glyph
        Text(
            text = "✕",
            style = MaterialTheme.typography.titleLarge,
            color = if (current == TapMode.Mark) active else inactive,
        )
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
    onTap: () -> Unit,
) {
    val color = when (state) {
        CellState.Empty -> MaterialTheme.colorScheme.surfaceVariant
        CellState.Filled -> MaterialTheme.colorScheme.primary
        CellState.Marked -> MaterialTheme.colorScheme.tertiary
    }
    Box(
        modifier = Modifier
            .size(size)
            .padding(2.dp)
            .background(color = color, shape = RoundedCornerShape(4.dp))
            .clickable { onTap() },
    )
}
