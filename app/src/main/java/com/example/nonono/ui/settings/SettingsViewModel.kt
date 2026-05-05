package com.example.nonono.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nonono.data.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UserSettingsRepository(app)

    val automaticRowSolver: StateFlow<Boolean> =
        repo.automaticRowSolver.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    fun setAutomaticRowSolver(enabled: Boolean) {
        viewModelScope.launch { repo.setAutomaticRowSolver(enabled) }
    }
}
