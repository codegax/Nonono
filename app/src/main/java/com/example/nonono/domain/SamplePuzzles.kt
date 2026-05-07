package com.example.nonono.domain

data class Level(
    val name: String,
    val puzzle: Puzzle,
    val solution: Board,
)

val samplePuzzles: List<Level> = listOf(
    level(
        "Heart",
        ".X.X.",
        "XXXXX",
        "XXXXX",
        ".XXX.",
        "..X..",
    ),
    level(
        "Smiley",
        "XX.XX",
        "XX.XX",
        ".....",
        "X...X",
        ".XXX.",
    ),
    level(
        "Arrow Up",
        "..X..",
        ".XXX.",
        "X.X.X",
        "..X..",
        "..X..",
    ),
    level(
        "Letter A",
        "..X..",
        ".X.X.",
        "X...X",
        "XXXXX",
        "X...X",
    ),
    level(
        "Plus",
        "..X..",
        "..X..",
        "XXXXX",
        "..X..",
        "..X..",
    ),
    level(
        "Diamond",
        "..X..",
        ".XXX.",
        "XXXXX",
        ".XXX.",
        "..X..",
    ),
    level(
        "House",
        "..X..",
        ".XXX.",
        "XXXXX",
        "X...X",
        "XXXXX",
    ),
    level(
        "Letter T",
        "XXXXX",
        "..X..",
        "..X..",
        "..X..",
        "..X..",
    ),
    level(
        "Star",
        "..X..",
        "XXXXX",
        ".XXX.",
        "XX.XX",
        "X...X",
    ),
    level(
        "Letter X",
        "X...X",
        ".X.X.",
        "..X..",
        ".X.X.",
        "X...X",
    ),
)

private fun level(name: String, vararg art: String): Level {
    val height = art.size
    val width = art.first().length
    require(art.all { it.length == width }) { "$name: rows must all be the same width" }

    val cells = List(width * height) { i ->
        if (art[i / width][i % width] == 'X') CellState.Filled else CellState.Empty
    }
    val solution = Board(width = width, height = height, cells = cells)

    return Level(name = name, puzzle = puzzleFor(solution), solution = solution)
}
