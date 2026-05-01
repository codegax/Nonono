package com.example.nonono.domain

fun samplePuzzle(): Puzzle =
    Puzzle(
        rows =
            listOf(
                listOf(1, 1),
                listOf(5),
                listOf(5),
                listOf(3),
                listOf(1),
            ),
        cols =
            listOf(
                listOf(2),
                listOf(4),
                listOf(4),
                listOf(4),
                listOf(2),
            ),
    )

fun samplePuzzleSolution(): Board {
    val width = 5
    val height = 5
    val F = CellState.Filled
    val E = CellState.Empty
    val cells =
        listOf( E, F, E, F, E, F, F, F, F, F, F, F, F, F, F, E, F, F, F, E, E, E, F, E, E,)
        /*
            E, F, E, F, E,
            F, F, F, F, F,
            F, F, F, F, F,
            E, F, F, F, E,
            E, E, F, E, E,
         */
    return Board(width = width, height = height, cells = cells)
}
