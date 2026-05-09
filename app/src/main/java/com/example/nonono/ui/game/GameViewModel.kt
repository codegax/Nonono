package com.example.nonono.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nonono.data.DailyOutcome
import com.example.nonono.data.DailyPlayRepository
import com.example.nonono.data.InProgressDailyRepository
import com.example.nonono.data.LevelsProgressRepository
import com.example.nonono.data.StreakRepository
import com.example.nonono.data.UserSettingsRepository
import com.example.nonono.data.currentEpochDay
import com.example.nonono.domain.Board
import com.example.nonono.domain.CellState
import com.example.nonono.domain.GameStatus
import com.example.nonono.domain.Level
import com.example.nonono.domain.Puzzle
import com.example.nonono.domain.TapMode
import com.example.nonono.domain.generateLevel
import com.example.nonono.domain.samplePuzzles
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    app: Application,
    val mode: GameMode,
) : AndroidViewModel(app) {
    private val dailyRepo = DailyPlayRepository(app)
    private val levelsRepo = LevelsProgressRepository(app)
    private val inProgressRepo = InProgressDailyRepository(app)
    private val streakRepo = StreakRepository(app)
    private val userSettings = UserSettingsRepository(app)

    private val autoMark: StateFlow<Boolean> = userSettings.automaticRowSolver.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true,
    )

    private val _level = MutableStateFlow(generateForMode(mode))
    val level: StateFlow<Level> = _level.asStateFlow()

    private val _board = MutableStateFlow(emptyBoardFor(_level.value.puzzle))
    val board: StateFlow<Board> = _board.asStateFlow()

    private val _gameStatus = MutableStateFlow(GameStatus.Playing)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _tapMode = MutableStateFlow(TapMode.Fill)
    val tapMode: StateFlow<TapMode> = _tapMode.asStateFlow()

    private val initialized = MutableStateFlow(mode !is GameMode.Daily)

    val canRestart: Boolean get() = mode !is GameMode.Daily
    val canNextPuzzle: Boolean get() = mode is GameMode.Endless
    val isDaily: Boolean get() = mode is GameMode.Daily

    init {
        if (mode is GameMode.Daily) {
            viewModelScope.launch {
                inProgressRepo.load()?.let { saved ->
                    _board.value = saved.board
                    _lives.value = saved.lives
                    _gameStatus.value = saved.status
                }
                initialized.value = true
                combine(_board, _lives, _gameStatus) { b, l, s -> Triple(b, l, s) }
                    .drop(1)
                    .collect { (board, lives, status) ->
                        if (status == GameStatus.Playing) {
                            inProgressRepo.save(board, lives, status)
                        } else {
                            inProgressRepo.clear()
                        }
                    }
            }
        }
    }

    fun onCellTap(x: Int, y: Int) {
        if (!initialized.value) return
        val solution = _level.value.solution
        val current = _board.value
        if (_gameStatus.value != GameStatus.Playing) return
        if (current.get(x, y) != CellState.Empty) return

        val solutionState = if (solution.get(x, y) == CellState.Filled) CellState.Filled else CellState.Marked
        val stateSelected = when (_tapMode.value) {
            TapMode.Fill -> CellState.Filled
            TapMode.Mark -> CellState.Marked
        }

        val updated = current.set(x, y, solutionState)
        _board.value = updated

        if (solutionState == stateSelected) {
            if (updated.matchesFills(solution)) setStatus(GameStatus.Won)
        } else {
            val newLives = _lives.value - 1
            _lives.value = newLives
            if (newLives < 1) setStatus(GameStatus.Lost)
        }

        if (autoMark.value) {
            viewModelScope.launch {
                staggerFillRowIfSatisfied(y)
                staggerFillColumnIfSatisfied(x)
            }
        }
    }

    private suspend fun staggerFillRowIfSatisfied(y: Int) {
        val solution = _level.value.solution
        val board = _board.value
        val solutionFilled = solution.row(y).count { it == CellState.Filled }
        val boardFilled = board.row(y).count { it == CellState.Filled }
        if (solutionFilled != boardFilled) return

        var current = board
        for (x in 0 until current.width) {
            if (current.get(x, y) == CellState.Empty) {
                val target = if (solution.get(x, y) == CellState.Filled) CellState.Filled else CellState.Marked
                current = current.set(x, y, target)
                _board.value = current
                delay(STAGGER_MS)
            }
        }
    }

    private suspend fun staggerFillColumnIfSatisfied(x: Int) {
        val solution = _level.value.solution
        val board = _board.value
        val solutionFilled = solution.column(x).count { it == CellState.Filled }
        val boardFilled = board.column(x).count { it == CellState.Filled }
        if (solutionFilled != boardFilled) return

        var current = board
        for (y in 0 until current.height) {
            if (current.get(x, y) == CellState.Empty) {
                val target = if (solution.get(x, y) == CellState.Filled) CellState.Filled else CellState.Marked
                current = current.set(x, y, target)
                _board.value = current
                delay(STAGGER_MS)
            }
        }
    }

    fun toggleTapMode() {
        _tapMode.value = if (_tapMode.value == TapMode.Fill) TapMode.Mark else TapMode.Fill
    }

    fun reset() {
        if (!canRestart) return
        _board.value = emptyBoardFor(_level.value.puzzle)
        _gameStatus.value = GameStatus.Playing
        _lives.value = 3
        _tapMode.value = TapMode.Fill
    }

    fun nextPuzzle() {
        if (!canNextPuzzle) return
        val nextLevel = generateLevel(name = "Endless", seed = Random.nextLong(), width = GRID_SIZE, height = GRID_SIZE)
            ?: samplePuzzles.first()
        _level.value = nextLevel
        _board.value = emptyBoardFor(nextLevel.puzzle)
        _gameStatus.value = GameStatus.Playing
        _lives.value = 3
        _tapMode.value = TapMode.Fill
    }

    private fun setStatus(newStatus: GameStatus) {
        if (_gameStatus.value == newStatus) return
        _gameStatus.value = newStatus
        if (newStatus != GameStatus.Won && newStatus != GameStatus.Lost) return

        viewModelScope.launch {
            when (val m = mode) {
                is GameMode.Daily -> {
                    val outcome = if (newStatus == GameStatus.Won) DailyOutcome.Won else DailyOutcome.Lost
                    dailyRepo.recordOutcome(outcome)
                    if (newStatus == GameStatus.Won) streakRepo.recordWin() else streakRepo.recordLoss()
                }
                is GameMode.Level -> {
                    if (newStatus == GameStatus.Won) levelsRepo.markSolved(m.index)
                }
                is GameMode.Endless -> Unit
            }
        }
    }

    companion object {
        fun factory(app: Application, mode: GameMode) = viewModelFactory {
            initializer { GameViewModel(app, mode) }
        }
    }
}

private const val STAGGER_MS = 40L
private const val GRID_SIZE = 5

private fun generateForMode(mode: GameMode): Level {
    val seed = when (mode) {
        is GameMode.Daily -> currentEpochDay()
        is GameMode.Level -> currentEpochDay() * 10L + mode.index
        is GameMode.Endless -> mode.seed
    }
    val name = when (mode) {
        is GameMode.Daily -> "Today"
        is GameMode.Level -> "Level ${mode.index + 1}"
        is GameMode.Endless -> "Endless"
    }
    return generateLevel(name = name, seed = seed, width = GRID_SIZE, height = GRID_SIZE)
        ?: samplePuzzles.first()
}

private fun emptyBoardFor(puzzle: Puzzle): Board {
    val width = puzzle.cols.size
    val height = puzzle.rows.size
    return Board(width = width, height = height, cells = List(width * height) { CellState.Empty })
}

private fun Board.matchesFills(solution: Board): Boolean {
    if (width != solution.width || height != solution.height) return false
    return cells.indices.all { i ->
        (cells[i] == CellState.Filled) == (solution.cells[i] == CellState.Filled)
    }
}
