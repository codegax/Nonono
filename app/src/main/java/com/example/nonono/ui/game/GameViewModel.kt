package com.example.nonono.ui.game

import androidx.lifecycle.ViewModel
import com.example.nonono.domain.Board
import com.example.nonono.domain.CellState
import com.example.nonono.domain.GameStatus
import com.example.nonono.domain.Puzzle
import com.example.nonono.domain.TapMode
import com.example.nonono.domain.samplePuzzle
import com.example.nonono.domain.samplePuzzleSolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    val puzzle: Puzzle = samplePuzzle()
    val solution: Board = samplePuzzleSolution()

    private val _board = MutableStateFlow(emptyBoardFor(puzzle))
    val board: StateFlow<Board> = _board.asStateFlow()

    private val _gameStatus = MutableStateFlow(GameStatus.Playing)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _tapMode = MutableStateFlow(TapMode.Fill)
    val tapMode: StateFlow<TapMode> = _tapMode.asStateFlow()

    fun onCellTap(
        x: Int,
        y: Int,
    ) {
        val current = _board.value
        if (_gameStatus.value == GameStatus.Playing && current.get(x, y) == CellState.Empty) {
            val solutionState = if (solution.get(x, y) == CellState.Filled) CellState.Filled else CellState.Marked
            val stateSelected =
                when (_tapMode.value) {
                    TapMode.Fill -> CellState.Filled
                    TapMode.Mark -> CellState.Marked
                }

            val updated = current.set(x, y, solutionState)
            _board.value = updated
            if (solutionState == stateSelected) {
                if (updated.matchesFills(solution)) _gameStatus.value = GameStatus.Won

                val solutionRow = solution.row(y)
                val solutionColumn = solution.column(x)
                val updatedRow = updated.row(y)
                val updatedColumn = updated.column(x)

                if (solutionRow.count { it == CellState.Filled } == updatedRow.count { it == CellState.Filled }) {
                    _board.value = fillRow(y, updated, solution)
                }
                if (solutionColumn.count { it == CellState.Filled } == updatedColumn.count { it == CellState.Filled }) {
                    _board.value = fillColumn(x, updated, solution)
                }
            } else {
                val newLives = _lives.value - 1
                _lives.value = newLives
                if (newLives < 1) _gameStatus.value = GameStatus.Lost
            }
        }
    }

    fun fillRow(
        y: Int,
        current: Board,
        solution: Board,
    ): Board {
        val newCells = current.cells.toMutableList()
        for (x in 0 until current.width) {
            val target = if (solution.get(x, y) == CellState.Filled) CellState.Filled else CellState.Marked
            newCells[y * current.width + x] = target
        }
        return current.copy(cells = newCells)
    }

    fun fillColumn(
        x: Int,
        current: Board,
        solution: Board,
    ): Board {
        val newCells = current.cells.toMutableList()
        for (y in 0 until current.height) {
            val target = if (solution.get(x, y) == CellState.Filled) CellState.Filled else CellState.Marked
            newCells[y * current.width + x] = target
        }
        return current.copy(cells = newCells)
    }

    fun toggleTapMode() {
        _tapMode.value = if (_tapMode.value == TapMode.Fill) TapMode.Mark else TapMode.Fill
    }

    fun reset() {
        _board.value = emptyBoardFor(puzzle)
        _gameStatus.value = GameStatus.Playing
        _lives.value = 3
        _tapMode.value = TapMode.Fill
    }
}

private fun emptyBoardFor(puzzle: Puzzle): Board {
    val width = puzzle.cols.size
    val height = puzzle.rows.size
    return Board(
        width = width,
        height = height,
        cells = List(width * height) { CellState.Empty },
    )
}

private fun Board.matchesFills(solution: Board): Boolean {
    if (width != solution.width || height != solution.height) return false
    return cells.indices.all { i ->
        (cells[i] == CellState.Filled) == (solution.cells[i] == CellState.Filled)
    }
}
