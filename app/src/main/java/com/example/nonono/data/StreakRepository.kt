package com.example.nonono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.streakStore: DataStore<Preferences> by preferencesDataStore(name = "daily_streak")

private object StreakKeys {
    val LastWinDay = longPreferencesKey("last_win_day")
    val Streak = intPreferencesKey("streak")
}

class StreakRepository(context: Context) {
    private val store = context.applicationContext.streakStore

    val current: Flow<Int> = store.data.map { prefs ->
        val lastWin = prefs[StreakKeys.LastWinDay] ?: -1L
        val stored = prefs[StreakKeys.Streak] ?: 0
        if (lastWin >= currentEpochDay() - 1L) stored else 0
    }

    suspend fun recordWin() {
        store.edit { prefs ->
            val today = currentEpochDay()
            val lastWin = prefs[StreakKeys.LastWinDay] ?: -1L
            val previous = prefs[StreakKeys.Streak] ?: 0
            val next = when {
                lastWin == today -> previous
                lastWin == today - 1L -> previous + 1
                else -> 1
            }
            prefs[StreakKeys.LastWinDay] = today
            prefs[StreakKeys.Streak] = next
        }
    }

    suspend fun recordLoss() {
        store.edit { prefs ->
            prefs[StreakKeys.Streak] = 0
        }
    }
}
