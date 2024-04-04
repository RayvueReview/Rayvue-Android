package com.bigbratan.rayvue.ui.main.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.services.GamesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val gamesService: GamesService,
) : ViewModel() {
    val obtainedGamesState = MutableStateFlow<ObtainedGamesState>(ObtainedGamesState.Loading)
    val isRefreshing = MutableStateFlow(false)

    fun getData(
        canRefresh: Boolean = true,
    ) {
        viewModelScope.launch {
            try {
                if (canRefresh)
                    isRefreshing.value = true

                val games = gamesService.fetchGames()

                obtainedGamesState.value = ObtainedGamesState.Success(GamesItemViewModel(games))
            } catch (e: Exception) {
                if (canRefresh)
                    isRefreshing.value = true

                obtainedGamesState.value = ObtainedGamesState.Error
            } finally {
                if (canRefresh)
                    isRefreshing.value = false
            }
        }
    }
}

sealed class ObtainedGamesState {
    object Loading : ObtainedGamesState()

    data class Success(
        val games: GamesItemViewModel
    ) : ObtainedGamesState()

    object Error : ObtainedGamesState()
}

data class GamesItemViewModel(
    private val games: List<Game>
) {
    val allGames = games.sortedBy { game -> game.displayName }

    val recentGames = games.sortedByDescending { game -> game.dateAdded }.take(10)

    val randomGames = games.shuffled().take(10)
}