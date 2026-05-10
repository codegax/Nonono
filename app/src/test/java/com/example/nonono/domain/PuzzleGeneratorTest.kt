package com.example.nonono.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleGeneratorTest {
    @Test
    fun generatesUniquelySolvable5x5() {
        val level = generateLevel(name = "Test", seed = 42L, width = 5, height = 5)
        assertNotNull(level)
        assertTrue(solve(level!!.puzzle) is SolveResult.Unique)
    }

    @Test
    fun sameSeedProducesSameLevel() {
        val a = generateLevel(name = "A", seed = 1234L, width = 5, height = 5)
        val b = generateLevel(name = "B", seed = 1234L, width = 5, height = 5)
        assertEquals(a?.solution, b?.solution)
    }

    @Test
    fun differentSeedsProduceDifferentLevels() {
        val a = generateLevel(name = "A", seed = 1L, width = 5, height = 5)
        val b = generateLevel(name = "B", seed = 2L, width = 5, height = 5)
        assertNotNull(a)
        assertNotNull(b)
        assertTrue(a!!.solution != b!!.solution)
    }

    @Test
    fun cluesRoundTripThroughSolution() {
        val level = generateLevel(name = "Test", seed = 7L, width = 5, height = 5)!!
        val derivedRows = (0 until level.solution.height).map { y -> cluesOf(level.solution.row(y)) }
        val derivedCols = (0 until level.solution.width).map { x -> cluesOf(level.solution.column(x)) }
        assertEquals(level.puzzle.rows, derivedRows)
        assertEquals(level.puzzle.cols, derivedCols)
    }

    @Test
    fun generatesAcrossManySeeds() {
        for (seed in 1L..20L) {
            val level = generateLevel(name = "S$seed", seed = seed, width = 5, height = 5)
            assertNotNull("seed $seed should generate within attempts", level)
        }
    }

    @Test
    fun generatesEveryTierUniquely() {
        for (tier in Difficulty.entries) {
            val level = generateLevel(
                name = tier.displayName,
                seed = 17L + tier.ordinal,
                width = tier.size,
                height = tier.size,
                fillRate = tier.fillRate,
            )
            assertNotNull("${tier.displayName} should generate", level)
            assertTrue(
                "${tier.displayName} should be uniquely solvable",
                solve(level!!.puzzle) is SolveResult.Unique,
            )
        }
    }
}
