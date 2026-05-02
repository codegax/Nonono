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

    val state = List(width * height) { i ->
        if (art[i / width][i % width] == 'X') CellState.Filled else CellState.Empty
    }

    fun runs(line: List<CellState>): List<Int> {
        val out = mutableListOf<Int>()
        var n = 0
        for (c in line) {
            if (c == CellState.Filled) {
                n++
            } else if (n > 0) {
                out.add(n); n = 0
            }
        }
        if (n > 0) out.add(n)
        return out.ifEmpty { listOf(0) }
    }

    val rows = (0 until height).map { y ->
        runs((0 until width).map { x -> state[y * width + x] })
    }
    val cols = (0 until width).map { x ->
        runs((0 until height).map { y -> state[y * width + x] })
    }

    return Level(
        name = name,
        puzzle = Puzzle(rows = rows, cols = cols),
        solution = Board(width = width, height = height, cells = state),
    )
}
