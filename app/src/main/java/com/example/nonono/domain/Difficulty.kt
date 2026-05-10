package com.example.nonono.domain

enum class Difficulty(val displayName: String, val size: Int, val fillRate: Double) {
    Easy("Easy", 5, 0.50),
    Medium("Medium", 10, 0.58),
    Hard("Hard", 15, 0.62);

    companion object {
        fun forEpochDay(day: Long): Difficulty =
            entries[day.mod(entries.size.toLong()).toInt()]

        fun forLevelIndex(index: Int): Difficulty = when (index) {
            in 0..2 -> Easy
            in 3..5 -> Medium
            else -> Hard
        }

        fun forSeed(seed: Long): Difficulty =
            entries[seed.mod(entries.size.toLong()).toInt()]
    }
}
