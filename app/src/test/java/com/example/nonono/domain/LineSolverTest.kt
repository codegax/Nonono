package com.example.nonono.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LineSolverTest {
    private val E = CellState.Empty
    private val F = CellState.Filled
    private val M = CellState.Marked

    @Test
    fun cluesOfFullRun() {
        assertEquals(listOf(5), cluesOf(listOf(F, F, F, F, F)))
    }

    @Test
    fun cluesOfMixedRuns() {
        assertEquals(listOf(2, 1), cluesOf(listOf(F, F, M, F, M)))
    }

    @Test
    fun cluesOfEmptyLineReturnsZero() {
        assertEquals(listOf(0), cluesOf(listOf(E, E, E, E, E)))
    }

    @Test
    fun cluesOfMarkedTreatedAsEmpty() {
        assertEquals(listOf(1, 2), cluesOf(listOf(F, M, F, F, M)))
    }

    @Test
    fun fullLineForcesAllFilled() {
        assertEquals(listOf(F, F, F, F, F), solveLine(listOf(5), listOf(E, E, E, E, E)))
    }

    @Test
    fun zeroClueForcesAllMarked() {
        assertEquals(listOf(M, M, M, M, M), solveLine(listOf(0), listOf(E, E, E, E, E)))
    }

    @Test
    fun emptyClueListForcesAllMarked() {
        assertEquals(listOf(M, M, M, M, M), solveLine(emptyList(), listOf(E, E, E, E, E)))
    }

    @Test
    fun overlapForcesMiddle() {
        assertEquals(listOf(E, F, F, F, E), solveLine(listOf(4), listOf(E, E, E, E, E)))
    }

    @Test
    fun threeInFiveOnlyForcesCenter() {
        assertEquals(listOf(E, E, F, E, E), solveLine(listOf(3), listOf(E, E, E, E, E)))
    }

    @Test
    fun knownFilledNarrowsPlacements() {
        assertEquals(listOf(M, M, F, F, F), solveLine(listOf(3), listOf(E, E, E, E, F)))
    }

    @Test
    fun knownMarkedNarrowsPlacements() {
        assertEquals(listOf(F, F, F, M, M), solveLine(listOf(3), listOf(E, E, E, M, E)))
    }

    @Test
    fun cluesTooLargeReturnsNull() {
        assertNull(solveLine(listOf(6), listOf(E, E, E, E, E)))
    }

    @Test
    fun conflictingKnownReturnsNull() {
        assertNull(solveLine(listOf(2), listOf(F, M, F, E, E)))
    }
}
