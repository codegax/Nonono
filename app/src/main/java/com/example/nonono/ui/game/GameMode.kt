package com.example.nonono.ui.game

sealed class GameMode {
    data object Daily : GameMode()
    data class Level(val index: Int) : GameMode()
    data class Endless(val seed: Long) : GameMode()
}
