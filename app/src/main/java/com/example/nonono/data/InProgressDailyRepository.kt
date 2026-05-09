package com.example.nonono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.nonono.domain.Board
import com.example.nonono.domain.CellState
import com.example.nonono.domain.GameStatus
import kotlinx.coroutines.flow.first

private val Context.inProgressStore: DataStore<Preferences> by preferencesDataStore(name = "in_progress_daily")

private object InProgressKeys {
    val EpochDay = longPreferencesKey("epoch_day")
    val Width = intPreferencesKey("width")
    val Height = intPreferencesKey("height")
    val Cells = stringPreferencesKey("cells")
    val Lives = intPreferencesKey("lives")
    val Status = stringPreferencesKey("status")
}

data class InProgressDaily(
    val board: Board,
    val lives: Int,
    val status: GameStatus,
)

class InProgressDailyRepository(context: Context) {
    private val store = context.applicationContext.inProgressStore

    suspend fun load(): InProgressDaily? {
        val prefs = store.data.first()
        val storedDay = prefs[InProgressKeys.EpochDay] ?: return null
        if (storedDay != currentEpochDay()) return null

        val width = prefs[InProgressKeys.Width] ?: return null
        val height = prefs[InProgressKeys.Height] ?: return null
        val cells = prefs[InProgressKeys.Cells]
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull()?.let(CellState.entries::getOrNull) }
            ?: return null
        if (cells.size != width * height) return null
        val lives = prefs[InProgressKeys.Lives] ?: return null
        val status = prefs[InProgressKeys.Status]?.let(GameStatus::valueOf) ?: return null

        return InProgressDaily(
            board = Board(width = width, height = height, cells = cells),
            lives = lives,
            status = status,
        )
    }

    suspend fun save(board: Board, lives: Int, status: GameStatus) {
        store.edit { prefs ->
            prefs[InProgressKeys.EpochDay] = currentEpochDay()
            prefs[InProgressKeys.Width] = board.width
            prefs[InProgressKeys.Height] = board.height
            prefs[InProgressKeys.Cells] = board.cells.joinToString(",") { it.ordinal.toString() }
            prefs[InProgressKeys.Lives] = lives
            prefs[InProgressKeys.Status] = status.name
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }
}
