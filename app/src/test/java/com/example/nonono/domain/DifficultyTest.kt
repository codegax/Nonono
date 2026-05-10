package com.example.nonono.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DifficultyTest {
    @Test
    fun forEpochDayCyclesAcrossThreeTiers() {
        assertEquals(Difficulty.Easy, Difficulty.forEpochDay(0))
        assertEquals(Difficulty.Medium, Difficulty.forEpochDay(1))
        assertEquals(Difficulty.Hard, Difficulty.forEpochDay(2))
        assertEquals(Difficulty.Easy, Difficulty.forEpochDay(3))
        assertEquals(Difficulty.Medium, Difficulty.forEpochDay(4))
    }

    @Test
    fun forEpochDayHandlesNegativeAndLargeValues() {
        assertEquals(Difficulty.Easy, Difficulty.forEpochDay(-3))
        assertEquals(Difficulty.Easy, Difficulty.forEpochDay(20_001))
    }

    @Test
    fun forLevelIndexFollowsBuckets() {
        assertEquals(Difficulty.Easy, Difficulty.forLevelIndex(0))
        assertEquals(Difficulty.Easy, Difficulty.forLevelIndex(2))
        assertEquals(Difficulty.Medium, Difficulty.forLevelIndex(3))
        assertEquals(Difficulty.Medium, Difficulty.forLevelIndex(5))
        assertEquals(Difficulty.Hard, Difficulty.forLevelIndex(6))
        assertEquals(Difficulty.Hard, Difficulty.forLevelIndex(9))
        assertEquals(Difficulty.Hard, Difficulty.forLevelIndex(99))
    }

    @Test
    fun forSeedIsDeterministic() {
        assertEquals(Difficulty.forSeed(12345L), Difficulty.forSeed(12345L))
        assertEquals(Difficulty.forSeed(-987L), Difficulty.forSeed(-987L))
    }

    @Test
    fun sizesMatchSpec() {
        assertEquals(5, Difficulty.Easy.size)
        assertEquals(10, Difficulty.Medium.size)
        assertEquals(15, Difficulty.Hard.size)
    }

    @Test
    fun fillRateClimbsWithSize() {
        val rates = Difficulty.entries.map { it.fillRate }
        assertEquals(rates.sorted(), rates)
    }
}
