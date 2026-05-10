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
import com.example.nonono.domain.Difficulty
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

    private val _level = MutableStateFlow<Level?>(null)
    val level: StateFlow<Level?> = _level.asStateFlow()

    private val _board = MutableStateFlow<Board?>(null)
    val board: StateFlow<Board?> = _board.asStateFlow()

    private val _gameStatus = MutableStateFlow(GameStatus.Playing)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _tapMode = MutableStateFlow(TapMode.Fill)
    val tapMode: StateFlow<TapMode> = _tapMode.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()
    private var sessionStartMs: Long? = null

    private val _mistakeFlash = MutableStateFlow<MistakeFlash?>(null)
    val mistakeFlash: StateFlow<MistakeFlash?> = _mistakeFlash.asStateFlow()
    private var mistakeCounter = 0L

    private val initialized = MutableStateFlow(false)

    val canRestart: Boolean get() = mode !is GameMode.Daily
    val canNextPuzzle: Boolean get() = mode is GameMode.Endless
    val isDaily: Boolean get() = mode is GameMode.Daily

    init {
        viewModelScope.launch {
            val generated = generateForMode(mode)
            _level.value = generated

            if (mode is GameMode.Daily) {
                val saved = inProgressRepo.load()
                if (saved != null) {
                    _board.value = saved.board
                    _lives.value = saved.lives
                    _gameStatus.value = saved.status
                } else {
                    _board.value = emptyBoardFor(generated.puzzle)
                }
                initialized.value = true
                combine(_board, _lives, _gameStatus) { b, l, s -> Triple(b, l, s) }
                    .drop(1)
                    .collect { (board, lives, status) ->
                        if (board == null) return@collect
                        if (status == GameStatus.Playing) {
                            inProgressRepo.save(board, lives, status)
                        } else {
                            inProgressRepo.clear()
                        }
                    }
            } else {
                _board.value = emptyBoardFor(generated.puzzle)
                initialized.value = true
            }
        }
    }

    fun onCellTap(x: Int, y: Int) {
        if (!initialized.value) return
        val level = _level.value ?: return
        val current = _board.value ?: return
        val solution = level.solution
        if (_gameStatus.value != GameStatus.Playing) return
        if (current.get(x, y) != CellState.Empty) return

        if (sessionStartMs == null) sessionStartMs = System.currentTimeMillis()

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
            mistakeCounter += 1
            _mistakeFlash.value = MistakeFlash(x = x, y = y, id = mistakeCounter)
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
        val solution = _level.value?.solution ?: return
        val board = _board.value ?: return
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
        val solution = _level.value?.solution ?: return
        val board = _board.value ?: return
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
        val level = _level.value ?: return
        _board.value = emptyBoardFor(level.puzzle)
        _gameStatus.value = GameStatus.Playing
        _lives.value = 3
        _tapMode.value = TapMode.Fill
        sessionStartMs = null
        _elapsedMs.value = 0L
    }

    fun nextPuzzle() {
        if (!canNextPuzzle) return
        viewModelScope.launch {
            initialized.value = false
            _level.value = null
            _board.value = null
            val seed = Random.nextLong()
            val difficulty = Difficulty.forSeed(seed)
            val nextLevel = generateLevel(
                name = "Endless · ${difficulty.displayName}",
                seed = seed,
                width = difficulty.size,
                height = difficulty.size,
                fillRate = difficulty.fillRate,
            ) ?: samplePuzzles.first()
            _level.value = nextLevel
            _board.value = emptyBoardFor(nextLevel.puzzle)
            _gameStatus.value = GameStatus.Playing
            _lives.value = 3
            _tapMode.value = TapMode.Fill
            sessionStartMs = null
            _elapsedMs.value = 0L
            initialized.value = true
        }
    }

    private fun setStatus(newStatus: GameStatus) {
        if (_gameStatus.value == newStatus) return
        _gameStatus.value = newStatus
        if (newStatus != GameStatus.Won && newStatus != GameStatus.Lost) return

        val start = sessionStartMs
        _elapsedMs.value = if (start != null) System.currentTimeMillis() - start else 0L

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

data class MistakeFlash(val x: Int, val y: Int, val id: Long)

private const val STAGGER_MS = 40L

private fun generateForMode(mode: GameMode): Level {
    val (difficulty, seed, name) = when (mode) {
        is GameMode.Daily -> Triple(
            Difficulty.forEpochDay(currentEpochDay()),
            currentEpochDay(),
            "Today",
        )
        is GameMode.Level -> Triple(
            Difficulty.forLevelIndex(mode.index),
            currentEpochDay() * 10L + mode.index,
            "Level ${mode.index + 1}",
        )
        is GameMode.Endless -> Triple(
            Difficulty.forSeed(mode.seed),
            mode.seed,
            "Endless",
        )
    }
    return generateLevel(
        name = "$name · ${difficulty.displayName}",
        seed = seed,
        width = difficulty.size,
        height = difficulty.size,
        fillRate = difficulty.fillRate,
    ) ?: samplePuzzles.first()
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
