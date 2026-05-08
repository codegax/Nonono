package com.example.nonono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.levelsProgressStore: DataStore<Preferences> by preferencesDataStore(name = "levels_progress")

private object LevelsPrefKeys {
    val EpochDay = longPreferencesKey("epoch_day")
    val Solved = stringPreferencesKey("solved_indices")
}

class LevelsProgressRepository(context: Context) {
    private val store = context.applicationContext.levelsProgressStore

    val solvedToday: Flow<Set<Int>> = store.data.map { prefs ->
        val storedDay = prefs[LevelsPrefKeys.EpochDay] ?: -1L
        if (storedDay != currentEpochDay()) {
            emptySet()
        } else {
            prefs[LevelsPrefKeys.Solved]
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet()
                ?: emptySet()
        }
    }

    suspend fun markSolved(index: Int) {
        store.edit { prefs ->
            val today = currentEpochDay()
            val storedDay = prefs[LevelsPrefKeys.EpochDay] ?: -1L
            val current = if (storedDay == today) {
                prefs[LevelsPrefKeys.Solved]
                    ?.split(",")
                    ?.mapNotNull { it.toIntOrNull() }
                    ?.toMutableSet()
                    ?: mutableSetOf()
            } else {
                mutableSetOf()
            }
            current.add(index)
            prefs[LevelsPrefKeys.EpochDay] = today
            prefs[LevelsPrefKeys.Solved] = current.sorted().joinToString(",")
        }
    }
}
