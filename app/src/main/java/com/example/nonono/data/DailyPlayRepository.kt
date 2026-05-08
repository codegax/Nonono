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

private val Context.dailyPlayStore: DataStore<Preferences> by preferencesDataStore(name = "daily_play")

private object DailyPrefKeys {
    val EpochDay = longPreferencesKey("epoch_day")
    val Outcome = stringPreferencesKey("outcome")
}

enum class DailyOutcome { Pending, Won, Lost }

fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

class DailyPlayRepository(context: Context) {
    private val store = context.applicationContext.dailyPlayStore

    val today: Flow<DailyOutcome> = store.data.map { prefs ->
        val storedDay = prefs[DailyPrefKeys.EpochDay] ?: -1L
        if (storedDay != currentEpochDay()) {
            DailyOutcome.Pending
        } else {
            when (prefs[DailyPrefKeys.Outcome]) {
                "Won" -> DailyOutcome.Won
                "Lost" -> DailyOutcome.Lost
                else -> DailyOutcome.Pending
            }
        }
    }

    suspend fun recordOutcome(outcome: DailyOutcome) {
        store.edit { prefs ->
            prefs[DailyPrefKeys.EpochDay] = currentEpochDay()
            prefs[DailyPrefKeys.Outcome] = outcome.name
        }
    }
}
