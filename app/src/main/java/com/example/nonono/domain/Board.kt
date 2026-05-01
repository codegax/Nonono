package com.example.nonono.domain

data class Board(
    val width: Int,
    val height: Int,
    val cells: List<CellState>,
) {
    fun get(
        x: Int,
        y: Int,
    ): CellState = cells[y * width + x]

    fun set(
        x: Int,
        y: Int,
        state: CellState,
    ): Board =
        copy(
            cells =
                cells.toMutableList().also {
                    it[y * width + x] = state
                },
        )
}
