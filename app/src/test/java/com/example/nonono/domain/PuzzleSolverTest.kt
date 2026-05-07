package com.example.nonono.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleSolverTest {
    @Test
    fun solvesFirstSamplePuzzle() {
        val level = samplePuzzles.first()
        val result = solve(level.puzzle)
        assertTrue("expected unique, got $result", result is SolveResult.Unique)
        assertEquals(level.solution, (result as SolveResult.Unique).solution)
    }

    @Test
    fun allSamplePuzzlesAreSolvable() {
        for (level in samplePuzzles) {
            val result = solve(level.puzzle)
            assertTrue("${level.name} should yield a solution", result !is SolveResult.None)
        }
    }

    @Test
    fun ambiguous2x2HasMultipleSolutions() {
        val puzzle = Puzzle(
            rows = listOf(listOf(1), listOf(1)),
            cols = listOf(listOf(1), listOf(1)),
        )
        assertTrue(solve(puzzle) is SolveResult.Multiple)
    }

    @Test
    fun impossibleClueReturnsNone() {
        val puzzle = Puzzle(
            rows = listOf(listOf(3), listOf(0)),
            cols = listOf(listOf(1), listOf(1)),
        )
        assertTrue(solve(puzzle) is SolveResult.None)
    }
}
