package com.bigbratan.rayvue.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.services.FirebaseStorageService
import com.bigbratan.rayvue.services.GamesService
import com.bigbratan.rayvue.services.UserService
import com.bigbratan.rayvue.ui.main.games.GamesItemViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val userService: UserService,
    private val gamesService: GamesService,
) : ViewModel() {
    val startDestination: MutableStateFlow<String?> = MutableStateFlow(null)

    val searchedGamesState =
        MutableStateFlow<SearchedGamesState>(SearchedGamesState.Loading)

    init {
        if (userService.isUserLoggedIn) {
            viewModelScope.launch {
                userService.user.collect { user ->
                    if (user != null) {
                        startDestination.value = Screen.Main.route
                    }
                }
            }
        } else {
            startDestination.value = Screen.Auth.route
        }
    }

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
    object Loading : SearchedGamesState()

    data class Success(
        val games: List<Game>
    ) : SearchedGamesState()

    object Error : SearchedGamesState()
}