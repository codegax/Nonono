package com.example.nonono.domain

sealed class SolveResult {
    data object None : SolveResult()
    data class Unique(val solution: Board) : SolveResult()
    data class Multiple(val first: Board, val second: Board) : SolveResult()
}

fun solve(puzzle: Puzzle): SolveResult {
    val width = puzzle.cols.size
    val height = puzzle.rows.size
    val initial = Board(width, height, List(width * height) { CellState.Empty })
    val solutions = mutableListOf<Board>()
    search(puzzle, initial, solutions, limit = 2)
    return when (solutions.size) {
        0 -> SolveResult.None
        1 -> SolveResult.Unique(solutions[0])
        else -> SolveResult.Multiple(solutions[0], solutions[1])
    }
}

private fun search(puzzle: Puzzle, start: Board, solutions: MutableList<Board>, limit: Int) {
    val board = propagate(puzzle, start) ?: return
    val firstEmpty = board.cells.indexOf(CellState.Empty)
    if (firstEmpty == -1) {
        solutions.add(board.asSolution())
        return
    }
    if (solutions.size >= limit) return

    val x = firstEmpty % board.width
    val y = firstEmpty / board.width
    search(puzzle, board.set(x, y, CellState.Filled), solutions, limit)
    if (solutions.size >= limit) return
    search(puzzle, board.set(x, y, CellState.Marked), solutions, limit)
}

private fun Board.asSolution(): Board = copy(
    cells = cells.map { if (it == CellState.Filled) CellState.Filled else CellState.Empty },
)

private fun propagate(puzzle: Puzzle, board: Board): Board? {
    var current = board
    var changed = true
    while (changed) {
        changed = false
        for (y in 0 until current.height) {
            val row = current.row(y)
            val solved = solveLine(puzzle.rows[y], row) ?: return null
            if (solved != row) {
                changed = true
                for (x in 0 until current.width) {
                    if (solved[x] != row[x]) current = current.set(x, y, solved[x])
                }
            }
        }
        for (x in 0 until current.width) {
            val col = current.column(x)
            val solved = solveLine(puzzle.cols[x], col) ?: return null
            if (solved != col) {
                changed = true
                for (y in 0 until current.height) {
                    if (solved[y] != col[y]) current = current.set(x, y, solved[y])
                }
            }
        }
    }
    return current
}
