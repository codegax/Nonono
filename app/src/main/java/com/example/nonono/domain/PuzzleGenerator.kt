package com.example.nonono.domain

import kotlin.random.Random

fun generateLevel(
    name: String,
    seed: Long,
    width: Int,
    height: Int,
    fillRate: Double = 0.50,
    maxAttempts: Int = 500,
): Level? {
    val random = Random(seed)
    repeat(maxAttempts) {
        val cells = List(width * height) {
            if (random.nextDouble() < fillRate) CellState.Filled else CellState.Empty
        }
        val solution = Board(width, height, cells)
        if (solution.hasAnyEmptyLine()) return@repeat

        val puzzle = puzzleFor(solution)
        val result = solve(puzzle)
        if (result is SolveResult.Unique) {
            return Level(name = name, puzzle = puzzle, solution = result.solution)
        }
    }
    return null
}

fun puzzleFor(solution: Board): Puzzle {
    val rows = (0 until solution.height).map { y -> cluesOf(solution.row(y)) }
    val cols = (0 until solution.width).map { x -> cluesOf(solution.column(x)) }
    return Puzzle(rows = rows, cols = cols)
}

private fun Board.hasAnyEmptyLine(): Boolean {
    for (y in 0 until height) if (row(y).none { it == CellState.Filled }) return true
    for (x in 0 until width) if (column(x).none { it == CellState.Filled }) return true
    return false
}
