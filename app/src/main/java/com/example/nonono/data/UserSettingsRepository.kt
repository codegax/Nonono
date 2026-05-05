package com.example.nonono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userSettingsStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

private object Keys {
    val AutomaticRowSolver = booleanPreferencesKey("automatic_row_solver")
}

class UserSettingsRepository(context: Context) {
    private val store = context.applicationContext.userSettingsStore

    val automaticRowSolver: Flow<Boolean> =
        store.data.map { it[Keys.AutomaticRowSolver] ?: true }

    suspend fun setAutomaticRowSolver(enabled: Boolean) {
        store.edit { it[Keys.AutomaticRowSolver] = enabled }
    }
}
