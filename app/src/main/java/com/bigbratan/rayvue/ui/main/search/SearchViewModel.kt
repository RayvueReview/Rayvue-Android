package com.bigbratan.rayvue.ui.main.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.services.GamesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val gamesService: GamesService,
) : ViewModel() {
    val searchedGamesState =
        MutableStateFlow<SearchedGamesState>(SearchedGamesState.Idle)

    fun searchGames(query: String) {
        viewModelScope.launch {
            searchedGamesState.value = SearchedGamesState.Loading

            try {
                val games = gamesService.searchGames(query)

                searchedGamesState.value = SearchedGamesState.Success(games)
            } catch (e: Exception) {
                searchedGamesState.value = SearchedGamesState.Error
            }
        }
    }
}

sealed class SearchedGamesState {
    object Idle : SearchedGamesState()

    object Loading : SearchedGamesState()

    data class Success(
        val games: List<Game>
    ) : SearchedGamesState()

    object Error : SearchedGamesState()
}