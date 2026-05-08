package com.example.nonono

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nonono.ui.game.GameMode
import com.example.nonono.ui.game.GameScreen
import com.example.nonono.ui.home.HomeScreen
import com.example.nonono.ui.levels.LevelsScreen
import com.example.nonono.ui.settings.SettingsScreen
import kotlin.random.Random

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Home)
    val padded = Modifier.safeDrawingPadding().padding(16.dp)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Home> {
                    HomeScreen(
                        onPlayDaily = { backStack.add(GameDaily) },
                        onLevels = { backStack.add(Levels) },
                        onEndless = { backStack.add(GameEndless(Random.nextLong())) },
                        onSettings = { backStack.add(Settings) },
                        modifier = padded,
                    )
                }
                entry<Settings> {
                    SettingsScreen(
                        onHome = { backStack.removeLastOrNull() },
                        modifier = padded,
                    )
                }
                entry<Levels> {
                    LevelsScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onLevel = { index -> backStack.add(GameLevel(index)) },
                        modifier = padded,
                    )
                }
                entry<GameDaily> {
                    GameScreen(
                        mode = GameMode.Daily,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = padded,
                    )
                }
                entry<GameLevel> { key ->
                    GameScreen(
                        mode = GameMode.Level(key.index),
                        onBack = { backStack.removeLastOrNull() },
                        modifier = padded,
                    )
                }
                entry<GameEndless> { key ->
                    GameScreen(
                        mode = GameMode.Endless(key.seed),
                        onBack = { backStack.removeLastOrNull() },
                        modifier = padded,
                    )
                }
            },
    )
}
