package com.example.nonono.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nonono.data.DailyOutcome
import com.example.nonono.data.DailyPlayRepository
import com.example.nonono.data.LevelsProgressRepository
import com.example.nonono.data.StreakRepository
import com.example.nonono.data.currentEpochDay
import com.example.nonono.domain.Board
import com.example.nonono.domain.generateLevel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val dailyRepo = DailyPlayRepository(app)
    private val levelsRepo = LevelsProgressRepository(app)
    private val streakRepo = StreakRepository(app)

    val dailyOutcome: StateFlow<DailyOutcome> = dailyRepo.today.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DailyOutcome.Pending,
    )

    val levelsSolvedCount: StateFlow<Int> = levelsRepo.solvedToday
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0,
        )

    val streak: StateFlow<Int> = streakRepo.current.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0,
    )

    val dailySolution: Board? = generateLevel(
        name = "Today",
        seed = currentEpochDay(),
        width = 5,
        height = 5,
    )?.solution
}
