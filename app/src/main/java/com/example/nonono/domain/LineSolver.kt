package com.example.nonono.domain

fun cluesOf(line: List<CellState>): List<Int> {
    val runs = mutableListOf<Int>()
    var n = 0
    for (cell in line) {
        if (cell == CellState.Filled) {
            n++
        } else if (n > 0) {
            runs.add(n)
            n = 0
        }
    }
    if (n > 0) runs.add(n)
    return runs
}

/**
 * Returns the line with cells upgraded to Filled or Marked wherever the clue forces them.
 * Cells that remain ambiguous keep their existing state. Returns null if no run placement
 * is consistent with both the clue and the cells already known in the line.
 */
fun solveLine(clues: List<Int>, line: List<CellState>): List<CellState>? {
    val width = line.size
    val placements = allPlacements(clues, width).filter { it.fits(line) }
    if (placements.isEmpty()) return null

    return List(width) { i ->
        when {
            placements.all { it[i] == CellState.Filled } -> CellState.Filled
            placements.all { it[i] == CellState.Marked } -> CellState.Marked
            else -> line[i]
        }
    }
}

private fun allPlacements(clues: List<Int>, width: Int): List<List<CellState>> {
    val noRuns = clues.isEmpty() || (clues.size == 1 && clues[0] == 0)
    if (noRuns) return listOf(List(width) { CellState.Marked })

    val out = mutableListOf<List<CellState>>()
    val current = ArrayList<CellState>(width)

    fun place(remaining: List<Int>) {
        if (remaining.isEmpty()) {
            out.add(current + List(width - current.size) { CellState.Marked })
            return
        }
        val run = remaining.first()
        val rest = remaining.drop(1)
        // Reserve cells for runs we still need to place: each remaining run + 1 separator before it.
        val minTail = rest.sum() + rest.size
        val maxGap = width - current.size - run - minTail
        if (maxGap < 0) return

        for (gap in 0..maxGap) {
            val saved = current.size
            repeat(gap) { current.add(CellState.Marked) }
            repeat(run) { current.add(CellState.Filled) }
            if (rest.isNotEmpty()) current.add(CellState.Marked)
            place(rest)
            while (current.size > saved) current.removeAt(current.size - 1)
        }
    }
    place(clues)
    return out
}

private fun List<CellState>.fits(known: List<CellState>): Boolean {
    for (i in indices) {
        val k = known[i]
        if (k != CellState.Empty && k != this[i]) return false
    }
    return true
}
