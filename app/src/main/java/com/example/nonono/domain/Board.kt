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

    fun row(y: Int): List<CellState> = cells.subList(y * width, (y + 1) * width)

    fun column(x: Int): List<CellState> = (0 until height).map { i -> cells[i * width + x] }
}
