package com.example.nonono

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey

@Serializable data object Game : NavKey

@Serializable data object Settings : NavKey
