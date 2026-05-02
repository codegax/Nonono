package com.example.nonono

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nonono.ui.home.HomeScreen
import com.example.nonono.ui.game.GameScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Home> {
                    HomeScreen(
                        onPlay = { backStack.add(Game) },
                        modifier = Modifier.safeDrawingPadding().padding(16.dp),
                    )
                }
                entry<Game> {
                    GameScreen(
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier.safeDrawingPadding().padding(16.dp),
                    )
                }
            },
    )
}
