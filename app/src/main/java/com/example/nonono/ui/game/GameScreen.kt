package com.example.nonono.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Application
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonono.domain.CellState
import com.example.nonono.domain.GameStatus
import com.example.nonono.domain.Level
import com.example.nonono.domain.Puzzle
import com.example.nonono.domain.TapMode
import kotlin.math.abs

@Composable
fun GameScreen(
    mode: GameMode,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(
        key = mode.toString(),
        factory = GameViewModel.factory(LocalContext.current.applicationContext as Application, mode),
    ),
) {
    val level by viewModel.level.collectAsStateWithLifecycle()
    val board by viewModel.board.collectAsStateWithLifecycle()
    val gameStatus by viewModel.gameStatus.collectAsStateWithLifecycle()
    val lives by viewModel.lives.collectAsStateWithLifecycle()
    val tapMode by viewModel.tapMode.collectAsStateWithLifecycle()
    val elapsedMs by viewModel.elapsedMs.collectAsStateWithLifecycle()
    val mistakeFlash by viewModel.mistakeFlash.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                level = level,
                lives = lives,
                canRestart = viewModel.canRestart,
                canNextPuzzle = viewModel.canNextPuzzle,
                onBack = onBack,
                onRestart = viewModel::reset,
                onNewPuzzle = viewModel::nextPuzzle,
            )

            AnimatedVisibility(visible = gameStatus == GameStatus.Won) {
                val (label, action) = winAction(viewModel.mode, viewModel::nextPuzzle, onBack)
                ResultPanel(
                    won = true,
                    title = if (viewModel.isDaily) "Daily complete" else "You won",
                    timeMs = elapsedMs,
                    livesRemaining = lives,
                    actionLabel = label,
                    onAction = action,
                )
            }
            AnimatedVisibility(visible = gameStatus == GameStatus.Lost) {
                val (label, action) = lossAction(viewModel.mode, viewModel::reset)
                ResultPanel(
                    won = false,
                    title = if (viewModel.isDaily) "Come back tomorrow" else "Out of lives",
                    timeMs = elapsedMs,
                    livesRemaining = lives,
                    actionLabel = label,
                    onAction = action,
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val currentLevel = level
                val currentBoard = board
                if (currentLevel == null || currentBoard == null) {
                    CircularProgressIndicator()
                } else {
                    val puzzle = currentLevel.puzzle
                    val maxRowClues = puzzle.rows.maxOfOrNull { it.size } ?: 1
                    val maxColClues = puzzle.cols.maxOfOrNull { it.size } ?: 1
                    val rowGutterUnits = (maxRowClues * 0.7f).coerceAtLeast(1.2f)
                    val colGutterUnits = (maxColClues * 1.0f).coerceAtLeast(1.2f)
                    val widthUnits = currentBoard.width + rowGutterUnits + rowGutterUnits
                    val heightUnits = currentBoard.height + colGutterUnits
                    val cellByWidth = maxWidth / widthUnits
                    val cellByHeight = maxHeight / heightUnits
                    val cell = minOf(cellByWidth, cellByHeight).coerceIn(12.dp, 48.dp)
                    val rowGutter = cell * rowGutterUnits
                    val colGutter = cell * colGutterUnits
                    val cellPx = with(density) { cell.toPx() }
                    val majorLinePx = with(density) { 2.dp.toPx() }
                    val majorLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ColumnClues(puzzle = puzzle, cell = cell, rowGutter = rowGutter, colGutter = colGutter)

                        Row(verticalAlignment = Alignment.Top) {
                            Column {
                                for (y in 0 until currentBoard.height) {
                                    RowClue(clue = puzzle.rows[y], cell = cell, gutter = rowGutter)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .pointerInput(currentBoard.width, currentBoard.height, cellPx) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)

                                        fun cellAt(offset: Offset): Pair<Int, Int>? {
                                            val cx = (offset.x / cellPx).toInt()
                                            val cy = (offset.y / cellPx).toInt()
                                            return if (cx in 0 until currentBoard.width && cy in 0 until currentBoard.height) {
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

                                            val cellHit = cellAt(change.position) ?: continue
                                            val (cx, cy) = cellHit

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
                                }
                                    .drawWithContent {
                                        drawContent()
                                        for (i in 5 until currentBoard.width step 5) {
                                            val x = i * cellPx
                                            drawLine(
                                                color = majorLineColor,
                                                start = Offset(x, 0f),
                                                end = Offset(x, size.height),
                                                strokeWidth = majorLinePx,
                                            )
                                        }
                                        for (i in 5 until currentBoard.height step 5) {
                                            val y = i * cellPx
                                            drawLine(
                                                color = majorLineColor,
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = majorLinePx,
                                            )
                                        }
                                    },
                            ) {
                                Column {
                                    for (y in 0 until currentBoard.height) {
                                        Row {
                                            for (x in 0 until currentBoard.width) {
                                                val flashKey = mistakeFlash
                                                    ?.takeIf { it.x == x && it.y == y }
                                                    ?.id
                                                Cell(
                                                    state = currentBoard.get(x, y),
                                                    size = cell,
                                                    flashKey = flashKey,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.width(rowGutter))
                        }
                    }
                }
            }

            BottomBar(
                tapMode = tapMode,
                enabled = gameStatus == GameStatus.Playing && level != null,
                onToggle = viewModel::toggleTapMode,
            )
        }

        Confetti(
            active = gameStatus == GameStatus.Won,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun TopBar(
    level: Level?,
    lives: Int,
    canRestart: Boolean,
    canNextPuzzle: Boolean,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onNewPuzzle: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val hasMenu = canRestart || canNextPuzzle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        ) {
            Text(
                text = level?.name ?: "Loading…",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (level != null) {
                Text(
                    text = "${level.solution.width} × ${level.solution.height}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LivesIndicator(lives = lives, max = 3)

        Spacer(Modifier.width(4.dp))

        if (hasMenu) {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    if (canRestart) {
                        DropdownMenuItem(
                            text = { Text("Restart") },
                            onClick = {
                                menuOpen = false
                                onRestart()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                    if (canNextPuzzle) {
                        DropdownMenuItem(
                            text = { Text("New puzzle") },
                            onClick = {
                                menuOpen = false
                                onNewPuzzle()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LivesIndicator(lives: Int, max: Int) {
    val shake = remember { Animatable(0f) }
    var prevLives by remember { mutableStateOf(lives) }

    LaunchedEffect(lives) {
        if (lives < prevLives) {
            shake.snapTo(0f)
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 360
                    0f at 0
                    -8f at 60
                    8f at 120
                    -6f at 200
                    4f at 280
                    0f at 360
                },
            )
        }
        prevLives = lives
    }

    Row(
        modifier = Modifier.offset(x = shake.value.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (i in 0 until max) {
            val alive = i < lives
            val tint by animateColorAsState(
                targetValue = if (alive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                animationSpec = tween(durationMillis = 280),
                label = "lifeTint",
            )
            val scale by animateFloatAsState(
                targetValue = if (alive) 1f else 0.82f,
                animationSpec = tween(durationMillis = 280),
                label = "lifeScale",
            )
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = if (alive) "Life remaining" else "Life lost",
                tint = tint,
                modifier = Modifier
                    .size(18.dp)
                    .scale(scale),
            )
        }
    }
}

private fun winAction(
    mode: GameMode,
    onNext: () -> Unit,
    onBack: () -> Unit,
): Pair<String?, () -> Unit> = when (mode) {
    is GameMode.Daily -> null to {}
    is GameMode.Level -> "Back to levels" to onBack
    is GameMode.Endless -> "Next" to onNext
}

private fun lossAction(
    mode: GameMode,
    onRestart: () -> Unit,
): Pair<String?, () -> Unit> = when (mode) {
    is GameMode.Daily -> null to {}
    is GameMode.Level, is GameMode.Endless -> "Try again" to onRestart
}

@Composable
private fun ResultPanel(
    won: Boolean,
    title: String,
    timeMs: Long,
    livesRemaining: Int,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    val container = if (won) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val onContainer = if (won) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    val pulse = if (won) {
        val infinite = rememberInfiniteTransition(label = "winPulse")
        val v by infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.015f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "winPulseScale",
        )
        v
    } else {
        1f
    }

    val mistakes = (3 - livesRemaining).coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
        color = container,
        contentColor = onContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = if (won) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatBlock(label = "Time", value = formatElapsed(timeMs))
                if (won) {
                    StatBlock(label = "Mistakes", value = "$mistakes / 3")
                }
            }

            if (actionLabel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onAction) {
                        Text(text = actionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.2.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val m = totalSec / 60L
    val s = totalSec % 60L
    return "%d:%02d".format(m, s)
}


@Composable
private fun BottomBar(
    tapMode: TapMode,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            TapModeToggle(
                current = tapMode,
                enabled = enabled,
                onToggle = onToggle,
            )
        }
    }
}

@Composable
private fun TapModeToggle(
    current: TapMode,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val modes = listOf(TapMode.Fill, TapMode.Mark)
    SingleChoiceSegmentedButtonRow {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = current == mode,
                enabled = enabled,
                onClick = { if (current != mode) onToggle() },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                icon = {
                    when (mode) {
                        TapMode.Fill ->
                            Box(
                                modifier = Modifier
                                    .size(SegmentedButtonDefaults.IconSize)
                                    .background(
                                        color = LocalContentColor.current,
                                        shape = RoundedCornerShape(2.dp),
                                    ),
                            )
                        TapMode.Mark ->
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                            )
                    }
                },
                label = {
                    Text(
                        text = when (mode) {
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
private fun ColumnClues(puzzle: Puzzle, cell: Dp, rowGutter: Dp, colGutter: Dp) {
    val style = clueTextStyle(cell)
    Row(verticalAlignment = Alignment.Bottom) {
        Spacer(Modifier.width(rowGutter))
        for (clue in puzzle.cols) {
            Column(
                modifier = Modifier.size(width = cell, height = colGutter),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (n in clue) {
                    Text(
                        text = n.toString(),
                        style = style,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.width(rowGutter))
    }
}

@Composable
private fun RowClue(clue: List<Int>, cell: Dp, gutter: Dp) {
    val style = clueTextStyle(cell)
    Row(
        modifier = Modifier.size(width = gutter, height = cell),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (n in clue) {
            Text(
                text = n.toString(),
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
}

@Composable
private fun clueTextStyle(cell: Dp) = when {
    cell >= 36.dp -> MaterialTheme.typography.bodyMedium
    cell >= 26.dp -> MaterialTheme.typography.bodySmall
    else -> MaterialTheme.typography.labelSmall
}

private data class Confetto(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val size: Float,
)

@Composable
private fun Confetti(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
    )

    var pieces by remember { mutableStateOf<List<Confetto>>(emptyList()) }
    val time = remember { Animatable(0f) }

    LaunchedEffect(active) {
        if (active) {
            val random = kotlin.random.Random.Default
            pieces = List(60) {
                Confetto(
                    startX = 0.2f + random.nextFloat() * 0.6f,
                    startY = -0.05f + random.nextFloat() * 0.05f,
                    velocityX = random.nextFloat() * 0.6f - 0.3f,
                    velocityY = random.nextFloat() * 0.2f - 0.4f,
                    color = palette[random.nextInt(palette.size)],
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = random.nextFloat() * 4f - 2f,
                    size = 10f + random.nextFloat() * 10f,
                )
            }
            time.snapTo(0f)
            time.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2600, easing = LinearEasing),
            )
            pieces = emptyList()
        } else {
            pieces = emptyList()
        }
    }

    if (pieces.isEmpty()) return

    Canvas(modifier = modifier) {
        val t = time.value
        val w = size.width
        val h = size.height
        for (p in pieces) {
            val x = p.startX * w + p.velocityX * t * w
            val y = p.startY * h + p.velocityY * t * h + 1.6f * t * t * h
            val degrees = p.rotation + p.rotationSpeed * t * 360f
            val alpha = (1f - (t * t)).coerceIn(0f, 1f)
            rotate(degrees = degrees, pivot = Offset(x + p.size / 2f, y + p.size / 2f)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(p.size, p.size * 0.55f),
                )
            }
        }
    }
}

@Composable
private fun Cell(
    state: CellState,
    size: Dp,
    flashKey: Long? = null,
) {
    val targetColor = if (state == CellState.Filled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val errorColor = MaterialTheme.colorScheme.error

    val color = remember { androidx.compose.animation.Animatable(targetColor) }

    LaunchedEffect(targetColor) {
        color.animateTo(targetColor, tween(durationMillis = 160))
    }

    LaunchedEffect(flashKey) {
        if (flashKey != null) {
            color.snapTo(errorColor)
            color.animateTo(targetColor, tween(durationMillis = 450))
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .padding((size * 0.05f).coerceAtLeast(1.dp))
            .background(color = color.value, shape = RoundedCornerShape((size * 0.12f).coerceAtLeast(2.dp))),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = state == CellState.Marked) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Marked",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size).padding((size * 0.2f).coerceAtLeast(2.dp)),
            )
        }
    }
}
