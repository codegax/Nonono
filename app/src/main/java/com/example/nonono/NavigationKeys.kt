package com.example.nonono

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey

@Serializable data object Settings : NavKey

@Serializable data object Levels : NavKey

@Serializable data object GameDaily : NavKey

@Serializable data class GameLevel(val index: Int) : NavKey

@Serializable data class GameEndless(val seed: Long) : NavKey
